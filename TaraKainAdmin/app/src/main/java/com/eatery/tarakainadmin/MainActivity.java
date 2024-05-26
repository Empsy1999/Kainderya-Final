package com.eatery.tarakainadmin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eatery.tarakainadmin.notification.NotificationHelper;
import com.eatery.tarakainadmin.notification.Notifications;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OwnersAdapter.DataTransferInterface {
    private String adminId;
    private LinearLayout triviaBtn, logoutBtn, notificationBtn;
    private RecyclerView recyclerView;
    private OwnersAdapter userAdapter;
    private ArrayList<Owners> ownersArrayList = new ArrayList<>();
    private RelativeLayout userDetailsRl;
    private ImageView profileImg, statusImg, deleteUserImg;
    private EditText inputName, inputEateryName, inputEmail, inputMobileNumber, inputPassword;
    private LinearLayout sendVerificationBtn, sendEmailMessageBtn, saveBtn, viewIdBtn;
    private String recipientId, recipientEmail;
    private Owners owner;
    private ProgressDialog progressDialog;
    private boolean isChecked = false;
    private NotificationAdapter notificationAdapter;
    private ArrayList<Notifications> notificationsArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("admin", MODE_PRIVATE);
        adminId = sharedPreferences.getString("id", null);

        NotificationHelper.newVolleyRequestQueue(this);

        initViews();
        setClickListener();
        getOwnersData();
        askNotificationPermission();

    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                    Log.d("==permission", "granted notification");
                } else {
                    Toast.makeText(this, "Allow permission to receive push notification", Toast.LENGTH_SHORT).show();
                    showPermissionDialog("Notification Permission");
                }
            });

    private void showPermissionDialog(String notificationPermission) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Alert for Permission")
                .setMessage(notificationPermission)
                .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                Log.d("==permission", "granted");
            }
//            else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
//                // TODO: display an educational UI explaining to the user the features that will be enabled
//                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
//                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
//                //       If the user selects "No thanks," allow the user to continue without notifications.
//            }
            else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void getOwnersData() {
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Owners");
        customerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ownersArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Owners owners = dataSnapshot.getValue(Owners.class);
                        ownersArrayList.add(owners);
                        userAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        userAdapter = new OwnersAdapter(this, ownersArrayList, this);
        recyclerView.setAdapter(userAdapter);

    }

    private void setClickListener() {
        triviaBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, TriviaActivity.class));
        });

        sendVerificationBtn.setOnClickListener(view -> {
            String id = String.valueOf(new Random().nextInt((999999 - 111111) + 1) + 111111);
            Log.d("==code", id);
            String subject = "TaraKain" + " Verification Code";
            String body = "Your verification code is " + id;
            NotificationHelper.sendPushNotification(MainActivity.this,
                    adminId, adminId, recipientId, subject, body, "verification");
            updateVerificationCode(recipientId, id);
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientEmail});
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            intent.setData(Uri.parse("mailto:"));
            startActivity(Intent.createChooser(intent, "Choose an Email client :"));
        });

        sendEmailMessageBtn.setOnClickListener(view -> {
            String subject = "TaraKain" + " Account Verification Update";
            String body = "This email is to inform you about you account has been verified. ";
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientEmail});
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            intent.setData(Uri.parse("mailto:"));
            startActivity(Intent.createChooser(intent, "Choose an Email client :"));
        });

        statusImg.setOnClickListener(view -> {
            showProgressDialog();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Owners");
            String status = owner.getStatus();
            if (status.equals(getString(R.string.verified))) {
                status = getString(R.string.not_verified);
            } else {
                status = getString(R.string.verified);
            }
            String finalStatus = status;
            reference.child(recipientId).child("status").setValue(status)
                    .addOnCompleteListener(task -> {
                        hideProgressDialog();
                        if (task.isSuccessful()) {
                            NotificationHelper.sendPushNotification(MainActivity.this,
                                    adminId, adminId, recipientId, "Account Verification", "Congratulation! Your account is now Verified successfully.", "verification");
                            userDetailsRl.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Owners" + " account status is " + finalStatus, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Try again later", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        hideProgressDialog();
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        deleteUserImg.setOnClickListener(view -> {
            showProgressDialog();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Owners");
            reference.child(recipientId).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            hideProgressDialog();
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Owner" + " deleted successfully.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Please try again...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        });

        saveBtn.setOnClickListener(view -> {
            String name = inputName.getText().toString();
            String eateryName = inputEateryName.getText().toString();
            String email = inputEmail.getText().toString();
            //String phone = inputMobileNumber.getText().toString();
            String password = inputPassword.getText().toString();

            if (name.isEmpty()) {
                inputName.setError(getString(R.string.name));
                inputName.requestFocus();
            } else if (eateryName.isEmpty()) {
                inputEateryName.setError(getString(R.string.eatery_name));
                inputEateryName.requestFocus();
            } else if (!email.contains("@")) {
                inputEmail.setError(getString(R.string.email));
                inputEmail.requestFocus();
            }
//            else if (phone.isEmpty() || phone.length() < 11){
//                inputMobileNumber.setError(getString(R.string.mobile_number));
//                inputMobileNumber.requestFocus();
//            }
            else if (password.isEmpty() || password.length() < 8) {
                inputPassword.setError(getString(R.string.password));
                inputPassword.requestFocus();
            } else {
                showProgressDialog();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Owners");
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("id", recipientId);
                hashMap.put("ownerName", name);
                hashMap.put("eateryName", eateryName);
                hashMap.put("email", email);
                //hashMap.put("phone", phone);
                hashMap.put("password", password);
                reference.child(recipientId).updateChildren(hashMap)
                        .addOnCompleteListener(task -> {
                            hideProgressDialog();
                            if (task.isSuccessful()) {
                                userDetailsRl.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "User details updated successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Try again later", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> {
                            hideProgressDialog();
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        viewIdBtn.setOnClickListener(view -> {
            showViewOwnerIdDialog();
        });

        logoutBtn.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("admin", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLogin", false);
            editor.apply();
            startActivity(new Intent(MainActivity.this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        notificationBtn.setOnClickListener(view -> {
            viewAllNotificationDialog();
        });

    }

    private void viewAllNotificationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_view_notification);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView recyclerView = dialog.findViewById(R.id.notification_recycler);
        TextView noDataTxt = dialog.findViewById(R.id.no_notify_txt);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        getNotificationData(recyclerView, noDataTxt);

        dialog.show();
    }

    private void getNotificationData(RecyclerView recyclerView, TextView noDataTxt) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    notificationsArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Notifications notifications = dataSnapshot.getValue(Notifications.class);
                        assert notifications != null;
                        if (notifications.getReceiverId().equals(adminId)){
                            noDataTxt.setVisibility(View.GONE);
                            notificationsArrayList.add(notifications);
                            notificationsArrayList.sort((t1, t2) -> {
                                try {
                                    Date start = new SimpleDateFormat("yyyyddMMHHmmss")
                                            .parse(t1.getDate());
                                    Date end = new SimpleDateFormat("yyyyddMMHHmmss")
                                            .parse(t2.getDate());
                                    Log.d("== sts", start + " " + end);
                                    assert end != null;
                                    return end.compareTo(start);
                                } catch (ParseException e) {
                                    return t2.getDate().compareToIgnoreCase(t1.getDate());
                                }
                            });
                            notificationAdapter.notifyDataSetChanged();
                            reference.child(notifications.getId()).child("status").setValue("read");
                        }
                    }
                }
                if (notificationsArrayList.isEmpty()){
                    noDataTxt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        notificationAdapter = new NotificationAdapter(this, notificationsArrayList);
        recyclerView.setAdapter(notificationAdapter);
    }

    private void showViewOwnerIdDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_view_owner_id);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch toogleSwitch = dialog.findViewById(R.id.toggle_switch_id);
        ImageView frontIdImg = dialog.findViewById(R.id.front_id_image);
        ImageView backIdImg = dialog.findViewById(R.id.back_id_image);
        RelativeLayout backImg = dialog.findViewById(R.id.back_img);
        TextView imageTypeTxt = dialog.findViewById(R.id.image_type_txt);

        backImg.setOnClickListener(view -> {
            dialog.dismiss();
        });

        if (owner != null) {
            Log.d("==url", owner.getPermitDocUrl() + " : \n " + owner.getIdPicUrl());
            Glide.with(this).load(owner.getPermitDocUrl())
                    .placeholder(R.drawable.view_id).into(frontIdImg);
            Glide.with(this).load(owner.getIdPicUrl())
                    .placeholder(R.drawable.view_id).into(backIdImg);
        }

        toogleSwitch.setChecked(isChecked);
        if (isChecked) {
            imageTypeTxt.setText(R.string.business_permit);
            frontIdImg.setVisibility(View.VISIBLE);
            backIdImg.setVisibility(View.GONE);
        } else {
            imageTypeTxt.setText(R.string.valid_id);
            frontIdImg.setVisibility(View.GONE);
            backIdImg.setVisibility(View.VISIBLE);
        }

        toogleSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            isChecked = checked;
            if (isChecked) {
                imageTypeTxt.setText(R.string.business_permit);
                frontIdImg.setVisibility(View.VISIBLE);
                backIdImg.setVisibility(View.GONE);
            } else {
                imageTypeTxt.setText(R.string.valid_id);
                frontIdImg.setVisibility(View.GONE);
                backIdImg.setVisibility(View.VISIBLE);
            }
        });

        dialog.show();
    }

    private void updateVerificationCode(String userId, String code) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Owners");
        reference.child(userId).child("verificationCode").setValue(code);
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        progressDialog.dismiss();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        userDetailsRl = findViewById(R.id.rl1);
        profileImg = findViewById(R.id.profile_img);
        statusImg = findViewById(R.id.status_img);
        deleteUserImg = findViewById(R.id.delete_user);
        inputName = findViewById(R.id.input_name);
        inputEateryName = findViewById(R.id.input_eatery_name);
        inputEmail = findViewById(R.id.input_email);
        inputMobileNumber = findViewById(R.id.input_mobile_number);
        inputPassword = findViewById(R.id.input_password);
        sendVerificationBtn = findViewById(R.id.send_verification_btn);
        sendEmailMessageBtn = findViewById(R.id.send_email_btn);
        saveBtn = findViewById(R.id.save_btn);
        viewIdBtn = findViewById(R.id.view_id_btn);
        triviaBtn = findViewById(R.id.trivia_btn);
        logoutBtn = findViewById(R.id.logout_btn);
        notificationBtn = findViewById(R.id.notification_btn);
    }

    @Override
    public void onSetValues(Owners owners) {
        owner = owners;
        userDetailsRl.setVisibility(View.VISIBLE);
        if (owners.getStatus().equals(getString(R.string.verified))) {
            statusImg.setImageResource(R.drawable.verified_img);
        } else {
            statusImg.setImageResource(R.drawable.ic_block_24);
        }
        Glide.with(this).load(owners.getEateryImage()).into(profileImg);
        inputName.setText(owners.getOwnerName());
        inputEateryName.setText(owners.getEateryName());
        inputEmail.setText(owners.getEmail());
        //inputMobileNumber.setText(owners.getPhone());
        inputPassword.setText(owners.getPassword());
        recipientEmail = owners.getEmail();
        recipientId = owners.getId();
    }

    @Override
    public void onSetValues(String id) {

    }
}
package com.eatery.tarakain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.documentfile.provider.DocumentFile;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eatery.tarakain.notification.NotificationHelper;
import com.eatery.tarakain.notification.Token;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private EditText eateryNameEdtTxt, ownerNameEdtTxt, emailEdtTxt, passwordEdtTxt, confirmPasswordEdtTxt;
    private String eateryName, ownerName, email, password, confirmPassword, downloadImageUrl, downloadDocUrl, ownerRandomKey, id;
    private Uri ImageUri;
    private LinearLayout uploadPicIdBtn, uploadBusinessPermitBtn;
    private ImageView idPickIv, permitIv;
    private AppCompatButton registerBtn;
    private StorageReference ownerImageRef;
    private ProgressDialog loadingBar;
    private DatabaseReference ownerRef;
    private boolean isExists = false;
    private int ID_IMAGE_REQ_CODE = 101;
    private int PERMIT_DOC_REQ_CODE = 102;
    private Uri documentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();

        ownerImageRef = FirebaseStorage.getInstance().getReference().child("Owner Id Images");
        ownerRef = FirebaseDatabase.getInstance().getReference().child("Owners");
        loadingBar = new ProgressDialog(this);

        registerBtn.setOnClickListener(view -> {
            validateInputData();
        });

        uploadPicIdBtn.setOnClickListener(view -> {
            ImagePicker.with(RegisterActivity.this)
                    .galleryOnly()
                    .galleryMimeTypes(new String[]{
                            "image/png",
                            "image/jpg",
                            "image/jpeg"
                    })
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(ID_IMAGE_REQ_CODE);
        });

        uploadBusinessPermitBtn.setOnClickListener(view -> {
//            Intent intent = new Intent();
//            intent.setType("application/pdf");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            startActivityForResult(Intent.createChooser(intent, "Choose document.."), PERMIT_DOC_REQ_CODE);
            ImagePicker.with(RegisterActivity.this)
                    .galleryOnly()
                    .galleryMimeTypes(new String[]{
                            "image/png",
                            "image/jpg",
                            "image/jpeg"
                    })
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(PERMIT_DOC_REQ_CODE);
        });

    }

    private void validateInputData() {
        eateryName = eateryNameEdtTxt.getText().toString();
        email = emailEdtTxt.getText().toString();
        ownerName = ownerNameEdtTxt.getText().toString();
        password = passwordEdtTxt.getText().toString();
        confirmPassword = confirmPasswordEdtTxt.getText().toString();

        if (eateryName.isEmpty()) {
            eateryNameEdtTxt.setError(getString(R.string.input_eatery_name));
            eateryNameEdtTxt.requestFocus();
        } else if (email.isEmpty() || !email.contains("@")) {
            emailEdtTxt.setError(getString(R.string.input_email_address));
            emailEdtTxt.requestFocus();
        } else if (ownerName.isEmpty()) {
            ownerNameEdtTxt.setError(getString(R.string.input_name));
            ownerNameEdtTxt.requestFocus();
        } else if (password.isEmpty()) {
            passwordEdtTxt.setError(getString(R.string.input_password));
            passwordEdtTxt.requestFocus();
        } else if (password.length() < 6) {
            passwordEdtTxt.setError(getString(R.string.input_valid_password));
            passwordEdtTxt.requestFocus();
        } else if (confirmPassword.isEmpty()) {
            confirmPasswordEdtTxt.setError(getString(R.string.input_confirm_password));
            confirmPasswordEdtTxt.requestFocus();
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordEdtTxt.setError(getString(R.string.password_not_match));
            confirmPasswordEdtTxt.requestFocus();
        } else if (ImageUri == null) {
            Toast.makeText(this, "Id Picture is mandatory...", Toast.LENGTH_SHORT).show();
        } else if (documentUri == null) {
            Toast.makeText(this, "Please select permit document image to upload..", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setMessage("Please wait...");
            loadingBar.setCancelable(false);
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    isExists = false;
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String checkEmail = dataSnapshot.child("email").getValue(String.class);
                            if (Objects.equals(checkEmail, email)) {
                                loadingBar.dismiss();
                                isExists = true;
                                Toast.makeText(RegisterActivity.this, "Account with this Email Already registered...", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            }
                        }
                    }
                    if (!isExists) {
                        storeOwnerInformation();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadingBar.dismiss();
                    Toast.makeText(RegisterActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void storeOwnerInformation() {


        DatabaseReference pushedPostRef = FirebaseDatabase.getInstance().getReference();
        ownerRandomKey = pushedPostRef.push().getKey();

        id = FirebaseDatabase.getInstance().getReference().push().getKey();


        final StorageReference filepath = ownerImageRef.child(ImageUri.getLastPathSegment() + ownerRandomKey + ".jpg");
        final UploadTask uploadTask = filepath.putFile(ImageUri);

        uploadTask.addOnFailureListener(e -> {
            String message = e.toString();
            Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();

        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(RegisterActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }


                downloadImageUrl = filepath.getDownloadUrl().toString();
                return filepath.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    downloadImageUrl = task.getResult().toString();
                    Toast.makeText(RegisterActivity.this, "Got the image", Toast.LENGTH_SHORT).show();
                    uploadPermitDocument();
                }
            });
        });

    }

    private void uploadPermitDocument() {
        StorageReference moduleDocumentRef = FirebaseStorage.getInstance().getReference().child("Permit Document");

        DatabaseReference pushedPostRef = FirebaseDatabase.getInstance().getReference();
        String docId = pushedPostRef.push().getKey();

        final StorageReference filepath = moduleDocumentRef.child(docId + ".jpg");
        final UploadTask uploadTask = filepath.putFile(documentUri);

        uploadTask.addOnFailureListener(e -> {
            String message = e.toString();
            Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();

        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(RegisterActivity.this, "Permit uploaded successfully", Toast.LENGTH_SHORT).show();

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }


                    downloadDocUrl = filepath.getDownloadUrl().toString();
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    downloadDocUrl = task.getResult().toString();
                    Toast.makeText(RegisterActivity.this, "Got the permit image", Toast.LENGTH_SHORT).show();
                    SaveOwnerInfoToDatabase();
                }
            });
        });

    }

    private void SaveOwnerInfoToDatabase() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", id);
        hashMap.put("email", email);
        hashMap.put("eateryName", eateryName);
        hashMap.put("ownerName", ownerName);
        hashMap.put("password", password);
        hashMap.put("idPicUrl", downloadImageUrl);
        hashMap.put("permitDocUrl", downloadDocUrl);
        hashMap.put("status", getString(R.string.pending));

        ownerRef.child(id).updateChildren(hashMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateToken(id);
                        saveOwnerInfo();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        sendNotificationToAdmin();

                        loadingBar.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account Created successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        loadingBar.dismiss();
                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    loadingBar.dismiss();
                    Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotificationToAdmin() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Admin");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String adminId = dataSnapshot.child("id").getValue(String.class);
                        assert adminId != null;
                        Log.d("==adminId", adminId);
                        NotificationHelper.sendPushNotification(RegisterActivity.this,
                                id, id, adminId,
                                "New Owner Account",
                                ownerName + " created new account. Check details and verify account."
                                , "signup");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void updateToken(String id) {
        SharedPreferences sharedPreferences1 = getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        editor1.putString("id", id);
        editor1.apply();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("==", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("===", token);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
                    Token token1 = new Token(token);
                    assert id != null;
                    ref.child(id).setValue(token1);
                });
    }

    private void saveOwnerInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("owners", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ownerName", ownerName);
        editor.putString("eateryName", eateryName);
        editor.putString("email", email);
        editor.putString("ownerId", id);
        editor.apply();
    }

    private void initView() {
        eateryNameEdtTxt = findViewById(R.id.input_eatery_name);
        ownerNameEdtTxt = findViewById(R.id.input_owner_name);
        emailEdtTxt = findViewById(R.id.input_email);
        passwordEdtTxt = findViewById(R.id.input_password);
        confirmPasswordEdtTxt = findViewById(R.id.input_confirm_password);
        uploadPicIdBtn = findViewById(R.id.upload_pic_btn);
        idPickIv = findViewById(R.id.id_pic_iv);
        permitIv = findViewById(R.id.permit_iv);
        uploadBusinessPermitBtn = findViewById(R.id.upload_business_permit);
        registerBtn = findViewById(R.id.register_btn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == ID_IMAGE_REQ_CODE) {
                ImageUri = data.getData();
                idPickIv.setImageURI(ImageUri);
            }
            if (requestCode == PERMIT_DOC_REQ_CODE) {
//                DocumentFile documentFile = DocumentFile.fromSingleUri(this, data.getData());
//                assert documentFile != null;
                documentUri = data.getData();
                permitIv.setImageURI(documentUri);
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        YoYo.with(Techniques.Pulse)
                .repeat(-1)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(5000)
                .playOn(findViewById(R.id.logo));

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1200)
                .playOn(findViewById(R.id.register_img));

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1300)
                .playOn(findViewById(R.id.register_txt));

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1400)
                .playOn(eateryNameEdtTxt);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1500)
                .playOn(emailEdtTxt);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1600)
                .playOn(ownerNameEdtTxt);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1700)
                .playOn(passwordEdtTxt);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1800)
                .playOn(confirmPasswordEdtTxt);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1900)
                .playOn(uploadPicIdBtn);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1900)
                .playOn(uploadBusinessPermitBtn);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(2000)
                .playOn(registerBtn);
    }
}
package com.eatery.tarakain;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eatery.tarakain.model.Owners;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class AddMenuActivity extends AppCompatActivity {
    private TextView eaterNameTxt, ownerNameTxt;
    private EditText inputTitle, inputPrice, inputDescription;
    private String title, eateryName, ownerName, eateryImg, email, ownerId,
            price, description, downloadImageUrl, eateryImageUrl, id, menuRandomKey;
    private Uri ImageUri, eaterImgUri;
    private ImageView updateImage, uploadMenuImg, checkMenuImg;
    private TextView updateTxt, viewAllMenuTxt;
    private StorageReference menuImageRef, eateryImgRef;
    private ProgressDialog loadingBar;
    private DatabaseReference menuRef;
    private ImageView uploadEateryImg;
    private int MENU_IMAGE_REQ_CODE = 101;
    private int EATERY_IMAGE_REQ_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu);

        initView();

        SharedPreferences sharedPreferences = getSharedPreferences("owners", MODE_PRIVATE);
        eateryName = sharedPreferences.getString("eateryName", null);
        ownerName = sharedPreferences.getString("ownerName", null);
        email = sharedPreferences.getString("email", null);
        ownerId = sharedPreferences.getString("ownerId", null);

        menuImageRef = FirebaseStorage.getInstance().getReference().child("Menu Images");
        eateryImgRef = FirebaseStorage.getInstance().getReference().child("Eatery Images");
        menuRef = FirebaseDatabase.getInstance().getReference().child("Menu");
        loadingBar = new ProgressDialog(this);

        if (eateryName != null) {
            eaterNameTxt.setText(eateryName);
        }
        if (ownerName != null) {
            ownerNameTxt.setText(ownerName);
        }

        uploadMenuImg.setOnClickListener(view -> {
            ImagePicker.with(AddMenuActivity.this)
                    .galleryOnly()
                    .galleryMimeTypes(new String[]{
                            "image/png",
                            "image/jpg",
                            "image/jpeg"
                    })
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(MENU_IMAGE_REQ_CODE);
        });

        uploadEateryImg.setOnClickListener(view -> {
            ImagePicker.with(AddMenuActivity.this)
                    .galleryOnly()
                    .galleryMimeTypes(new String[]{
                            "image/png",
                            "image/jpg",
                            "image/jpeg"
                    })
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(EATERY_IMAGE_REQ_CODE);
        });

        updateTxt.setOnClickListener(view -> {
            validateMenuData();
        });

        updateImage.setOnClickListener(view -> {
            validateMenuData();
        });

        checkMenuImg.setOnClickListener(view -> {
            startActivity(new Intent(AddMenuActivity.this, UpdateMenuActivity.class));
        });

        viewAllMenuTxt.setOnClickListener(view -> {
            startActivity(new Intent(AddMenuActivity.this, UpdateMenuActivity.class));
        });

        slideInLeft();

        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void getEateryDetails() {
        DatabaseReference ownerRef = FirebaseDatabase.getInstance().getReference().child("Owners");
        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Owners owners = dataSnapshot.getValue(Owners.class);
                        assert owners != null;
                        if (Objects.equals(owners.getEmail(), email)) {
                            eateryImg = owners.getEateryImage();
                            if (eateryImg != null) {
                                if (!eateryImg.isEmpty()) {
                                    Glide.with(AddMenuActivity.this)
                                            .load(eateryImg)
                                            .placeholder(R.drawable.logo)
                                            .into(uploadEateryImg);
                                }
                            }
                            if (owners.getLatitude() == null ||
                                    owners.getLongitude() == null ||
                                    owners.getLongitude().isEmpty() ||
                                    owners.getLatitude().isEmpty()) {
                               // showAddLatLngDialog();
                                startActivity(new Intent(AddMenuActivity.this, PickLocationActivity.class));
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showAddLatLngDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_lnglat);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText inputLatitude = dialog.findViewById(R.id.dialog_input_latitude);
        EditText inputLongitude = dialog.findViewById(R.id.dialog_input_longitude);
        ImageView save = dialog.findViewById(R.id.dialog_save_loc_info);
        TextView textView = dialog.findViewById(R.id.dialog_text_view);
        save.setOnClickListener(view -> {
            String longitude = inputLongitude.getText().toString();
            String latitude = inputLatitude.getText().toString();
            if (latitude.isEmpty()) {
                inputLatitude.setError(getString(R.string.input_latitude));
                inputLatitude.requestFocus();
            } else if (longitude.isEmpty()) {
                inputLongitude.setError(getString(R.string.input_longitude));
                inputLongitude.requestFocus();
            } else {
                loadingBar.setMessage(getString(R.string.please_wait));
                loadingBar.setCancelable(false);
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                DatabaseReference ownerRef = FirebaseDatabase.getInstance().getReference().child("Owners");
                ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Owners owners = dataSnapshot.getValue(Owners.class);
                                assert owners != null;
                                if (Objects.equals(owners.getEmail(), email)) {
                                    String id = owners.getId();
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("longitude", longitude);
                                    hashMap.put("latitude", latitude);
                                    ownerRef.child(id).updateChildren(hashMap)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    loadingBar.dismiss();
                                                    dialog.dismiss();
                                                    Toast.makeText(AddMenuActivity.this, "Location Added...", Toast.LENGTH_SHORT).show();
                                                    startActivity(getIntent());
                                                    finish();
                                                } else {
                                                    loadingBar.dismiss();
                                                    dialog.dismiss();
                                                    Toast.makeText(AddMenuActivity.this, "Failed to update, " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(e -> {
                                                loadingBar.dismiss();
                                                dialog.dismiss();
                                                Toast.makeText(AddMenuActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddMenuActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.show();
    }

    private void validateMenuData() {
        title = inputTitle.getText().toString();
        price = inputPrice.getText().toString();
        description = inputDescription.getText().toString();
        if (ImageUri == null) {
            Toast.makeText(this, "Menu Image required...", Toast.LENGTH_SHORT).show();
        } else if (title.isEmpty()) {
            inputTitle.setError(getString(R.string.input_title));
            inputTitle.requestFocus();
        } else if (price.isEmpty()) {
            inputPrice.setError(getString(R.string.input_price));
            inputPrice.requestFocus();
        } else if (description.isEmpty()) {
            inputDescription.setError(getString(R.string.input_description));
            inputPrice.requestFocus();
        } else {
            loadingBar.setMessage("Please wait...");
            loadingBar.setCancelable(false);
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            storeMenuInformation();
        }
    }

    private void storeMenuInformation() {


        DatabaseReference pushedPostRef = FirebaseDatabase.getInstance().getReference();
        menuRandomKey = pushedPostRef.push().getKey();

        id = FirebaseDatabase.getInstance().getReference().push().getKey();


        final StorageReference filepath = menuImageRef.child(ImageUri.getLastPathSegment() + menuRandomKey + ".jpg");
        final UploadTask uploadTask = filepath.putFile(ImageUri);

        uploadTask.addOnFailureListener(e -> {
            String message = e.toString();
            Toast.makeText(AddMenuActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();

        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(AddMenuActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }


                downloadImageUrl = filepath.getDownloadUrl().toString();
                return filepath.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    downloadImageUrl = task.getResult().toString();
                    Toast.makeText(AddMenuActivity.this, "Got the image", Toast.LENGTH_SHORT).show();
                    storeInfoToDatabase();
                }
            });
        });

    }

    private void storeInfoToDatabase() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", id);
        hashMap.put("email", email);
        hashMap.put("ownerId", ownerId);
        hashMap.put("title", title);
        hashMap.put("price", price);
        hashMap.put("description", description);
        hashMap.put("idPicUrl", downloadImageUrl);

        menuRef.child(id).updateChildren(hashMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        inputPrice.setText("");
                        id = null;
                        inputDescription.setText("");
                        uploadMenuImg.setImageResource(R.drawable.add);
                        Intent intent = new Intent(AddMenuActivity.this, ConfirmUpdateActivity.class);
                        startActivity(intent);

                        loadingBar.dismiss();
                        Toast.makeText(AddMenuActivity.this, "Menu Successfully Uploaded", Toast.LENGTH_SHORT).show();
                    } else {
                        loadingBar.dismiss();
                        String message = task.getException().toString();
                        Toast.makeText(AddMenuActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }

                }).addOnFailureListener(e -> {
                    loadingBar.dismiss();
                    Toast.makeText(AddMenuActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void initView() {
        eaterNameTxt = findViewById(R.id.input_eatery_name);
        ownerNameTxt = findViewById(R.id.input_owner_name);
        inputTitle = findViewById(R.id.input_menu_title);
        inputPrice = findViewById(R.id.input_menu_price);
        inputDescription = findViewById(R.id.input_menu_desc);
        updateImage = findViewById(R.id.update_menu_img);
        updateTxt = findViewById(R.id.update_menu_txt);
        uploadMenuImg = findViewById(R.id.upload_menu_img);
        checkMenuImg = findViewById(R.id.check_menu_img);
        viewAllMenuTxt = findViewById(R.id.view_all_menu_txt);
        uploadEateryImg = findViewById(R.id.upload_eatery_img);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == MENU_IMAGE_REQ_CODE) {
                ImageUri = data.getData();
                uploadMenuImg.setImageURI(ImageUri);
            }
            if (requestCode == EATERY_IMAGE_REQ_CODE) {
                eaterImgUri = data.getData();
                uploadEateryImg.setImageURI(eaterImgUri);
                storeEateryImgToDatabase();
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }

    }

    private void storeEateryImgToDatabase() {
        DatabaseReference pushedPostRef = FirebaseDatabase.getInstance().getReference();
        String key = pushedPostRef.push().getKey();

        final StorageReference filepath = eateryImgRef.child(eaterImgUri.getLastPathSegment() + key + ".jpg");
        final UploadTask uploadTask = filepath.putFile(eaterImgUri);

        uploadTask.addOnFailureListener(e -> {
            String message = e.toString();
            Toast.makeText(AddMenuActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();

        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(AddMenuActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }


                eateryImageUrl = filepath.getDownloadUrl().toString();
                return filepath.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    eateryImageUrl = task.getResult().toString();
                    Toast.makeText(AddMenuActivity.this, "Got the image", Toast.LENGTH_SHORT).show();
                    DatabaseReference ownerRef = FirebaseDatabase.getInstance().getReference().child("Owners");
                    ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Owners owners = dataSnapshot.getValue(Owners.class);
                                    assert owners != null;
                                    if (Objects.equals(owners.getEmail(), email)) {
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("eateryImage", eateryImageUrl);
                                        ownerRef.child(owners.getId()).updateChildren(hashMap)
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        loadingBar.dismiss();
                                                        Toast.makeText(AddMenuActivity.this, "Eatery Image Updated", Toast.LENGTH_SHORT).show();
                                                        startActivity(getIntent());
                                                        finish();
                                                    } else {
                                                        loadingBar.dismiss();
                                                        Toast.makeText(AddMenuActivity.this, "Failed to update, " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(e -> {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(AddMenuActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        YoYo.with(Techniques.BounceIn)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1000)
                .playOn(findViewById(R.id.logo));

        YoYo.with(Techniques.BounceIn)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1200)
                .playOn(findViewById(R.id.input_eatery_name));

        YoYo.with(Techniques.BounceIn)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1300)
                .playOn(findViewById(R.id.input_owner_name));

        YoYo.with(Techniques.BounceIn)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1500)
                .playOn(findViewById(R.id.upload_img));

        YoYo.with(Techniques.BounceIn)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1700)
                .playOn(findViewById(R.id.update_menu));

        YoYo.with(Techniques.BounceIn)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1900)
                .playOn(findViewById(R.id.menu_input));

        getEateryDetails();

    }

    private void slideInLeft() {
        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(2000)
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animator) {
                        slideInRight();
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animator) {

                    }
                })
                .playOn(findViewById(R.id.bottom_img));
    }

    private void slideInRight() {
        YoYo.with(Techniques.SlideOutRight)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(2000)
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animator) {
                        slideInLeft();
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animator) {

                    }
                })
                .playOn(findViewById(R.id.bottom_img));
    }

    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            startActivity(new Intent(AddMenuActivity.this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
    };

}
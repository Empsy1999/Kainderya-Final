package com.eatery.tarakainadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eatery.tarakainadmin.notification.Notifications;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class TriviaActivity extends AppCompatActivity  implements TriviaAdapter.TriviaInterface {
    private String adminId;
    private ImageView backImg;
    private LinearLayout createTrivia;
    private RecyclerView recyclerView;
    private TriviaAdapter adapter;
    private ArrayList<Trivia> triviaArrayList = new ArrayList<>();
    private Trivia trivia;
    private ProgressDialog progressDialog;
    private String id, title, description, downloadImageUrl;
    private Uri postUri;
    private ImageView addTriviaImg;
    private final int POST_IMG_REQ_CODE = 102;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trivia);

        SharedPreferences sharedPreferences = getSharedPreferences("admin", MODE_PRIVATE);
        adminId = sharedPreferences.getString("id", null);

        initViews();
        getTriviaPost();

        createTrivia.setOnClickListener(view -> {
            showCreateTriviaDialog("new");
        });

        backImg.setOnClickListener(view -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

    }

    private void showCreateTriviaDialog(String type) {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_trivia);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LinearLayout postTrivia = dialog.findViewById(R.id.post_trivia);
        EditText inputTriviaTitle = dialog.findViewById(R.id.input_trivia_title);
        EditText inputTriviaDesc = dialog.findViewById(R.id.input_trivia_description);
        addTriviaImg = dialog.findViewById(R.id.add_trivia_image);

        addTriviaImg.setOnClickListener(view -> {
            ImagePicker.with(TriviaActivity.this)
                    .galleryOnly()
                    .galleryMimeTypes(new String[]{
                            "image/png",
                            "image/jpg",
                            "image/jpeg"
                    })
                    .crop(4, 3)
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(POST_IMG_REQ_CODE);
        });

        if (type.equals("edit")){
            if (trivia != null){
                inputTriviaTitle.setText(trivia.getTitle());
                inputTriviaDesc.setText(trivia.getDescription());
                if (trivia.getImage() != null){
                    Glide.with(TriviaActivity.this).load(trivia.getImage()).into(addTriviaImg);
                }
            }
        }

        postTrivia.setOnClickListener(view -> {
            title = inputTriviaTitle.getText().toString();
            description = inputTriviaDesc.getText().toString();
            if (title.isEmpty()){
                inputTriviaTitle.setError(getString(R.string.title));
                inputTriviaTitle.requestFocus();
            } else if (description.isEmpty()) {
                inputTriviaDesc.setError(getString(R.string.description));
                inputTriviaDesc.requestFocus();
            }else {
                downloadImageUrl = "";
                showProgressDialog();
                if (postUri != null){
                    uploadPhotoId(type);
                }else {
                    saveTriviaToDatabase(type);
                }
            }
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == POST_IMG_REQ_CODE) {
                postUri = data.getData();
                addTriviaImg.setImageURI(postUri);
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            postUri = null;
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            postUri = null;
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadPhotoId(String type) {
        StorageReference idImagesRef = FirebaseStorage.getInstance().getReference().child("Trivia Images");

        DatabaseReference pushedPostRef = FirebaseDatabase.getInstance().getReference();
        String imageId = pushedPostRef.push().getKey();

        final StorageReference filepath = idImagesRef.child(postUri.getLastPathSegment() + imageId + ".jpg");
        final UploadTask uploadTask = filepath.putFile(postUri);

        uploadTask.addOnFailureListener(e -> {
            String message = e.toString();
            Toast.makeText(TriviaActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            hideProgressDialog();

        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(TriviaActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }


                    downloadImageUrl = filepath.getDownloadUrl().toString();
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    downloadImageUrl = task.getResult().toString();
                    Toast.makeText(TriviaActivity.this, "Got the image", Toast.LENGTH_SHORT).show();
                    saveTriviaToDatabase(type);
                }
            });
        });

    }

    private void saveTriviaToDatabase(String type) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Trivia");
        id = null;
        if (type.equals("new")){
            id = reference.push().getKey();
        }else {
            id = trivia.getId();
        }
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", id);
        hashMap.put("title", title);
        hashMap.put("description", description);
        hashMap.put("date", getCurrentDate());
        hashMap.put("dateTime", getCurrentDateTime());
        hashMap.put("userId", adminId);
        if (downloadImageUrl != null && !downloadImageUrl.isEmpty() && postUri != null) {
            hashMap.put("image", downloadImageUrl);
        }
        reference.child(id).updateChildren(hashMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        if (dialog != null){
                            dialog.dismiss();
                        }
                        Toast.makeText(TriviaActivity.this, "Post Added", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(TriviaActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                    }
                    hideProgressDialog();
                }).addOnFailureListener(e -> {
                    hideProgressDialog();
                    Toast.makeText(TriviaActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

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

    private void getTriviaPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Trivia");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                triviaArrayList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Trivia trivia = dataSnapshot.getValue(Trivia.class);
                        assert trivia != null;
                        if (trivia.getUserId().equals(adminId)){
                            triviaArrayList.add(trivia);
                            triviaArrayList.sort((t1, t2) -> {
                                try {
                                    Date start = new SimpleDateFormat("yyyyddMMHHmmss")
                                            .parse(t1.getDateTime());
                                    Date end = new SimpleDateFormat("yyyyddMMHHmmss")
                                            .parse(t2.getDateTime());
                                    Log.d("== sts", start + " " + end);
                                    assert end != null;
                                    return end.compareTo(start);
                                } catch (ParseException e) {
                                    return t2.getDateTime().compareToIgnoreCase(t1.getDateTime());
                                }
                            });
                            adapter.notifyDataSetChanged();
                        }
                    }
                }else {
                    Toast.makeText(TriviaActivity.this, "No trivia.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TriviaActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        adapter = new TriviaAdapter(this, triviaArrayList, this);
        recyclerView.setAdapter(adapter);
    }

    private void initViews() {
        backImg = findViewById(R.id.back_img);
        createTrivia = findViewById(R.id.create_trivia);
        recyclerView = findViewById(R.id.trivia_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false
        ));
    }

    @Override
    public void onSetValues(Trivia t) {
        trivia = t;
        showCreateTriviaDialog("edit");
    }

    public static String getCurrentDateTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyddMMHHmmss");
        return dateFormat.format(c.getTime());
    }

    public static String getCurrentDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        return dateFormat.format(c.getTime());
    }

}
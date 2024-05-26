package com.eatery.tarakain;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eatery.tarakain.model.Menus;
import com.eatery.tarakain.model.Owners;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class UpdateMenuActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressDialog loadingBar;
    private ImageView dialogUpdateImg, dialogSaveMenu, addMenu;
    private LinearLayout reviewImg;
    private Spinner categorySpinner;
    private Uri ImageUri;
    private EditText dialogInputTitle, dialogInputPrice, dialogInputDesc;
    private String downloadImgUrl;
    private StorageReference menuImageRef;
    private DatabaseReference menuRef, eaterDetailRef;
    private Dialog dialog;
    private TextView eaterNameTxt, ownerNameTxt, eateryAddressTxt;
    private ImageView eateryImageView;
    private String eateryImgUrl, categoryName, ownerId;
    private String[] menuCategoryName ={"Meat","Vegetable","Seafood","Merienda","Desert"};
    private EditMenuAdapter menuAdapter;
    private ArrayList<Menus> menusArrayList = new ArrayList<>();

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_menu);

        menuImageRef = FirebaseStorage.getInstance().getReference().child("Menu Images");
        menuRef = FirebaseDatabase.getInstance().getReference().child("Menu");
        eaterDetailRef = FirebaseDatabase.getInstance().getReference().child("Eatery Info");
        recyclerView = findViewById(R.id.edit_menu_list);
        eaterNameTxt = findViewById(R.id.update_eatery_name);
        ownerNameTxt = findViewById(R.id.update_owner_name);
        eateryAddressTxt = findViewById(R.id.update_eatery_address);
        eateryImageView = findViewById(R.id.eatery_image);
        addMenu = findViewById(R.id.add_menu);
        reviewImg = findViewById(R.id.show_review_lyt);
        loadingBar = new ProgressDialog(this);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        getMenuItems();
        SharedPreferences sharedPreferences = getSharedPreferences("owners", MODE_PRIVATE);
        ownerNameTxt.setText(sharedPreferences.getString("ownerName", null));
        ownerId = sharedPreferences.getString("ownerId", null);

        eaterDetailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    eaterNameTxt.setText(snapshot.child("eateryName").getValue(String.class));
                    eateryAddressTxt.setText(snapshot.child("eateryAddress").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        reviewImg.setOnClickListener(view -> {
            startActivity(new Intent(UpdateMenuActivity.this, ShowReviewActivity.class));
        });

        eaterNameTxt.setOnClickListener(view -> {
            showEditEateryDialog(getString(R.string.input_eatery_name), "eateryName", eaterNameTxt.getText().toString());
        });

        eateryAddressTxt.setOnClickListener(view -> {
            showEditEateryDialog(getString(R.string.input_eatery_address), "eateryAddress", eateryAddressTxt.getText().toString());
        });

        ownerNameTxt.setOnClickListener(view -> {
            //showEditOwnerDialog("ownerName");
        });

        addMenu.setOnClickListener(view -> {
            startActivity(new Intent(UpdateMenuActivity.this, AddMenuActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        slideInLeft();
        getEateryDetails();

        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void getEateryDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("owners", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        DatabaseReference ownerRef = FirebaseDatabase.getInstance().getReference().child("Owners");
        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Owners owners = dataSnapshot.getValue(Owners.class);
                        assert owners != null;
                        if (Objects.equals(owners.getEmail(), email)) {
                            eateryImgUrl = owners.getEateryImage();
                            if (eateryImgUrl != null) {
                                if (!eateryImgUrl.isEmpty()) {
                                    Glide.with(UpdateMenuActivity.this)
                                            .load(eateryImgUrl)
                                            .placeholder(R.drawable.logo)
                                            .into(eateryImageView);
                                }
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

    private void showEditEateryDialog(String string, String hashMapObj, String text) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_eatery);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText input = dialog.findViewById(R.id.dialog_edit_text);
        ImageView save = dialog.findViewById(R.id.dialog_save_info);
        TextView textView = dialog.findViewById(R.id.dialog_text_view);
        textView.setText(string);
        input.setHint(string);
        input.setText(text);
        save.setOnClickListener(view -> {
            String name = input.getText().toString();
            if (name.isEmpty()){
                input.setError(getString(R.string.input_eatery_name));
                input.requestFocus();
            }
            else {
                loadingBar.setMessage(getString(R.string.please_wait));
                loadingBar.setCancelable(false);
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(hashMapObj, name);
                eaterDetailRef.updateChildren(hashMap)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                loadingBar.dismiss();
                                dialog.dismiss();
                                Toast.makeText(UpdateMenuActivity.this, "Eatery Info Updated", Toast.LENGTH_SHORT).show();
                                startActivity(getIntent());
                                finish();
                            }
                            else {
                                loadingBar.dismiss();
                                dialog.dismiss();
                                Toast.makeText(UpdateMenuActivity.this, "Failed to update, " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> {
                            loadingBar.dismiss();
                            dialog.dismiss();
                            Toast.makeText(UpdateMenuActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
        dialog.show();
    }

    private void getMenuItems() {
        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference().child("Menu");
        menuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                menusArrayList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Menus menus = dataSnapshot.getValue(Menus.class);
                        if (menus.getOwnerId() != null){
                            if (menus.getOwnerId().equals(ownerId)){
                                menusArrayList.add(menus);
                                menuAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        menuAdapter = new EditMenuAdapter(this, menusArrayList);
        recyclerView.setAdapter(menuAdapter);
//        FirebaseRecyclerOptions<Menus> options = new FirebaseRecyclerOptions.Builder<Menus>()
//                .setQuery(menuRef.orderByChild("id"), Menus.class).build();
//        FirebaseRecyclerAdapter<Menus, EditMenuViewHolder> adapter = new FirebaseRecyclerAdapter<Menus, EditMenuViewHolder>(options) {
//            @SuppressLint("SetTextI18n")
//            @Override
//            protected void onBindViewHolder(@NonNull EditMenuViewHolder holder, int position, @NonNull Menus model) {
//                if (model.getOwnerId().equals(ownerId)) {
//                    Glide.with(UpdateMenuActivity.this)
//                            .load(model.getIdPicUrl())
//                            .into(holder.menuImg);
//                    holder.price.setText(getString(R.string.price_php) + model.getPrice());
//                    holder.description.setText(model.getDescription());
//                    if (model.getCategory() != null && !model.getCategory().isEmpty()) {
//                        holder.category.setText(model.getCategory());
//                    }
//                    holder.title.setText(model.getTitle());
//                    holder.menuInfoLayout.setOnClickListener(view -> {
//                        if (holder.buttonLayout.getVisibility() == View.VISIBLE) {
//                            holder.buttonLayout.setVisibility(View.GONE);
//                        } else {
//                            holder.buttonLayout.setVisibility(View.VISIBLE);
//                        }
//                    });
//                    holder.editImg.setOnClickListener(view -> {
//                        showEditMenuDialog(model);
//                    });
//
//                    holder.deleteImg.setOnClickListener(view -> {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateMenuActivity.this);
//                        builder.setTitle("Delete Menu");
//                        builder.setMessage("Are you sure you want to delete this menu ?");
//                        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
//                            loadingBar.setMessage("Please wait...");
//                            loadingBar.setCancelable(false);
//                            loadingBar.setCanceledOnTouchOutside(false);
//                            loadingBar.show();
//                            menuRef.child(model.getId()).removeValue().addOnCompleteListener(task -> {
//                                if (task.isSuccessful()) {
//                                    loadingBar.dismiss();
//                                    Toast.makeText(UpdateMenuActivity.this, "Menu Deleted...", Toast.LENGTH_SHORT).show();
//                                } else {
//                                    loadingBar.dismiss();
//                                    Toast.makeText(UpdateMenuActivity.this, "Failed to delete, " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                            }).addOnFailureListener(e -> {
//                                loadingBar.dismiss();
//                                Toast.makeText(UpdateMenuActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            });
//                        });
//                        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
//                        builder.show();
//                    });
//                }
//            }
//
//            @NonNull
//            @Override
//            public EditMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(UpdateMenuActivity.this).inflate(R.layout.layout_update_menu, parent, false);
//                return new EditMenuViewHolder(view);
//            }
//        };
//        recyclerView.setAdapter(adapter);
//        adapter.startListening();
    }

    private void showEditMenuDialog(Menus model) {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_menu);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialogUpdateImg = dialog.findViewById(R.id.update_img);
        dialogInputPrice = dialog.findViewById(R.id.dialog_update_price);
        dialogInputTitle = dialog.findViewById(R.id.dialog_update_title);
        dialogInputDesc = dialog.findViewById(R.id.dialog_update_desc);
        dialogSaveMenu = dialog.findViewById(R.id.dialog_save_menu);
        categorySpinner = dialog.findViewById(R.id.category_spinner);
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item, menuCategoryName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        for (int i = 0; i < menuCategoryName.length; i++){
            if (menuCategoryName[i].equals(model.getCategory())){
                categorySpinner.setSelection(i);
            }
        }

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                categoryName = menuCategoryName[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        downloadImgUrl = model.getIdPicUrl();

        dialogInputTitle.setText(model.getTitle());
        dialogInputPrice.setText(model.getPrice());
        dialogInputDesc.setText(model.getDescription());
        Glide.with(UpdateMenuActivity.this).load(model.getIdPicUrl())
                .into(dialogUpdateImg);
        dialogUpdateImg.setOnClickListener(view -> {
            ImagePicker.with(UpdateMenuActivity.this)
                    .galleryOnly()
                    .galleryMimeTypes(new String[]{
                            "image/png",
                            "image/jpg",
                            "image/jpeg"
                    })
                    .cropSquare()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        dialogSaveMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = dialogInputTitle.getText().toString();
                String id = model.getId();
                String price = dialogInputPrice.getText().toString();
                String description = dialogInputDesc.getText().toString();
                String category = categorySpinner.getSelectedItem().toString();
                updateMenu(id, title, description, category, price, downloadImgUrl);
            }
        });

        dialog.show();
    }

    private void updateMenu(String id, String title, String description, String category, String price, String downloadImgUrl) {
        if (title.isEmpty()) {
            dialogInputTitle.setError(getString(R.string.input_title));
            dialogInputTitle.requestFocus();
        } else if (description.isEmpty()) {
            dialogInputDesc.setError(getString(R.string.input_description));
            dialogInputDesc.requestFocus();
        } else if (price.isEmpty()) {
            dialogInputPrice.setError(getString(R.string.input_price));
            dialogInputPrice.requestFocus();
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Please Select Menu Category...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setMessage("Please wait...");
            loadingBar.setCancelable(false);
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("id", id);
            hashMap.put("title", title);
            hashMap.put("ownerId", ownerId);
            hashMap.put("price", price);
            hashMap.put("description", description);
            hashMap.put("idPicUrl", downloadImgUrl);
            hashMap.put("category", category);

            menuRef.child(id).updateChildren(hashMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            dialog.dismiss();
                            Intent intent = new Intent(UpdateMenuActivity.this, ConfirmUpdateActivity.class);
                            startActivity(intent);
                        } else {
                            loadingBar.dismiss();
                            dialog.dismiss();
                            Toast.makeText(UpdateMenuActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        loadingBar.dismiss();
                        dialog.dismiss();
                        Toast.makeText(UpdateMenuActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void uploadMenuImage() {
        loadingBar.setMessage("Please wait...");
        loadingBar.setCancelable(false);
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        DatabaseReference pushedPostRef = FirebaseDatabase.getInstance().getReference();
        String menuRandomKey = pushedPostRef.push().getKey();


        final StorageReference filepath = menuImageRef.child(ImageUri.getLastPathSegment() + menuRandomKey + ".jpg");
        final UploadTask uploadTask = filepath.putFile(ImageUri);

        uploadTask.addOnFailureListener(e -> {
            String message = e.toString();
            Toast.makeText(UpdateMenuActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();

        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(UpdateMenuActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }


                downloadImgUrl = filepath.getDownloadUrl().toString();
                return filepath.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    loadingBar.dismiss();
                    downloadImgUrl = task.getResult().toString();
                    Toast.makeText(UpdateMenuActivity.this, "Got the image", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            ImageUri = data.getData();
            dialogUpdateImg.setImageURI(ImageUri);
            dialog.setCancelable(false);
            uploadMenuImage();
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }

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
                .playOn(findViewById(R.id.update_eatery_name));

        YoYo.with(Techniques.BounceIn)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1300)
                .playOn(findViewById(R.id.update_owner_name));

        YoYo.with(Techniques.BounceIn)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1500)
                .playOn(findViewById(R.id.update_eatery_address));

        YoYo.with(Techniques.BounceIn)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1700)
                .playOn(findViewById(R.id.update_menu));

        YoYo.with(Techniques.BounceIn)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1900)
                .playOn(findViewById(R.id.menu_input));

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
                startActivity(new Intent(UpdateMenuActivity.this, AddMenuActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
    };

    public class EditMenuAdapter extends RecyclerView.Adapter<EditMenuViewHolder>{
        Context context;
        ArrayList<Menus> menusArrayList;

        public EditMenuAdapter(Context context, ArrayList<Menus> menusArrayList) {
            this.context = context;
            this.menusArrayList = menusArrayList;
        }

        @NonNull
        @Override
        public EditMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_update_menu, parent, false);
            return new EditMenuViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EditMenuViewHolder holder, int position) {
            Menus model = menusArrayList.get(position);
            Glide.with(UpdateMenuActivity.this)
                    .load(model.getIdPicUrl())
                    .into(holder.menuImg);
            holder.price.setText(getString(R.string.price_php) + model.getPrice());
            holder.description.setText(model.getDescription());
            if (model.getCategory() != null && !model.getCategory().isEmpty()) {
                holder.category.setText(model.getCategory());
            }
            holder.title.setText(model.getTitle());
            holder.menuInfoLayout.setOnClickListener(view -> {
                if (holder.buttonLayout.getVisibility() == View.VISIBLE) {
                    holder.buttonLayout.setVisibility(View.GONE);
                } else {
                    holder.buttonLayout.setVisibility(View.VISIBLE);
                }
            });
            holder.editImg.setOnClickListener(view -> {
                showEditMenuDialog(model);
            });

            holder.deleteImg.setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateMenuActivity.this);
                builder.setTitle("Delete Menu");
                builder.setMessage("Are you sure you want to delete this menu ?");
                builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                    loadingBar.setMessage("Please wait...");
                    loadingBar.setCancelable(false);
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    menuRef.child(model.getId()).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(UpdateMenuActivity.this, "Menu Deleted...", Toast.LENGTH_SHORT).show();
                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(UpdateMenuActivity.this, "Failed to delete, " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        loadingBar.dismiss();
                        Toast.makeText(UpdateMenuActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                });
                builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
                builder.show();
            });
        }

        @Override
        public int getItemCount() {
            return menusArrayList.size();
        }
    }

}
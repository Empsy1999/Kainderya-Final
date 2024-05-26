package com.eatery.tarakain;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eatery.tarakain.model.Menus;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowMenuActivity extends AppCompatActivity {
    private String getBudget, getCategory, ownerId;
    private int budget;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private TextView eateryName, eateryAddress;
    private DatabaseReference eaterDetailRef;
    private TextView menuStatusTxt;
    private Query query;
    private ReviewDialog reviewDialog;
    private ArrayList<Menus> menusArrayList = new ArrayList<>();
    private MenuAdapter adapter;
    private TextView showHideMenuTxt;
    private RecyclerView allMenuRecycler;
    private ArrayList<Menus> allMenusArrayList = new ArrayList<>();
    private MenuAdapter allMenuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_menu);

        ownerId = getIntent().getStringExtra("ownerId");
        getBudget = getIntent().getStringExtra("budget");
        getCategory = getIntent().getStringExtra("category");

        budget = Integer.parseInt(getBudget);

        eaterDetailRef = FirebaseDatabase.getInstance().getReference().child("Eatery Info");
        recyclerView = findViewById(R.id.menu_list);
        eateryName = findViewById(R.id.eatery_name);
        eateryAddress = findViewById(R.id.eater_address);
        menuStatusTxt = findViewById(R.id.loading);
        showHideMenuTxt = findViewById(R.id.show_hide_menu_txt);
        allMenuRecycler = findViewById(R.id.all_menu_list);
        allMenuRecycler.setHasFixedSize(true);
        allMenuRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        reviewDialog = new ReviewDialog(this, ownerId);

        if (getCategory.equals("all")) {
            checkDataExist(getBudget, 1);
        }
        else {
            checkDataExist(getCategory);
        }
        getAllMenu();

//        if (budget <= 40){
//            getMenuItems("40.00");
//        }
//        else if (budget >=50){
//            getMenuItems("50.00");
//        }

        showHideMenuTxt.setOnClickListener(view -> {
            String filterTxt = showHideMenuTxt.getText().toString();
            if (filterTxt.equals(getString(R.string.show))){
                showHideMenuTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_up_float, 0);
                showHideMenuTxt.setText(getString(R.string.hide));
                if (allMenusArrayList.isEmpty()){
                    menuStatusTxt.setVisibility(View.VISIBLE);
                    menuStatusTxt.setText(R.string.no_data);
                }else {
                    menuStatusTxt.setVisibility(View.GONE);
                }
                allMenuRecycler.setVisibility(View.VISIBLE);
            }
            if (filterTxt.equals(getString(R.string.hide))){
                showHideMenuTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
                showHideMenuTxt.setText(getString(R.string.show));
                if (allMenusArrayList.isEmpty()){
                    menuStatusTxt.setVisibility(View.VISIBLE);
                    menuStatusTxt.setText(R.string.no_data);
                }else {
                    if (menusArrayList.isEmpty()){
                        menuStatusTxt.setVisibility(View.VISIBLE);
                        menuStatusTxt.setText(R.string.no_data);
                    }else {
                        menuStatusTxt.setVisibility(View.GONE);
                    }
                }
                allMenuRecycler.setVisibility(View.GONE);
            }
        });

        eaterDetailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    eateryName.setText(snapshot.child("eateryName").getValue(String.class));
                    eateryAddress.setText(snapshot.child("eateryAddress").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void checkDataExist(String category) {
        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference().child("Menu");
        menuRef.orderByChild("category").startAt(getCategory).endAt(getCategory)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            menuStatusTxt.setVisibility(View.GONE);
                            getMenuItems(getCategory);
                        }
                        else {
                            checkDataExist(getBudget, 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ShowMenuActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkDataExist(String price, int value) {
        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference().child("Menu");
        menuRef.orderByChild("price").startAt(price).endAt(price)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("==data", value + " : "+ snapshot.exists());
                        if (snapshot.exists()) {
                            menuStatusTxt.setVisibility(View.GONE);
                            getMenuItems(price);
                        } else if (value == 2) {
                            menuStatusTxt.setVisibility(View.VISIBLE);
                            menuStatusTxt.setText(R.string.no_data);
                        } else {
                            checkDataExist(getBudget + ".00", 2);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ShowMenuActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getMenuItems(String value) {
        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference().child("Menu");
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Menus menus = dataSnapshot.getValue(Menus.class);
                        assert menus != null;
                        if (menus.getOwnerId() != null) {
                            if (menus.getOwnerId().equals(ownerId)) {
                                if (getCategory.equals("all")) {
                                    String price = menus.getPrice();
                                    if (price.equals(value)){
                                        menusArrayList.add(menus);
                                        adapter.notifyDataSetChanged();
                                    }
                                    query = menuRef.child(menus.getId()).orderByChild("price").startAt(value).endAt(value);
                                } else {
                                    String category = menus.getCategory();
                                    if (category.equalsIgnoreCase(value)){
                                        menusArrayList.add(menus);
                                        adapter.notifyDataSetChanged();
                                    }
                                    query = menuRef.child(menus.getId()).orderByChild("category").startAt(value).endAt(value);
                                }
//                                FirebaseRecyclerOptions<Menus> options = new FirebaseRecyclerOptions.Builder<Menus>()
//                                        .setQuery(query, Menus.class).build();
//                                FirebaseRecyclerAdapter<Menus, MenuViewHolder> adapter = new FirebaseRecyclerAdapter<Menus, MenuViewHolder>(options) {
//                                    @SuppressLint("SetTextI18n")
//                                    @Override
//                                    protected void onBindViewHolder(@NonNull MenuViewHolder holder, int position, @NonNull Menus model) {
//                                        Glide.with(ShowMenuActivity.this)
//                                                .load(model.getIdPicUrl())
//                                                .into(holder.menuImgTop);
//                                        Glide.with(ShowMenuActivity.this)
//                                                .load(model.getIdPicUrl())
//                                                .into(holder.menuImgBottom);
//                                        holder.menuPrice.setText(getString(R.string.price_php) + model.getPrice());
//                                        holder.menuDesc.setText(model.getDescription());
//                                        holder.menuNoTxt.setText(getString(R.string.menu) + " " + (position + 1));
//                                        if (position <= 2 && position % 2 == 1) {
//                                            holder.menuImgTop.setVisibility(View.GONE);
//                                            holder.menuImgBottom.setVisibility(View.VISIBLE);
//                                        }
//
//                                        YoYo.with(Techniques.Pulse)
//                                                .repeat(-1)
//                                                .repeatMode(ValueAnimator.REVERSE)
//                                                .duration(8000)
//                                                .playOn(holder.menuImgTop);
//
//                                        YoYo.with(Techniques.Pulse)
//                                                .repeat(-1)
//                                                .repeatMode(ValueAnimator.REVERSE)
//                                                .duration(8000)
//                                                .playOn(holder.menuImgBottom);
//
//                                        if (position > 2) {
//                                            holder.menuImgTop.setVisibility(View.VISIBLE);
//                                            holder.menuImgBottom.setVisibility(View.GONE);
//                                            if (position % 2 == 0) {
//                                                holder.menuImgTop.setVisibility(View.GONE);
//                                                holder.menuImgBottom.setVisibility(View.VISIBLE);
//                                            }
//                                        }
//                                    }
//
//                                    @NonNull
//                                    @Override
//                                    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                                        View view = LayoutInflater.from(ShowMenuActivity.this).inflate(R.layout.layout_menu_items, parent, false);
//                                        return new MenuViewHolder(view);
//                                    }
//                                };
//                                recyclerView.setAdapter(adapter);
//                                adapter.startListening();
                            }
                        }
                    }
                    Log.d("==arr", menusArrayList.size() + "");
                    if (menusArrayList.isEmpty()){
                        menuStatusTxt.setVisibility(View.VISIBLE);
                        menuStatusTxt.setText(R.string.no_data);
                    }
                }else {
                    menuStatusTxt.setVisibility(View.VISIBLE);
                    menuStatusTxt.setText(R.string.no_data);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShowMenuActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                menuStatusTxt.setText(R.string.no_data);
                menuStatusTxt.setVisibility(View.VISIBLE);
            }
        });

        adapter = new MenuAdapter(this, menusArrayList);
        recyclerView.setAdapter(adapter);

    }

    private void getAllMenu(){
        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference().child("Menu");
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Menus menus = dataSnapshot.getValue(Menus.class);
                        assert menus != null;
                        if (menus.getOwnerId() != null) {
                            if (menus.getOwnerId().equals(ownerId)) {
                                allMenusArrayList.add(menus);
                                allMenuAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }else {
                    menuStatusTxt.setVisibility(View.VISIBLE);
                    menuStatusTxt.setText(R.string.no_data);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShowMenuActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                menuStatusTxt.setText(R.string.no_data);
                menuStatusTxt.setVisibility(View.VISIBLE);
            }
        });
        allMenuAdapter = new MenuAdapter(this, allMenusArrayList);
        allMenuRecycler.setAdapter(allMenuAdapter);
    }

    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            reviewDialog.showReviewDialog();
        }
    };

}
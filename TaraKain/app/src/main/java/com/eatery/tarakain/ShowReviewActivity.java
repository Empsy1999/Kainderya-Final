package com.eatery.tarakain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eatery.tarakain.model.Reviews;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowReviewActivity extends AppCompatActivity {
    private String ownerId;
    private ImageView backImg, bottomImg;
    private RecyclerView recyclerView;
    private ArrayList<Reviews> reviewsArrayList = new ArrayList<>();
    private ReviewsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_review);

        SharedPreferences sharedPreferences = getSharedPreferences("owners", MODE_PRIVATE);
        ownerId = sharedPreferences.getString("ownerId", null);

        backImg = findViewById(R.id.back_img);
        bottomImg = findViewById(R.id.bottom_img);
        recyclerView = findViewById(R.id.reviews_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));

        getCustomerReviews();
        slideInLeft();

        backImg.setOnClickListener(view -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        YoYo.with(Techniques.BounceIn)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1000)
                .playOn(backImg);
        YoYo.with(Techniques.BounceIn)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1000)
                .playOn(findViewById(R.id.customer_reviews_btn));
    }

    private void getCustomerReviews() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Reviews");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewsArrayList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot :  snapshot.getChildren()) {
                        Reviews reviews = dataSnapshot.getValue(Reviews.class);
                        assert reviews != null;
                        if (reviews.getOwnerId() != null) {
                            if (reviews.getOwnerId().equals(ownerId)) {
                                reviewsArrayList.add(reviews);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }else {
                    Toast.makeText(ShowReviewActivity.this, "No Reviews", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShowReviewActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        adapter = new ReviewsAdapter(this, reviewsArrayList, 1);
        recyclerView.setAdapter(adapter);
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

}
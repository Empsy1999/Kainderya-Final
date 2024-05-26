package com.eatery.tarakain;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eatery.tarakain.model.Reviews;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
    Context context;
    ArrayList<Reviews> reviewsArrayList;
    int type;

    public ReviewsAdapter(Context context, ArrayList<Reviews> reviewsArrayList, int type) {
        this.context = context;
        this.reviewsArrayList = reviewsArrayList;
        this.type = type;
    }

    @NonNull
    @Override
    public ReviewsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_reviews, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ReviewsAdapter.ViewHolder holder, int position) {
        Reviews reviews = reviewsArrayList.get(position);
        holder.nameTxt.setText(reviews.getCustomerName());
        holder.reviewTime.setText(reviews.getTime());
        holder.ratingTxt.setText(reviews.getRating() +"");
        holder.customerReview.setText(reviews.getReview());
        holder.ratingBar.setRating(reviews.getRating());
        holder.ratingBar.setIsIndicator(true);
        if (type == 1){
            holder.deleteReviewImg.setVisibility(View.VISIBLE);
        } else if (type == 2) {
            holder.deleteReviewImg.setVisibility(View.GONE);
        }

        holder.deleteReviewImg.setOnClickListener(view -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Reviews");
            reference.child(reviews.getId()).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(context, "Review Deleted", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(context, "Please try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }

    @Override
    public int getItemCount() {
        return reviewsArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTxt, reviewTime, ratingTxt, customerReview;
        RatingBar ratingBar;
        ImageView deleteReviewImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTxt = itemView.findViewById(R.id.name_txt);
            reviewTime = itemView.findViewById(R.id.review_time);
            ratingTxt = itemView.findViewById(R.id.rating_txt);
            customerReview = itemView.findViewById(R.id.customer_review);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            deleteReviewImg = itemView.findViewById(R.id.delete_review_img);
        }
    }
}

package com.eatery.tarakain;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ReviewDialog {
    private Activity activity;
    private float rating = 0;
    private String review, ownerId;

    public ReviewDialog(Activity activity, String ownerId) {
        this.activity = activity;
        this.ownerId = ownerId;
    }

    public void showReviewDialog(){
        LayoutInflater inflater = activity.getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_review, null);

        ImageView close = view.findViewById(R.id.close_btn);
        ImageView submit = view.findViewById(R.id.submit_btn);
        EditText inputReview = view.findViewById(R.id.input_review);
        RatingBar ratingBar = view.findViewById(R.id.rating_bar);


        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setView(view)
                .show();
        alertDialog.getWindow().
                setBackgroundDrawable(new ColorDrawable(android.graphics.Color
                        .parseColor("#00000000")));

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        close.setOnClickListener(view1 -> {
            if (alertDialog.isShowing()){
                alertDialog.dismiss();
                activity.startActivity(new Intent(activity, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                rating = v;
                Log.d("==rating", v +"");
            }
        });

        submit.setOnClickListener(view1 -> {
            if (rating == 0){
                Toast.makeText(activity, "Please add Ratings...", Toast.LENGTH_SHORT).show();
            }
            else if (inputReview.getText().toString().isEmpty()){
                inputReview.setError("Write your review...");
                inputReview.requestFocus();
            }
            else {
                review = inputReview.getText().toString();
                saveUserReview(alertDialog, rating, review);
            }
        });

    }

    private void saveUserReview(AlertDialog alertDialog, float rating, String review) {
        String currentDate = new SimpleDateFormat("yyyy, dd-MM", Locale.getDefault()).format(new Date());
        String currentDateTime = new SimpleDateFormat("yyyy, dd-MM HH:mm a", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm a", Locale.getDefault()).format(new Date());
        SharedPreferences preferences = activity.getSharedPreferences("Customer", activity.MODE_PRIVATE);
        String customerName = preferences.getString("name", null);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Reviews");
        String id = databaseReference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", id);
        hashMap.put("rating", rating);
        hashMap.put("review", review);
        hashMap.put("customerName", customerName);
        hashMap.put("ownerId", ownerId);
        hashMap.put("time", currentTime);
        hashMap.put("date", currentDate);
        hashMap.put("dateTime", currentDateTime);
        assert id != null;
        databaseReference.child(id).updateChildren(hashMap);
        if (alertDialog.isShowing()){
            alertDialog.dismiss();
            activity.startActivity(new Intent(activity, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
    }

}

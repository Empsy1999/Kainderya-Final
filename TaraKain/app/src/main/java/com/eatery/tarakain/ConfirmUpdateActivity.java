package com.eatery.tarakain;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class ConfirmUpdateActivity extends AppCompatActivity {
    private TextView menuTxt, successTxt, uploadTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_update);

        menuTxt = findViewById(R.id.menu_txt);
        successTxt = findViewById(R.id.success_txt);
        uploadTxt = findViewById(R.id.uploaded_txt);

        new Handler().postDelayed(() -> getOnBackPressedDispatcher().onBackPressed(),2000);

    }

    @Override
    protected void onResume() {
        super.onResume();

        YoYo.with(Techniques.SlideInUp)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1000)
                .playOn(findViewById(R.id.success_bg));

        YoYo.with(Techniques.SlideInUp)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1300)
                .playOn(menuTxt);

        YoYo.with(Techniques.SlideInUp)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1600)
                .playOn(successTxt);

        YoYo.with(Techniques.SlideInUp)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1900)
                .playOn(uploadTxt);
    }
}
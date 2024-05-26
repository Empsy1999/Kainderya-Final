package com.eatery.tarakain;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eatery.tarakain.notification.NotificationHelper;

public class MainActivity extends AppCompatActivity {
    private ImageView loginImg, triviaImg, customerImg;
    private TextView triviaTxt, customerTxt, loginTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.newVolleyRequestQueue(this);

        loginImg = findViewById(R.id.login_img);
        triviaImg = findViewById(R.id.trivia_img);
        customerImg = findViewById(R.id.customer_img);
        triviaTxt = findViewById(R.id.trivia_txt);
        customerTxt = findViewById(R.id.customer_txt);
        loginTxt = findViewById(R.id.login_txt);

        triviaImg.setOnClickListener(view -> {
            startActivity(new Intent(
                    MainActivity.this,
                    TriviaActivity.class
            ));
        });

        loginImg.setOnClickListener(view -> {
            startActivity(new Intent(
                    MainActivity.this,
                    LoginActivity.class
            ));
        });

        customerImg.setOnClickListener(view -> {
            startActivity(new Intent(
                    MainActivity.this,
                    CustomerActivity.class
            ));
        });

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

    @Override
    protected void onResume() {
        super.onResume();
        YoYo.with(Techniques.SlideInUp)
                .duration(1200)
                .repeatMode(ValueAnimator.REVERSE)
                .playOn(loginImg);

        YoYo.with(Techniques.SlideInUp)
                .duration(1100)
                .repeatMode(ValueAnimator.REVERSE)
                .playOn(customerImg);

        YoYo.with(Techniques.SlideInUp)
                .duration(900)
                .repeatMode(ValueAnimator.REVERSE)
                .playOn(triviaImg);

        YoYo.with(Techniques.SlideInUp)
                .duration(900)
                .repeatMode(ValueAnimator.REVERSE)
                .playOn(triviaTxt);

        YoYo.with(Techniques.SlideInUp)
                .duration(900)
                .repeatMode(ValueAnimator.REVERSE)
                .playOn(customerTxt);

        YoYo.with(Techniques.SlideInUp)
                .duration(900)
                .repeatMode(ValueAnimator.REVERSE)
                .playOn(loginTxt);

        YoYo.with(Techniques.Pulse)
                .repeat(-1)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(5000)
                .playOn(findViewById(R.id.logo));
    }
}
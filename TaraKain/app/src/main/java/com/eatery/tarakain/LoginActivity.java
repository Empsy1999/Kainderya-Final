package com.eatery.tarakain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.eatery.tarakain.model.Owners;
import com.eatery.tarakain.notification.Token;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEdtTxt, passwordEdtTxt;
    private String email, password;
    private LinearLayout registerBtn;
    private AppCompatButton loginBtn;
    private DatabaseReference ownerRef;
    private ProgressDialog loadingBar;
    private boolean isExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();

        ownerRef = FirebaseDatabase.getInstance().getReference().child("Owners");
        loadingBar = new ProgressDialog(this);

        loginBtn.setOnClickListener(view -> {
            validateLoginData();
        });

        registerBtn.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

    }

    private void validateLoginData() {
        email = emailEdtTxt.getText().toString();
        password = passwordEdtTxt.getText().toString();

        if (email.isEmpty() || !email.contains("@")){
            emailEdtTxt.setError(getString(R.string.input_email_address));
            emailEdtTxt.requestFocus();
        }
        else if (password.isEmpty()){
            passwordEdtTxt.setError(getString(R.string.input_password));
            passwordEdtTxt.requestFocus();
        }
        else if (password.length() < 6){
            passwordEdtTxt.setError(getString(R.string.input_valid_password));
            passwordEdtTxt.requestFocus();
        }
        else {
            loadingBar.setMessage("Please wait...");
            loadingBar.setCancelable(false);
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            loginOwner();
        }
    }

    private void loginOwner() {
        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Owners owners = dataSnapshot.getValue(Owners.class);
                        assert owners != null;
                        if (Objects.equals(owners.getEmail(), email)){
                            if (Objects.equals(owners.getPassword(), password)){
                                if (Objects.equals(owners.getStatus(), getString(R.string.verified))) {
                                    isExists = true;
                                    SharedPreferences sharedPreferences = getSharedPreferences("owners", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("ownerName", owners.getOwnerName());
                                    editor.putString("eateryName", owners.getEateryName());
                                    editor.putString("email", email);
                                    editor.putString("ownerId", owners.getId());
                                    editor.apply();
                                    updateToken(owners.getId());
                                    loadingBar.dismiss();
                                    Toast.makeText(LoginActivity.this, "Logged in Successfully...", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, AddMenuActivity.class));
                                }else {
                                    loadingBar.dismiss();
                                    Toast.makeText(LoginActivity.this, "Account not verified.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                loadingBar.dismiss();
                                Toast.makeText(LoginActivity.this, "Password Incorrect. Please try again...", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                    }
                    if (!isExists){
                        loadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, "Account with this " + email + " does not exists...", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                        emailEdtTxt.setText("");
                        passwordEdtTxt.setText("");
                    }
                }
                else {
                    loadingBar.dismiss();
                    Toast.makeText(LoginActivity.this, "Please Register Account first...", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void initView() {
        registerBtn = findViewById(R.id.register_btn_main);
        emailEdtTxt = findViewById(R.id.input_email);
        passwordEdtTxt = findViewById(R.id.input_password);
        loginBtn = findViewById(R.id.login_btn);
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
                .playOn(findViewById(R.id.login_img));

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1400)
                .playOn(findViewById(R.id.login_txt));

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1200)
                .playOn(findViewById(R.id.register_img));

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1400)
                .playOn(findViewById(R.id.register_txt));

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1600)
                .playOn(emailEdtTxt);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1800)
                .playOn(passwordEdtTxt);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(2000)
                .playOn(loginBtn);
    }
}
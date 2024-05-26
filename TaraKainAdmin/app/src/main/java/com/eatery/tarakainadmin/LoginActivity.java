package com.eatery.tarakainadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.eatery.tarakainadmin.notification.Token;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText inputUsername, inputPassword;
    private String userName, password;
    private LinearLayout registerBtn, loginBtn;
    private ProgressDialog progressDialog;
    private boolean isExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkAdminLogin();
        initViews();
        setClickListener();

    }

    private void setClickListener() {
        registerBtn.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
        loginBtn.setOnClickListener(view -> {
            userName = inputUsername.getText().toString();
            password = inputPassword.getText().toString();
            if (userName.isEmpty()){
                inputUsername.setError(getString(R.string.username));
                inputUsername.requestFocus();
            }else if (password.isEmpty()){
                inputPassword.setError(getString(R.string.password));
                inputPassword.requestFocus();
            }else {
                showProgressDialog();
                loginAdmin();
            }
        });
    }

    private void checkAdminLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("admin", MODE_PRIVATE);
        boolean isLogin = sharedPreferences.getBoolean("isLogin", false);
        String id = sharedPreferences.getString("id", null);
        if (isLogin){
            if (id != null) {
                updateToken(id);
            }
            startActivity(new Intent(LoginActivity.this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }

    }

    private void loginAdmin() {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference().child("Admin");
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String id = dataSnapshot.child("id").getValue(String.class);
                        String getUserName = dataSnapshot.child("userName").getValue(String.class);
                        String checkPassword = dataSnapshot.child("password").getValue(String.class);
                        if (Objects.equals(getUserName, userName)){
                            if (Objects.equals(checkPassword, password)){
                                isExists = true;
                                SharedPreferences sharedPreferences = getSharedPreferences("admin", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isLogin", true);
                                editor.putString("id", id);
                                editor.apply();
                                hideProgressDialog();
                                updateToken(id);
                                Toast.makeText(LoginActivity.this, "Logged in Successfully...", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            }
                            else {
                                hideProgressDialog();
                                Toast.makeText(LoginActivity.this, "Password Incorrect. Please try again...", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                    }
                    if (!isExists){
                        hideProgressDialog();
                        Toast.makeText(LoginActivity.this, "Account with this " + userName + " does not exists...", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                        inputUsername.setText("");
                        inputPassword.setText("");
                    }
                }
                else {
                    hideProgressDialog();
                    Toast.makeText(LoginActivity.this, "Please Register Account first...", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
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

    private void initViews() {
        inputUsername = findViewById(R.id.input_username);
        inputPassword = findViewById(R.id.input_password);
        registerBtn = findViewById(R.id.register_btn);
        loginBtn = findViewById(R.id.login_btn);
    }

    private void showProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog(){
        progressDialog.dismiss();
    }


}
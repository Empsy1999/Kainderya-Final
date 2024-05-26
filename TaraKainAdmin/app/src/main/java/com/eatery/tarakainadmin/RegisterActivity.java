package com.eatery.tarakainadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private EditText inputUserName, inputEmail, inputPassword, inputConfirmPassword;
    private String id, userName, email, password, confirmPassword;
    private LinearLayout registerBtn;
    private ProgressDialog progressDialog;
    private boolean isExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setClickListener();

    }

    private void setClickListener() {
        registerBtn.setOnClickListener(view -> {
            userName = inputUserName.getText().toString();
            email = inputEmail.getText().toString();
            password = inputPassword.getText().toString();
            confirmPassword = inputConfirmPassword.getText().toString();
            if (userName.isEmpty()) {
                inputUserName.setError(getString(R.string.username));
                inputUserName.requestFocus();
            } else if (email.isEmpty() || !email.contains("@")) {
                inputEmail.setError(getString(R.string.email));
                inputEmail.requestFocus();
            } else if (password.isEmpty()) {
                inputPassword.setError(getString(R.string.password));
                inputPassword.requestFocus();
            } else if (confirmPassword.isEmpty() || !confirmPassword.equals(password)) {
                inputPassword.setError(getString(R.string.password_mismatch));
                inputPassword.requestFocus();
            } else {
                showProgressDialog();
                createAdminAccount();
            }
        });
    }


    private void createAdminAccount() {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference().child("Admin");
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isExists = false;
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String checkEmail = dataSnapshot.child("email").getValue(String.class);
                        String checkUserName = dataSnapshot.child("userName").getValue(String.class);
                        if (Objects.equals(checkUserName, userName)) {
                            isExists = true;
                            hideProgressDialog();
                            Toast.makeText(RegisterActivity.this, "Account with this username Already registered...", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        } else if (Objects.equals(checkEmail, email)) {
                            isExists = true;
                            hideProgressDialog();
                            Toast.makeText(RegisterActivity.this, "Account with this Email Already registered...", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        }
                    }
                    if (!isExists) {
                        saveAdminData(adminRef);
                    }
                } else {
                    saveAdminData(adminRef);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
                Toast.makeText(RegisterActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAdminData(DatabaseReference adminRef) {
        id = adminRef.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", id);
        hashMap.put("email", email);
        hashMap.put("userName", userName);
        hashMap.put("password", password);
        adminRef.child(id).updateChildren(hashMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        hideProgressDialog();
                        rememberAdmin();
                        Toast.makeText(this, "Registered Successfully.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    } else {
                        hideProgressDialog();
                        Toast.makeText(RegisterActivity.this, "Error: Please Try Again...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    hideProgressDialog();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void rememberAdmin() {
        SharedPreferences sharedPreferences = getSharedPreferences("admin", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLogin", true);
        editor.apply();
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


    private void initViews() {
        inputUserName = findViewById(R.id.input_username);
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        inputConfirmPassword = findViewById(R.id.input_confirm_password);
        registerBtn = findViewById(R.id.register_btn);
    }

}
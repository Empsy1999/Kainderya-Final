package com.eatery.tarakain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class CustomerActivity extends AppCompatActivity {
    private EditText inputBudget;
    private AppCompatButton continueBtn;
    private SwitchCompat filterSwitch;
    private Spinner categorySpinner;
    private String categoryName, customerName;
    private String[] menuCategoryName ={"Meat","Vegetable","Seafood","Merienda","Desert"};
    private LinearLayout customerNameLyt;
    private TextView enterNameTxt;
    private EditText inputCustomerName;
    private ImageView saveCustomerNameBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        inputBudget = findViewById(R.id.input_budget);
        continueBtn = findViewById(R.id.continue_btn);
        categorySpinner = findViewById(R.id.category_filter);
        filterSwitch = findViewById(R.id.filter_switch);
        customerNameLyt = findViewById(R.id.customer_name_lyt);
        enterNameTxt = findViewById(R.id.enter_name_txt);
        inputCustomerName = findViewById(R.id.input_customer_name);
        saveCustomerNameBtn = findViewById(R.id.save_customer_name);

        SharedPreferences preferences = getSharedPreferences("Customer", MODE_PRIVATE);
        customerName = preferences.getString("name", null);

        saveCustomerNameBtn.setOnClickListener(view -> {
            customerName = inputCustomerName.getText().toString();
            if (customerName.isEmpty()){
                inputCustomerName.setError("Input Your Name");
                inputCustomerName.requestFocus();
            }else {
                saveCustomerInfo();
                inputCustomerName.setText("");
                YoYo.with(Techniques.SlideOutRight)
                        .repeatMode(ValueAnimator.REVERSE)
                        .duration(2000)
                        .withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(@NonNull Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(@NonNull Animator animator) {
                                customerNameLyt.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(@NonNull Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(@NonNull Animator animator) {

                            }
                        })
                        .playOn(customerNameLyt);
            }
        });

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item, menuCategoryName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                categoryName = menuCategoryName[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (filterSwitch.isChecked()){
            categorySpinner.setVisibility(View.GONE);
            filterSwitch.setText(getString(R.string.all));
        }
        else {
            categorySpinner.setVisibility(View.VISIBLE);
            filterSwitch.setText(getString(R.string.filter));
        }
        filterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    categorySpinner.setVisibility(View.GONE);
                    filterSwitch.setText(getString(R.string.all));
                }
                else {
                    categorySpinner.setVisibility(View.VISIBLE);
                    filterSwitch.setText(getString(R.string.filter));
                }
            }
        });

        continueBtn.setOnClickListener(view -> {
            if (inputBudget.getText().toString().isEmpty()){
                inputBudget.setError(getString(R.string.input_budget));
                inputBudget.requestFocus();
            }
            else {
                if (filterSwitch.isChecked()) {
                    startActivity(new Intent(CustomerActivity.this, ShowMapActivity.class)
                            .putExtra("budget", inputBudget.getText().toString())
                            .putExtra("category", "all"));
                }
                else {
                    startActivity(new Intent(CustomerActivity.this, ShowMapActivity.class)
                            .putExtra("budget", inputBudget.getText().toString())
                            .putExtra("category", categoryName));
                }
            }
        });

        //slideInLeft();

    }

    private void saveCustomerInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("Customer", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", customerName);
        editor.apply();
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
                .duration(900)
                .playOn(findViewById(R.id.customer_img));

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1000)
                .playOn(findViewById(R.id.customer_txt));

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1100)
                .playOn(inputBudget);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1100)
                .playOn(filterSwitch);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1100)
                .playOn(categorySpinner);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1200)
                .playOn(continueBtn);

        YoYo.with(Techniques.SlideInLeft)
                .repeatMode(ValueAnimator.REVERSE)
                .duration(1200)
                .playOn(customerNameLyt);
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
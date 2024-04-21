package com.example.ambulanceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DriverLoginActivity extends AppCompatActivity {

    private TextInputEditText etDriverFullName, etDriverMobile, etEmailAddress, PasswordEt;
    private TextView TextLogin;
    private Button btnDriverContinue;
    private TextInputLayout passwordInputLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference driversRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login3);
        FirebaseApp.initializeApp(this);


        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        driversRef = database.getReference("drivers");

        etEmailAddress = findViewById(R.id.etEmailAddress);
        etDriverMobile = findViewById(R.id.etDriverMobile);
        etDriverFullName = findViewById(R.id.etDriverFullName);
        PasswordEt = findViewById(R.id.PasswordEt);
        btnDriverContinue = findViewById(R.id.btnDriverContinue);
        TextLogin = findViewById(R.id.TxtLogin);
        passwordInputLayout = findViewById(R.id.PasswordIn);
      PasswordEt.addTextChangedListener(new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {

          }

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
           String passwordInput = s.toString();
           if(passwordInput.length() >=10){
               Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
               Matcher matcher = pattern.matcher(passwordInput);
               boolean passwordsMatch = matcher.find();
               if(passwordsMatch){
                   passwordInputLayout.setHelperText("Your Password Are Strong");
                   passwordInputLayout.setError("");
               }else {
                   passwordInputLayout.setHelperText("mix of letters(upper and lower), number and sybols");
               }
           }else {
               passwordInputLayout.setHelperText("password must 10 characters long");
               passwordInputLayout.setError("");
           }
          }

          @Override
          public void afterTextChanged(Editable s) {

          }
      });



        TextLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToNextActivity();
            }
        });
        btnDriverContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();

            }
        });
    }

    private void registerUser() {
        String email = etEmailAddress.getText().toString().trim();
        String mobile = etDriverMobile.getText().toString().trim();
        String password = PasswordEt.getText().toString().trim();
        final String fullName = etDriverFullName.getText().toString().trim();

        // Check if any of the fields are empty
        if (TextUtils.isEmpty(email)) {
            etEmailAddress.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            PasswordEt.setError("Password is required");
            return;
        }
        if (TextUtils.isEmpty(mobile)) {
            etDriverMobile.setError("Mobile Number is required");
            return;
        }

        if (TextUtils.isEmpty(fullName)) {
            etDriverFullName.setError("Name is required");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        String userId = mAuth.getCurrentUser().getUid();
                        storeUserDataInDatabase(userId, email, fullName, mobile);

                        Toast.makeText(DriverLoginActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        navigateToNextActivity();

                        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("isVerifiedDriver", true);
                        editor.apply();
                    }
                });
    }
    private void storeUserDataInDatabase(String userId, String email, String fullName, String mobile) {

        Driver2 driver = new Driver2(fullName, email, mobile,0.0,0.0,false,"");
        driversRef.child(userId).setValue(driver);
    }


    private void navigateToNextActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivityDriver2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}

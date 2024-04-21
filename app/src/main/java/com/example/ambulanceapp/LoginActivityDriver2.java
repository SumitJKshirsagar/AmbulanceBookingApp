package com.example.ambulanceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivityDriver2 extends AppCompatActivity {

    private TextInputEditText etEmailAddress, PasswordEt;
    private TextView txtRegister;
    private Button btnDriverLogin;
    private TextInputLayout passwordInputLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_driver2);

        mAuth = FirebaseAuth.getInstance();

        etEmailAddress = findViewById(R.id.etEmailAddress2);
        PasswordEt = findViewById(R.id.PasswordEt2);
        btnDriverLogin = findViewById(R.id.btnDriverLogin);
        txtRegister = findViewById(R.id.txtRegister);
        passwordInputLayout = findViewById(R.id.PasswordT2);
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


        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToNextRegisterActivity();
            }
        });
        btnDriverLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String email = etEmailAddress.getText().toString().trim();
        String password = PasswordEt.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmailAddress.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            PasswordEt.setError("Password is required");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivityDriver2.this, "Authentication successful.",
                                Toast.LENGTH_SHORT).show();

                        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("isVerifiedDriver", true);
                        editor.apply();

                        navigateToNextActivity();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivityDriver2.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToNextActivity() {
        Intent intent = new Intent(getApplicationContext(), DriverMapsActivity.class); // Change MainActivity to your desired activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void navigateToNextRegisterActivity() {
        Intent intent = new Intent(getApplicationContext(), DriverLoginActivity.class); // Change MainActivity to your desired activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

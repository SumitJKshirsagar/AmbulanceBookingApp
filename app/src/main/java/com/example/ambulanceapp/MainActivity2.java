package com.example.ambulanceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class MainActivity2 extends AppCompatActivity {
    EditText inputnumber1,inputnumber2,inputnumber3,inputnumber4,inputnumber5,inputnumber6;
    String getotpbackend;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);



        inputnumber1 = findViewById(R.id.inputotp1);
        inputnumber2 = findViewById(R.id.inputotp2);
        inputnumber3 = findViewById(R.id.inputotp3);
        inputnumber4 = findViewById(R.id.inputotp4);
        inputnumber5 = findViewById(R.id.inputotp5);
        inputnumber6 = findViewById(R.id.inputotp6);
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("users");


        TextView textView = findViewById(R.id.textmobileshownumber);
        textView.setText(String.format(
                "+91-%s", getIntent().getStringExtra("mobile")
        ));
        getotpbackend = getIntent().getStringExtra("backendotp");

       final ProgressBar progressBarverificationotp = findViewById(R.id.progressbar_verify_otp);



        final Button verfybuttonclick = findViewById(R.id.verifybutton);
        if (verfybuttonclick != null) {
        verfybuttonclick.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!inputnumber1.getText().toString().trim().isEmpty() && !inputnumber2.getText().toString().trim().isEmpty() && !inputnumber3.getText().toString().trim().isEmpty() && !inputnumber4.getText().toString().trim().isEmpty() && !inputnumber5.getText().toString().trim().isEmpty()&&!inputnumber6.getText().toString().trim().isEmpty() ) {
                 String entercodeotp = inputnumber1.getText().toString()+
                         inputnumber2.getText().toString()+
                         inputnumber3.getText().toString()+
                         inputnumber4.getText().toString()+
                         inputnumber5.getText().toString()+
                         inputnumber6.getText().toString();

                 if (getotpbackend!=null){
                     progressBarverificationotp.setVisibility(View.VISIBLE);
                     verfybuttonclick.setVisibility(View.INVISIBLE);
                     PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                             getotpbackend,entercodeotp
                     );

                     FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                             .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                 @Override
                                 public void onComplete(@NonNull Task<AuthResult> task) {
                                     progressBarverificationotp.setVisibility(View.VISIBLE);
                                     verfybuttonclick.setVisibility(View.INVISIBLE);

                                     if (task.isSuccessful()){
                                         String phoneNumber = getIntent().getStringExtra("mobile");
                                         String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                         User user = new User("","",phoneNumber,0.0,0.0);
                                         reference.child(userId).setValue(user);


                                         SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                         SharedPreferences.Editor editor = preferences.edit();
                                         editor.putBoolean("isVerified", true);
                                         editor.apply();

                                         Intent intent =new Intent(getApplicationContext(),UserLogin.class);
                                         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                         startActivity(intent);
                                         finish();



                                     }else {
                                         Toast.makeText(MainActivity2.this,"Enter the correct otp",Toast.LENGTH_SHORT).show();
                                     }
                                 }
                             });


                 }else{
                     Toast.makeText(MainActivity2.this,"please check your internet connection",Toast.LENGTH_SHORT).show();
                 }


                } else {
                    Toast.makeText(MainActivity2.this, "Please enter all numbers", Toast.LENGTH_SHORT).show();
                }

            }
        });

            numberotpmove();

           TextView resendlabel = findViewById(R.id.textresendotp);

           resendlabel.setOnClickListener(new View.OnClickListener(){
               @Override
               public void onClick(View v){
                   PhoneAuthProvider.getInstance().verifyPhoneNumber(
                           "+91" + getIntent().getStringExtra("mobile"),
                           60,
                           TimeUnit.SECONDS,
                           MainActivity2.this,
                           new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                               @Override
                               public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                               }

                               @Override
                               public void onVerificationFailed(@NonNull FirebaseException e) {

                                   Toast.makeText(MainActivity2.this,e.getMessage(), Toast.LENGTH_SHORT).show();

                               }

                               @Override
                               public void onCodeSent(@NonNull String newbackendotp, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                                getotpbackend = newbackendotp;

                                   Toast.makeText(MainActivity2.this,"OTP sended successfully", Toast.LENGTH_SHORT).show();
                               }
                           }
                   );

               }
           });
        }


    }

    private void numberotpmove() {

        inputnumber1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after){
                if (!s.toString().trim().isEmpty()){
                    inputnumber2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inputnumber2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after){
                if (!s.toString().trim().isEmpty()){
                    inputnumber3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputnumber3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after){
                if (!s.toString().trim().isEmpty()){
                    inputnumber4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputnumber4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after){
                if (!s.toString().trim().isEmpty()){
                    inputnumber5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputnumber5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after){
                if (!s.toString().trim().isEmpty()){
                    inputnumber6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
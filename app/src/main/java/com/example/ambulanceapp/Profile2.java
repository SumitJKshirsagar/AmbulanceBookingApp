package com.example.ambulanceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile2 extends AppCompatActivity {

    ImageView arrow2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile2);
        arrow2 = findViewById(R.id.arrow2);
        arrow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Get a reference to the Firebase Realtime Database
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("drivers");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Now you can use this userId to fetch user details from the database


          // Query the database to fetch user details
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Check if the dataSnapshot exists
                    if (dataSnapshot.exists()) {
                        // Retrieve user details
                        String username = dataSnapshot.child("fullName").getValue(String.class);
                        String phoneNumber = dataSnapshot.child("mobile").getValue(String.class);
                        String emailId = dataSnapshot.child("email").getValue(String.class);
                        TextView actualNameOfUser = findViewById(R.id.actualName2);
                        TextView actualMobileOfUser = findViewById(R.id.actualMobile);
                        TextView actualEmailOfUser = findViewById(R.id.actualEmail);
                        actualNameOfUser.setText(username);
                        actualMobileOfUser.setText(phoneNumber);
                        actualEmailOfUser.setText(emailId);
                        // Use the user details in your app
                        Log.d("User Details", "Username: " + username + ", Mobile: " + phoneNumber);
                    } else {
                        // Handle the case where the user does not exist
                        Log.d("User Details", "User not found");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors
                    Log.d("User Details", "Error fetching user details: " + databaseError.getMessage());
                }
            });


        }
    }
}
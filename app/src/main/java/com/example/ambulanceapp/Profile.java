package com.example.ambulanceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {
    ImageView arrow;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        arrow = findViewById(R.id.arrow);
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get a reference to the Firebase Realtime Database
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
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
                        String username = dataSnapshot.child("firstName").getValue(String.class);
                        String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                        TextView actualNameOfUser = findViewById(R.id.actualName);
                        TextView actualMobileOfUser = findViewById(R.id.ActualMobile);
                        actualNameOfUser.setText(username);
                        actualMobileOfUser.setText(phoneNumber);
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
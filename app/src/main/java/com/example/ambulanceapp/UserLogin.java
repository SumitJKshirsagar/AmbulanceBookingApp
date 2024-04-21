package com.example.ambulanceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class UserLogin extends AppCompatActivity {
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        ImageView imageView = findViewById(R.id.imageView3);
        TextView textView = findViewById(R.id.textView2);
        EditText firstNameEditText = findViewById(R.id.editTextText);
        EditText lastNameEditText = findViewById(R.id.editTextText2);
        Button continueButton = findViewById(R.id.button);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the entered first and last names
                String firstName = firstNameEditText.getText().toString().trim();
                String lastName = lastNameEditText.getText().toString().trim();

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();

                    // Create a map to update only specific fields
                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put("firstName", firstName);
                    updateMap.put("lastName", lastName);

                    // Update the corresponding child in the database
                    userRef.child(userId).updateChildren(updateMap);

                    // Create an Intent to start the next activity
                    Intent intent = new Intent(UserLogin.this,UserMapsActivity.class);
                    startActivity(intent);
                } else {
                    // Handle the case where the current user is null (not authenticated)
                    // You might want to show an error message or take appropriate action
                }
            }
        });
    }
}

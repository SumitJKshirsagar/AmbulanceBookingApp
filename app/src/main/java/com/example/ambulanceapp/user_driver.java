package com.example.ambulanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;



import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class user_driver extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_driver);

        Button userButton = findViewById(R.id.userButton);
        Button driverButton = findViewById(R.id.driverButton);
        userButton = findViewById(R.id.userButton);
        driverButton = findViewById(R.id.driverButton);
        CheckBox checkBox = findViewById(R.id.checkBox);



        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the CheckBox is checked before proceeding
                if (checkBox.isChecked()) {

                    // The CheckBox is checked, proceed to the next activity for the user
                    Intent intent = new Intent(user_driver.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // The CheckBox is not checked, show a message or perform any required action
                    // For example, you can show a Toast message
                    Toast.makeText(user_driver.this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
                }
            }
        });

     
        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the CheckBox is checked before proceeding
                if (checkBox.isChecked()) {

                    // The CheckBox is checked, proceed to the next activity for the driver
                    Intent intent = new Intent(user_driver.this, DriverLoginActivity.class);
                    startActivity(intent);
                } else {
                    // The CheckBox is not checked, show a message or perform any required action
                    // For example, you can show a Toast message
                    Toast.makeText(user_driver.this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}


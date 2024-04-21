package com.example.ambulanceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class FlashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_screen);

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isVerified = preferences.getBoolean("isVerified", false);
        boolean isVerifiedDriver = preferences.getBoolean("isVerifiedDriver", false);

        if (isVerified) {

            Intent Intent = new Intent(this, UserMapsActivity.class);
            startActivity(Intent);
            finish();

            }else {
            if(isVerifiedDriver) {

                Intent Intent = new Intent(this, DriverMapsActivity.class);
                startActivity(Intent);
                finish();

            }else {

                Intent Intent = new Intent(this, user_driver.class);
                startActivity(Intent);
                finish();
            }
            }



        }
    public void clearSharedPreferences(Context context) {
        if (context == null) {
            throw new NullPointerException("Context object is null");
            // or
            // return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    }

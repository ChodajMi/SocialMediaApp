// SplashActivity.java
package com.example.socialmediaapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay for a few seconds to show the splash screen
        new Handler().postDelayed(() -> {
            // Start RegistrationActivity after the splash screen
            Intent intent = new Intent(SplashScreen.this, RegistrationActivity.class);
            startActivity(intent);
            finish(); // Close the SplashActivity
        }, 2000); // 2 seconds delay
    }
}

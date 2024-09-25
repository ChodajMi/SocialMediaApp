package com.example.socialmediaapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediaapplication.MainActivity;
import com.example.socialmediaapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CommentActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword;
    private ImageView ivEye;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.email_input);
        etPassword = findViewById(R.id.password_input);
        //ivEye = findViewById(R.id.);

        // Click listener for Sign Up
        TextView tvSignUp = findViewById(R.id.btn_sign_up);
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });

        // Click listener for Login
        Button btnLogin = findViewById(R.id.btn_sign_in);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Click listener for toggle password visibility
        ivEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });
    }

    private void signUpUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success
                        Toast.makeText(CommentActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                        // Redirect to MainActivity or desired activity
                        startActivity(new Intent(CommentActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // If sign up fails, display a message to the user
                        Toast.makeText(CommentActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login success
                        Toast.makeText(CommentActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        // Redirect to MainActivity
                        startActivity(new Intent(CommentActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // If login fails, display a message to the user
                        Toast.makeText(CommentActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void togglePasswordVisibility() {
        if (etPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            //ivEye.setImageResource(R.drawable.ic_eye_closed); // Replace with your closed eye drawable
        } else {
            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            //ivEye.setImageResource(R.drawable.ic_eye_open); // Replace with your open eye drawable
        }
        etPassword.setSelection(etPassword.getText().length());
    }
}

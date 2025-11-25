package com.example.medicinetime;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class activity_login extends AppCompatActivity {

    TextInputEditText emailEditText, passwordEditText;
    MaterialButton loginButton;
    TextView goToSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // <- this is your login XML

        // Views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        goToSignup = findViewById(R.id.goToSignup);

        // Animate fields
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        emailEditText.startAnimation(fadeIn);
        passwordEditText.startAnimation(fadeIn);
        loginButton.startAnimation(fadeIn);
        goToSignup.startAnimation(fadeIn);

        // Login Button click (for now simple Toast)
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(activity_login.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Add Firebase Auth login here
            Toast.makeText(activity_login.this, "Login successful!", Toast.LENGTH_SHORT).show();

            // Navigate to MainActivity (Dashboard)
            Intent intent = new Intent(activity_login.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        // Go to SignupActivity
        goToSignup.setOnClickListener(v -> {
            Intent intent = new Intent(activity_login.this, activity_signup.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }
}
package com.example.medicinetime;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class activity_login extends AppCompatActivity {

    TextInputEditText emailEditText, passwordEditText;
    MaterialButton loginButton;
    TextView goToSignup;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // 🔐 AUTO LOGIN CHECK
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(activity_login.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        goToSignup = findViewById(R.id.goToSignup);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        emailEditText.startAnimation(fadeIn);
        passwordEditText.startAnimation(fadeIn);
        loginButton.startAnimation(fadeIn);
        goToSignup.startAnimation(fadeIn);

        loginButton.setOnClickListener(v -> userLogin());

        goToSignup.setOnClickListener(v -> {
            startActivity(new Intent(activity_login.this, activity_signup.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void userLogin() {

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password required");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Logging In...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    loginButton.setEnabled(true);
                    loginButton.setText("Login");

                    if (task.isSuccessful()) {

                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(activity_login.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(this,
                                "Login Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}

package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kidapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText usernameInput, passwordInput, repeatPasswordInput;
    private Button registerButton;
    private TextView signInLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Configure Edge-to-Edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput);
        registerButton = findViewById(R.id.btnLogin); // Using the same ID from your layout
        signInLink = findViewById(R.id.tvSignUp);

        // Set up register button click listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String repeatPassword = repeatPasswordInput.getText().toString().trim();

                if (validateForm(username, password, repeatPassword)) {
                    // Assuming username is email for Firebase Auth
                    registerUser(username, password);
                }
            }
        });

        // Set up sign in link click listener
        signInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Login Activity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            }
        });
    }

    private boolean validateForm(String username, String password, String repeatPassword) {
        boolean valid = true;

        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("Required.");
            valid = false;
        } else {
            usernameInput.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Required.");
            valid = false;
        } else {
            passwordInput.setError(null);
        }

        if (TextUtils.isEmpty(repeatPassword)) {
            repeatPasswordInput.setError("Required.");
            valid = false;
        } else if (!password.equals(repeatPassword)) {
            repeatPasswordInput.setError("Passwords do not match.");
            valid = false;
        } else {
            repeatPasswordInput.setError(null);
        }

        return valid;
    }

    private void registerUser(String email, String password) {
        // Show progress indicator if needed

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success
                        Log.d("RegisterActivity", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Save additional user info to Firestore
                        saveUserToFirestore(user, email);

                        // Navigate to Login screen
                        Toast.makeText(RegisterActivity.this, "Registration Successful",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        // If sign up fails, display a message to the user
                        Log.w("RegisterActivity", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Registration failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    // Hide progress indicator if needed
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String email) {
        if (user != null) {
            // Create user data map
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("createdAt", System.currentTimeMillis());

            // Add a new document with the user's UID
            db.collection("users").document(user.getUid())
                    .set(userData)
                    .addOnSuccessListener(aVoid -> Log.d("RegisterActivity", "User data saved to Firestore"))
                    .addOnFailureListener(e -> Log.w("RegisterActivity", "Error saving user data", e));
        }
    }
}
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
    private EditText usernameInput, displayNameInput, passwordInput, repeatPasswordInput;
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
        displayNameInput = findViewById(R.id.displayNameInput);  // Added username field
        passwordInput = findViewById(R.id.passwordInput);
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput);
        registerButton = findViewById(R.id.btnLogin); // Using the same ID from your layout
        signInLink = findViewById(R.id.tvSignUp);

        // Set up register button click listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = usernameInput.getText().toString().trim();
                String username = displayNameInput.getText().toString().trim();  // Get the username
                String password = passwordInput.getText().toString().trim();
                String repeatPassword = repeatPasswordInput.getText().toString().trim();

                if (validateForm(email, username, password, repeatPassword)) {
                    // Register with email and password for Firebase Auth
                    registerUser(email, username, password);
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

    private boolean validateForm(String email, String username, String password, String repeatPassword) {
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            usernameInput.setError("Required.");
            valid = false;
        } else {
            usernameInput.setError(null);
        }

        if (TextUtils.isEmpty(username)) {
            displayNameInput.setError("Username is required.");
            valid = false;
        } else {
            displayNameInput.setError(null);
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

    private void registerUser(String email, String username, String password) {
        // Show progress indicator if needed

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success
                        Log.d("RegisterActivity", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Save additional user info to Firestore including username
                        saveUserToFirestore(user, email, username);

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

    private void saveUserToFirestore(FirebaseUser user, String email, String username) {
        if (user != null) {
            // Create user data map with additional username field
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("username", username);  // Store the username
            userData.put("createdAt", System.currentTimeMillis());

            // Add a new document with the user's UID
            db.collection("users").document(user.getUid())
                    .set(userData)
                    .addOnSuccessListener(aVoid -> Log.d("RegisterActivity", "User data saved to Firestore"))
                    .addOnFailureListener(e -> Log.w("RegisterActivity", "Error saving user data", e));
        }
    }
}
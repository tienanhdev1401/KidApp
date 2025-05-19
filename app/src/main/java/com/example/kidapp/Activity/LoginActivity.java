package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.kidapp.R;
import com.example.kidapp.ViewModel.LoginViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView forgotPasswordLink, signUpLink;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Configure Edge-to-Edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();

        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Initialize UI elements
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.btnLogin);
        forgotPasswordLink = findViewById(R.id.tvForgotPassword);
        signUpLink = findViewById(R.id.tvSignUp);

        // Add progress bar to layout if not already added
        // progressBar = findViewById(R.id.progressBar);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        // Set up login button click listener
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            loginViewModel.login(email, password);
        });

        // Set up forgot password link click listener
        forgotPasswordLink.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            loginViewModel.resetPassword(email);
        });

        // Set up sign up link click listener
        signUpLink.setOnClickListener(v -> {
            // Navigate to Register Activity
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        // Observe user authentication state
        loginViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                // User is logged in, navigate to MainActivity
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
                finish();
            }
        });

        // Observe loading state
        loginViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            // Toggle progress bar visibility
            // progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

            // Toggle controls enabled/disabled during loading
            boolean controlsEnabled = !isLoading;
            emailInput.setEnabled(controlsEnabled);
            passwordInput.setEnabled(controlsEnabled);
            loginButton.setEnabled(controlsEnabled);
        });

        // Observe error messages
        loginViewModel.getErrorLiveData().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe form field validation errors
        loginViewModel.getEmailError().observe(this, error -> {
            emailInput.setError(error);
        });

        loginViewModel.getPasswordError().observe(this, error -> {
            passwordInput.setError(error);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly

    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
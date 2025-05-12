package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.kidapp.R;
import com.example.kidapp.ViewModel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel registerViewModel;
    private EditText usernameInput, displayNameInput, passwordInput, repeatPasswordInput;
    private Button registerButton;
    private TextView signInLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        usernameInput = findViewById(R.id.usernameInput);
        displayNameInput = findViewById(R.id.displayNameInput);
        passwordInput = findViewById(R.id.passwordInput);
        repeatPasswordInput = findViewById(R.id.repeatPasswordInput);
        registerButton = findViewById(R.id.btnLogin);
        signInLink = findViewById(R.id.tvSignUp);

        observeViewModel();

        registerButton.setOnClickListener(v -> {
            String email = usernameInput.getText().toString().trim();
            String username = displayNameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String repeatPassword = repeatPasswordInput.getText().toString().trim();

            if (registerViewModel.validateForm(email, username, password, repeatPassword)) {
                registerViewModel.registerUser(email, username, password);
            }
        });

        signInLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void observeViewModel() {
        registerViewModel.getEmailError().observe(this, error -> usernameInput.setError(error));
        registerViewModel.getUsernameError().observe(this, error -> displayNameInput.setError(error));
        registerViewModel.getPasswordError().observe(this, error -> passwordInput.setError(error));
        registerViewModel.getRepeatPasswordError().observe(this, error -> repeatPasswordInput.setError(error));

        registerViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        registerViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Registration failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

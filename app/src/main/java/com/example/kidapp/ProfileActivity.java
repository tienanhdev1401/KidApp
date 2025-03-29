package com.example.kidapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Back button functionality
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Options button functionality
        ImageView optionsButton = findViewById(R.id.optionsButton);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Options clicked", Toast.LENGTH_SHORT).show();
                // Here you can show a menu or dialog with options
            }
        });

        // Edit button functionality
        ImageView editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Edit profile clicked", Toast.LENGTH_SHORT).show();
                // Here you would typically start an edit profile activity
            }
        });

        // Help Desk option functionality
        LinearLayout helpDeskOption = findViewById(R.id.helpDeskOption);
        helpDeskOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Help Desk clicked", Toast.LENGTH_SHORT).show();
                // Start help desk activity or show dialog
            }
        });

        // Log Out option functionality
        LinearLayout logOutOption = findViewById(R.id.logOutOption);
        logOutOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Log Out clicked", Toast.LENGTH_SHORT).show();
                // Implement logout logic here
                // Typically this would clear user session and go back to login screen
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // You can add any additional back button behavior here
    }
}
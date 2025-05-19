package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.kidapp.R;

/**
 * Activity trung gian để điều hướng người dùng đến trang tạo truyện AIStoryCreatorActivity
 */
public class StoryCreationActivity extends AppCompatActivity {

    private CardView cardAiStory;
    private CardView cardManualStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_creation);

        // Thiết lập toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Tạo truyện mới");
        }

        // Ánh xạ các view
        cardAiStory = findViewById(R.id.cardAiStory);
        cardManualStory = findViewById(R.id.cardManualStory);

        // Thiết lập sự kiện click
        cardAiStory.setOnClickListener(v -> navigateToAIStoryCreator());
        cardManualStory.setOnClickListener(v -> navigateToManualStoryCreator());
    }

    private void navigateToAIStoryCreator() {
        Intent intent = new Intent(this, AIStoryCreatorActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToManualStoryCreator() {
        Intent intent = new Intent(this, ManualStoryCreatorActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 
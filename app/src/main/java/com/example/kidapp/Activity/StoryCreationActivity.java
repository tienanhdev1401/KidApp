package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.kidapp.R;

/**
 * Activity trung gian để điều hướng người dùng đến trang tạo truyện AIStoryCreatorActivity
 */
public class StoryCreationActivity extends AppCompatActivity {

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

        // Chuyển hướng ngay đến AIStoryCreatorActivity
        redirectToAIStoryCreator();
    }

    private void redirectToAIStoryCreator() {
        Intent intent = new Intent(this, AIStoryCreatorActivity.class);
        startActivity(intent);
        finish(); // Kết thúc activity này để người dùng không quay lại
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
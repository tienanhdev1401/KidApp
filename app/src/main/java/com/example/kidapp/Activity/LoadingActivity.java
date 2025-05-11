package com.example.musicai.features.aistory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.musicai.R;
import com.example.musicai.features.aistory.model.StoryModel;
import com.example.musicai.features.aistory.service.GeminiService;

import java.util.ArrayList;

public class LoadingActivity extends AppCompatActivity {

    private static final String TAG = "LoadingActivity";
    private LottieAnimationView loadingAnimation;
    private TextView loadingText;
    private GeminiService geminiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        Log.d(TAG, "Activity created");

        // Initialize UI components
        loadingAnimation = findViewById(R.id.loadingAnimation);
        loadingText = findViewById(R.id.loadingText);

        if (loadingAnimation != null) {
            Log.d(TAG, "Animation view found");
            loadingAnimation.setAnimation(R.raw.loading_animation);
            loadingAnimation.playAnimation();
        } else {
            Log.e(TAG, "Animation view is null");
        }

        // Get data from Intent
        Intent intent = getIntent();
        
        // For backward compatibility, check if we have character lists or single character
        ArrayList<String> characters;
        ArrayList<String> items;
        
        if (intent.hasExtra("charactersList")) {
            characters = intent.getStringArrayListExtra("charactersList");
            Log.d(TAG, "Characters list size: " + (characters != null ? characters.size() : 0));
        } else {
            characters = new ArrayList<>();
            String character = intent.getStringExtra("character");
            if (character != null && !character.isEmpty()) {
                characters.add(character);
                Log.d(TAG, "Single character: " + character);
            }
        }
        
        if (intent.hasExtra("itemsList")) {
            items = intent.getStringArrayListExtra("itemsList");
            Log.d(TAG, "Items list size: " + (items != null ? items.size() : 0));
        } else {
            items = new ArrayList<>();
            String item = intent.getStringExtra("item");
            if (item != null && !item.isEmpty()) {
                items.add(item);
                Log.d(TAG, "Single item: " + item);
            }
        }
        
        String setting = intent.getStringExtra("setting");
        Log.d(TAG, "Setting: " + setting);

        // Initialize service to call Gemini AI with context
        geminiService = new GeminiService(this);

        // Create story
        generateStory(characters, setting, items);
    }

    private void generateStory(ArrayList<String> characters, String setting, ArrayList<String> items) {
        if (characters.isEmpty()) {
            Toast.makeText(this, "No characters selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        if (setting == null || setting.isEmpty()) {
            Toast.makeText(this, "No setting selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        if (items.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Cập nhật text loading để thông báo đang tạo cả truyện và hình ảnh
        loadingText.setText(getString(R.string.generating_story_and_images));
        
        geminiService.generateStoryAsync(characters, setting, items, new GeminiService.StoryCallback() {
            @Override
            public void onSuccess(StoryModel story) {
                // Move to story reader screen
                Intent intent = new Intent(LoadingActivity.this, StoryReaderActivity.class);
                intent.putExtra("story", story);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(() -> {
                    Toast.makeText(LoadingActivity.this, 
                            "Cannot create story: " + throwable.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up old images when activity is destroyed
        geminiService.cleanupOldImages();
    }

    @Override
    public void onBackPressed() {
        // Disable back button to prevent user from going back during story creation
        Toast.makeText(this, "Creating story, please wait...", Toast.LENGTH_SHORT).show();
    }
} 
package com.example.musicai;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicai.features.aistory.AIStoryCreatorActivity;

public class StoryHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history_story);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Cài đặt RecyclerView cho danh sách tính năng
        setupFeaturesList();
        
        // Thiết lập Floating Action Button để mở màn hình tạo truyện AI
        findViewById(R.id.fabAIStory).setOnClickListener(v -> startAIStoryCreator());
    }
    
    private void setupFeaturesList() {
        RecyclerView featuresRecyclerView = findViewById(R.id.featuresRecyclerView);
        // Cài đặt adapter cho danh sách tính năng (sẽ triển khai sau)
        // featuresRecyclerView.setAdapter(new FeaturesAdapter(getFeaturesList()));
    }
    
    public void startAIStoryCreator() {
        Intent intent = new Intent(this, AIStoryCreatorActivity.class);
        startActivity(intent);
    }
}
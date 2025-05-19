package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kidapp.Adapter.FeaturedStoryAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.StoryCategoryViewModel;
import com.example.kidapp.models.Story;

import java.util.ArrayList;
import java.util.List;

public class CategoryStoryActivity extends AppCompatActivity {
    private RecyclerView recyclerViewStories;
    private FeaturedStoryAdapter featuredStoryAdapter;
    private List<Story> storyList = new ArrayList<>();
    private View emptyStateView;
    private String categoryName;
    private StoryCategoryViewModel storyCategoryViewModel;
    private static final String TAG = "CategoryStoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_story);

        storyCategoryViewModel = new ViewModelProvider(this).get(StoryCategoryViewModel.class);
        
        // Lấy category name từ intent
        categoryName = getIntent().getStringExtra("categoryName");
        Log.d(TAG, "Category name received: " + categoryName);
        
        // Khởi tạo UI
        setUI();
        
        // Tải stories từ database
        loadStoriesFromDb();
        setBtnControl();
    }

    private void setBtnControl() {
        featuredStoryAdapter.setOnItemClickListener((position, story) -> {
            Intent intent = new Intent(this, StoryDetailActivity.class);
            intent.putExtra("story", story);
            intent.putParcelableArrayListExtra("playlist", new ArrayList<>(storyList));
            intent.putExtra("storyPosition", position);
            startActivity(intent);
        });
    }

    private void setUI() {
        // Set category name in top bar
        TextView categoryTitleText = findViewById(R.id.categoryTitleText);
        if (categoryName != null) {
            categoryTitleText.setText(categoryName);
        } else {
            categoryTitleText.setText("Stories");
        }

        // Set category description based on category name
        TextView categoryDescription = findViewById(R.id.categoryDescription);
        if (categoryName != null) {
            switch (categoryName.toLowerCase()) {
                case "animals":
                    categoryDescription.setText("Fascinating stories about animals!");
                    break;
                case "fairy tales":
                    categoryDescription.setText("Classic tales of magic and wonder!");
                    break;
                case "moral stories":
                    categoryDescription.setText("Stories with valuable life lessons!");
                    break;
                case "adventure":
                    categoryDescription.setText("Exciting journeys and brave heroes!");
                    break;
                default:
                    categoryDescription.setText("Discover amazing stories!");
                    break;
            }
        } else {
            categoryDescription.setText("Discover amazing stories!");
        }

        // Setup stories RecyclerView
        recyclerViewStories = findViewById(R.id.categoryRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewStories.setLayoutManager(layoutManager);
        featuredStoryAdapter = new FeaturedStoryAdapter(this, storyList, this);
        recyclerViewStories.setAdapter(featuredStoryAdapter);
        recyclerViewStories.setHasFixedSize(true);
        
        // Empty state view
        emptyStateView = findViewById(R.id.emptyStateContainer);

        // Back button click listener
        ImageView backButton = findViewById(R.id.categoryBackButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void loadStoriesFromDb() {
        if (categoryName == null || categoryName.isEmpty()) {
            Log.d(TAG, "Category name is null or empty");
            emptyStateView.setVisibility(View.VISIBLE);
            recyclerViewStories.setVisibility(View.GONE);
            return;
        }

        Log.d(TAG, "Loading stories for category: " + categoryName);
        
        storyCategoryViewModel.getStoryByCategoryName(categoryName).observe(this, stories -> {
            if (stories != null && !stories.isEmpty()) {
                Log.d(TAG, "Stories loaded: " + stories.size());
                storyList.clear();
                storyList.addAll(stories);
                featuredStoryAdapter.notifyDataSetChanged();
                recyclerViewStories.setVisibility(View.VISIBLE);
                emptyStateView.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "No stories found for category");
                recyclerViewStories.setVisibility(View.GONE);
                emptyStateView.setVisibility(View.VISIBLE);
            }
        });
    }
}
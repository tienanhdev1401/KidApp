package com.example.musicai.features.aistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicai.R;
import com.example.musicai.databinding.ActivityAiStoryCreatorBinding;
import com.example.musicai.features.aistory.ViewModel.StoryElementViewModel;
import com.example.musicai.features.aistory.adapter.StoryElementAdapter;
import com.example.musicai.features.aistory.model.StoryElement;

public class AIStoryCreatorActivity extends AppCompatActivity {
    private ActivityAiStoryCreatorBinding binding;
    private StoryElementViewModel viewModel;
    
    private StoryElementAdapter characterAdapter;
    private StoryElementAdapter settingAdapter;
    private StoryElementAdapter itemAdapter;
    
    private StoryElement selectedCharacter;
    private StoryElement selectedSetting;
    private StoryElement selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiStoryCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this, new StoryElementViewModel.Factory())
                .get(StoryElementViewModel.class);

        setupRecyclerViews();
        setupObservers();
        setupClickListeners();
    }

    private void setupRecyclerViews() {
        // Setup character adapter
        characterAdapter = new StoryElementAdapter(character -> {
            selectedCharacter = character;
            updatePreview();
        });
        binding.characterRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.characterRecyclerView.setAdapter(characterAdapter);

        // Setup setting adapter
        settingAdapter = new StoryElementAdapter(setting -> {
            selectedSetting = setting;
            updatePreview();
        });
        binding.settingRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.settingRecyclerView.setAdapter(settingAdapter);

        // Setup item adapter
        itemAdapter = new StoryElementAdapter(item -> {
            selectedItem = item;
            updatePreview();
        });
        binding.itemRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.itemRecyclerView.setAdapter(itemAdapter);
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading ->
                binding.progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getCharacters().observe(this, characters -> 
                characterAdapter.updateData(characters));

        viewModel.getSettings().observe(this, settings -> 
                settingAdapter.updateData(settings));

        viewModel.getItems().observe(this, items -> 
                itemAdapter.updateData(items));
    }

    private void setupClickListeners() {
        binding.createStoryButton.setOnClickListener(v -> {
            if (selectedCharacter != null && selectedSetting != null && selectedItem != null) {
                createStory();
            } else {
                Toast.makeText(this, "Vui lòng chọn đầy đủ các yếu tố", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePreview() {
        // Update preview UI based on selections
        binding.previewEmptyText.setVisibility(selectedSetting == null ? View.VISIBLE : View.GONE);
        
        // Update background
        if (selectedSetting != null) {
            binding.previewBackgroundImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(selectedSetting.getImageUrl())
                    .into(binding.previewBackgroundImage);
        }

        // Update selection count
        int selectedCount = 0;
        if (selectedCharacter != null) selectedCount++;
        if (selectedSetting != null) selectedCount++;
        if (selectedItem != null) selectedCount++;

        binding.selectionCountText.setText(selectedCount + "/3");
        binding.selectionCountText.setVisibility(View.VISIBLE);
    }

    private void createStory() {
        Intent intent = new Intent(this, LoadingActivity.class);
        intent.putExtra("character_id", selectedCharacter.getId());
        intent.putExtra("setting_id", selectedSetting.getId());
        intent.putExtra("item_id", selectedItem.getId());
        startActivity(intent);
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
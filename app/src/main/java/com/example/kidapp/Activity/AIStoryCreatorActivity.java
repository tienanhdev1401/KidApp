package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.databinding.ActivityAiStoryCreatorBinding;
import com.example.kidapp.ViewModel.StoryElementViewModel;
import com.example.kidapp.Adapter.StoryElementAdapter;
import com.example.kidapp.models.StoryElement;
import com.example.kidapp.models.StoryByAiModel;

import java.util.ArrayList;
import java.util.List;

public class AIStoryCreatorActivity extends AppCompatActivity {
    private ActivityAiStoryCreatorBinding binding;
    private StoryElementViewModel viewModel;
    
    private StoryElementAdapter characterAdapter;
    private StoryElementAdapter settingAdapter;
    private StoryElementAdapter itemAdapter;
    
    private StoryElement selectedSetting;
    private List<StoryElement> selectedCharacters = new ArrayList<>();
    private List<StoryElement> selectedItems = new ArrayList<>();
    
    private static final int MAX_CHARACTER_SELECTIONS = 3;
    private static final int MAX_ITEM_SELECTIONS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiStoryCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tạo truyện AI");
        }

        viewModel = new ViewModelProvider(this, new StoryElementViewModel.Factory())
                .get(StoryElementViewModel.class);

        setupRecyclerViews();
        setupObservers();
        setupClickListeners();
    }

    private void setupRecyclerViews() {
        // Setup character adapter
        characterAdapter = new StoryElementAdapter(
            (element, isSelected) -> {
                if (isSelected) {
                    selectedCharacters.add(element);
                } else {
                    selectedCharacters.remove(element);
                }
                updateCharacterCount();
                updatePreview();
            }, 
            StoryElement.ElementType.CHARACTER, 
            MAX_CHARACTER_SELECTIONS
        );
        binding.characterRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.characterRecyclerView.setAdapter(characterAdapter);

        // Setup setting adapter
        settingAdapter = new StoryElementAdapter(
            (element, isSelected) -> {
                if (isSelected) {
                    selectedSetting = element;
                } else {
                    selectedSetting = null;
                }
                updatePreview();
            }, 
            StoryElement.ElementType.SETTING, 
            1
        );
        binding.settingRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.settingRecyclerView.setAdapter(settingAdapter);

        // Setup item adapter
        itemAdapter = new StoryElementAdapter(
            (element, isSelected) -> {
                if (isSelected) {
                    selectedItems.add(element);
                } else {
                    selectedItems.remove(element);
                }
                updateItemCount();
                updatePreview();
            }, 
            StoryElement.ElementType.ITEM, 
            MAX_ITEM_SELECTIONS
        );
        binding.itemRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.itemRecyclerView.setAdapter(itemAdapter);
    }

    private void updateCharacterCount() {
        binding.characterCountText.setText(selectedCharacters.size() + "/" + MAX_CHARACTER_SELECTIONS);
    }

    private void updateItemCount() {
        binding.itemCountText.setText(selectedItems.size() + "/" + MAX_ITEM_SELECTIONS);
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
            if (selectedSetting == null) {
                Toast.makeText(this, "Vui lòng chọn bối cảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedCharacters.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một nhân vật", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một vật phẩm", Toast.LENGTH_SHORT).show();
                return;
            }
            
            createStory();
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
            
            // Hiển thị container cho nhân vật và vật phẩm
            binding.charactersContainer.setVisibility(View.VISIBLE);
            binding.itemsContainer.setVisibility(View.VISIBLE);
        } else {
            binding.charactersContainer.setVisibility(View.GONE);
            binding.itemsContainer.setVisibility(View.GONE);
        }
        
        // Cập nhật nhân vật đã chọn
        updateCharactersPreview();
        
        // Cập nhật vật phẩm đã chọn
        updateItemsPreview();

        // Update selection count
        int selectedCount = 0;
        if (!selectedCharacters.isEmpty()) selectedCount++;
        if (selectedSetting != null) selectedCount++;
        if (!selectedItems.isEmpty()) selectedCount++;

        binding.selectionCountText.setText(selectedCount + "/3");
        binding.selectionCountText.setVisibility(View.VISIBLE);
    }
    
    private void updateCharactersPreview() {
        // Xóa tất cả view hiện tại
        binding.charactersContainer.removeAllViews();
        
        // Hiển thị container nếu có nhân vật được chọn
        binding.charactersContainer.setVisibility(selectedCharacters.isEmpty() ? View.GONE : View.VISIBLE);
        
        // Thêm các nhân vật đã chọn vào container
        for (StoryElement character : selectedCharacters) {
            ImageView characterView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);
            params.setMargins(8, 0, 8, 0);
            characterView.setLayoutParams(params);
            characterView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            
            // Tạo border cho ảnh
            characterView.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_border));
            characterView.setClipToOutline(true);
            characterView.setPadding(4, 4, 4, 4);
            
            // Load ảnh nhân vật
            Glide.with(this)
                    .load(character.getImageUrl())
                    .circleCrop()
                    .into(characterView);
            
            // Thêm sự kiện click để bỏ chọn nhân vật
            characterView.setOnClickListener(v -> {
                // Tìm và bỏ chọn nhân vật này trong adapter
                int position = -1;
                List<StoryElement> elements = characterAdapter.getSelectedElements();
                for (int i = 0; i < elements.size(); i++) {
                    if (elements.get(i).getId().equals(character.getId())) {
                        position = i;
                        break;
                    }
                }
                
                if (position >= 0) {
                    // Thông báo cho adapter bỏ chọn phần tử này
                    characterAdapter.toggleSelection(character);
                    updatePreview();
                }
            });
            
            binding.charactersContainer.addView(characterView);
        }
    }
    
    private void updateItemsPreview() {
        // Xóa tất cả view hiện tại
        binding.itemsContainer.removeAllViews();
        
        // Hiển thị container nếu có vật phẩm được chọn
        binding.itemsContainer.setVisibility(selectedItems.isEmpty() ? View.GONE : View.VISIBLE);
        
        // Thêm các vật phẩm đã chọn vào container
        for (StoryElement item : selectedItems) {
            ImageView itemView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
            params.setMargins(8, 0, 8, 0);
            itemView.setLayoutParams(params);
            itemView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            
            // Tạo border cho ảnh
            itemView.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_border));
            itemView.setClipToOutline(true);
            itemView.setPadding(4, 4, 4, 4);
            
            // Load ảnh vật phẩm
            Glide.with(this)
                    .load(item.getImageUrl())
                    .circleCrop()
                    .into(itemView);
            
            // Thêm sự kiện click để bỏ chọn vật phẩm
            itemView.setOnClickListener(v -> {
                // Tìm và bỏ chọn vật phẩm này trong adapter
                int position = -1;
                List<StoryElement> elements = itemAdapter.getSelectedElements();
                for (int i = 0; i < elements.size(); i++) {
                    if (elements.get(i).getId().equals(item.getId())) {
                        position = i;
                        break;
                    }
                }
                
                if (position >= 0) {
                    // Thông báo cho adapter bỏ chọn phần tử này
                    itemAdapter.toggleSelection(item);
                    updatePreview();
                }
            });
            
            binding.itemsContainer.addView(itemView);
        }
    }

    private void createStory() {
        Intent intent = new Intent(this, LoadingActivity.class);
        
        // Tạo danh sách tên nhân vật
        ArrayList<String> characterNames = new ArrayList<>();
        for (StoryElement character : selectedCharacters) {
            characterNames.add(character.getName());
        }
        
        // Tạo danh sách tên vật phẩm
        ArrayList<String> itemNames = new ArrayList<>();
        for (StoryElement item : selectedItems) {
            itemNames.add(item.getName());
        }
        
        // Truyền dữ liệu theo cách LoadingActivity đang mong đợi
        intent.putStringArrayListExtra("charactersList", characterNames);
        intent.putStringArrayListExtra("itemsList", itemNames);
        intent.putExtra("setting", selectedSetting.getName());
        
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
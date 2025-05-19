package com.example.kidapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kidapp.Repository.StoryCategoryRepository;
import com.example.kidapp.models.Story;
import com.example.kidapp.models.StoryCategory;

import java.util.List;

public class StoryCategoryViewModel extends AndroidViewModel {
    private final StoryCategoryRepository storyCategoryRepository;

    public StoryCategoryViewModel(@NonNull Application application) {
        super(application);
        storyCategoryRepository = new StoryCategoryRepository(application);
    }

    public LiveData<List<StoryCategory>> getAllStoryCategories() {
        return storyCategoryRepository.getAllStoryCategories();
    }

    public LiveData<List<Story>> getStoryByCategoryName(String categoryName) {
        return storyCategoryRepository.getStoryByCategoryName(categoryName);
    }

    public LiveData<String> insertStoryCategory(StoryCategory storyCategory) {
        return storyCategoryRepository.insertStoryCategory(storyCategory);
    }
}

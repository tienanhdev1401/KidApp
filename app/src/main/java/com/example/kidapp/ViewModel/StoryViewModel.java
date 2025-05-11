package com.example.kidapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kidapp.Repository.StoryRepository;
import com.example.kidapp.models.Story;

import java.util.List;

public class StoryViewModel extends AndroidViewModel {
    private final StoryRepository storyRepository;

    public StoryViewModel(@NonNull Application application) {
        super(application);
        storyRepository = new StoryRepository(application);
    }

    public LiveData<List<Story>> getAllStories() {
        return storyRepository.getAllStories();
    }

    public LiveData<String> insertStory(Story story) {
        return storyRepository.insertStory(story);
    }

}

package com.example.kidapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.kidapp.Repository.StoryElementRepository;
import com.example.kidapp.models.StoryElement;

import java.util.List;

public class StoryElementViewModel extends ViewModel {
    private final StoryElementRepository repository;
    
    private final MutableLiveData<List<StoryElement>> characters = new MutableLiveData<>();
    private final MutableLiveData<List<StoryElement>> settings = new MutableLiveData<>();
    private final MutableLiveData<List<StoryElement>> items = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public StoryElementViewModel(StoryElementRepository repository) {
        this.repository = repository;
        loadAllElements();
    }

    private void loadAllElements() {
        isLoading.setValue(true);

        // Load characters
        repository.getElementsByType(StoryElement.ElementType.CHARACTER)
                .addOnSuccessListener(characters::setValue)
                .addOnFailureListener(e -> error.setValue("Không thể tải nhân vật: " + e.getMessage()));

        // Load settings
        repository.getElementsByType(StoryElement.ElementType.SETTING)
                .addOnSuccessListener(settings::setValue)
                .addOnFailureListener(e -> error.setValue("Không thể tải bối cảnh: " + e.getMessage()));

        // Load items
        repository.getElementsByType(StoryElement.ElementType.ITEM)
                .addOnSuccessListener(result -> {
                    items.setValue(result);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    error.setValue("Không thể tải vật phẩm: " + e.getMessage());
                    isLoading.setValue(false);
                });
    }

    public void refreshData() {
        loadAllElements();
    }

    public LiveData<List<StoryElement>> getCharacters() {
        return characters;
    }

    public LiveData<List<StoryElement>> getSettings() {
        return settings;
    }

    public LiveData<List<StoryElement>> getItems() {
        return items;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public static class Factory implements ViewModelProvider.Factory {
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(StoryElementViewModel.class)) {
                return (T) new StoryElementViewModel(new StoryElementRepository());
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
} 
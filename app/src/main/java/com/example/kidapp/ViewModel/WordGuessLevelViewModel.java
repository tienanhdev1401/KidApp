package com.example.kidapp.ViewModel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.kidapp.Repository.WordGuessLevelRepository;
import com.example.kidapp.models.WordGuessLevel;
import java.util.List;

public class WordGuessLevelViewModel extends AndroidViewModel {
    private final WordGuessLevelRepository repository;

    public WordGuessLevelViewModel(Application application) {
        super(application);
        repository = new WordGuessLevelRepository(application);
    }

    public LiveData<List<WordGuessLevel>> getAllLevels() {
        return repository.getAllLevels();
    }
} 
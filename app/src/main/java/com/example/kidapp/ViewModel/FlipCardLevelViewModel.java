package com.example.kidapp.ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kidapp.Repository.FlipCardLevelRepository;
import com.example.kidapp.models.FlipCardLevel;

import java.util.List;

public class FlipCardLevelViewModel extends AndroidViewModel {
    private final FlipCardLevelRepository flipCardLevelRepository;

    public FlipCardLevelViewModel(Application application) {
        super(application);
        flipCardLevelRepository = new FlipCardLevelRepository(application);
    }

    // Lấy tất cả level
    public LiveData<List<FlipCardLevel>> getAllLevels() {
        return flipCardLevelRepository.getAllLevels();
    }
} 
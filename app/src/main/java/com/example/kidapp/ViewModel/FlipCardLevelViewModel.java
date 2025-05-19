package com.example.kidapp.ViewModel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.kidapp.Repository.FlipCardLevelRepository;
import com.example.kidapp.models.FlipCardLevel;
import java.util.List;

public class FlipCardLevelViewModel extends AndroidViewModel {
    private final FlipCardLevelRepository repository;

    public FlipCardLevelViewModel(Application application) {
        super(application);
        repository = new FlipCardLevelRepository(application);
    }

    public LiveData<List<FlipCardLevel>> getAllLevels() {
        return repository.getAllLevels();
    }
} 
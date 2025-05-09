package com.example.kidapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kidapp.Repository.MusicCategoryRepository;
import com.example.kidapp.models.Music;
import com.example.kidapp.models.MusicCategory;

import java.util.List;

public class MusicCategoryViewModel extends AndroidViewModel {

    private final MusicCategoryRepository musicCategoryRepository;

    public MusicCategoryViewModel(@NonNull Application application) {
        super(application);
        musicCategoryRepository = new MusicCategoryRepository(application);
    }

    public LiveData<List<MusicCategory>> getAllMusicCategories() {
        return musicCategoryRepository.getAllMusicCategories();
    }
}





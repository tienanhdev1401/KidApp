package com.example.kidapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kidapp.Repository.MusicRepository;
import com.example.kidapp.models.Music;

import java.util.List;

public class MusicViewModel extends AndroidViewModel  {
    private final MusicRepository musicRepository;
    public MusicViewModel(@NonNull Application application) {
        super(application);
        musicRepository = new MusicRepository(application);
    }
    public LiveData<List<Music>> getAllMusics() {
        return musicRepository.getAllMusics();
    }

    public LiveData<Music> getMusicById(String id) {
        return musicRepository.getMusicById(id);
    }

    public LiveData<List<Music>> getMusicByType(String type) {
        return musicRepository.getMusicByType(type);
    }

    public LiveData<String> insertMusic(Music music) {
        return musicRepository.insertMusic(music);
    }
}

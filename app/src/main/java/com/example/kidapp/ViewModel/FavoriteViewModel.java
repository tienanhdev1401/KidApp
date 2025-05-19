package com.example.kidapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kidapp.Repository.FavoriteRepository;
import com.example.kidapp.models.Music;
import com.example.kidapp.models.Story;

import java.util.List;

public class FavoriteViewModel extends AndroidViewModel {
    private final FavoriteRepository favoriteRepository;
    public FavoriteViewModel(@NonNull Application application) {
        super(application);
        favoriteRepository = new FavoriteRepository(application);
    }

    public void toggleMusicFavorite(String userEmail, String musicId) {
        favoriteRepository.toggleMusicFavorite(userEmail, musicId);
    }

    public void toggleStoryFavorite(String userEmail, String storyId) {
        favoriteRepository.toggleStoryFavorite(userEmail, storyId);
    }

    public LiveData<Boolean> isMusicFavorite(String userEmail, String musicId) {
        return favoriteRepository.isMusicFavorite(userEmail, musicId);
    }

    public LiveData<Boolean> isStoryFavorite(String userEmail, String storyId) {
        return favoriteRepository.isStoryFavorite(userEmail, storyId);
    }

    public LiveData<List<Music>> getFavoriteMusic(String userEmail) {
        return favoriteRepository.getFavoriteMusic(userEmail);
    }

    public LiveData<List<Story>> getFavoriteStory(String userEmail) {
        return favoriteRepository.getFavoriteStory(userEmail);
    }
}

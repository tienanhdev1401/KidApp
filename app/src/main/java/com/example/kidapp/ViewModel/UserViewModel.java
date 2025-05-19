package com.example.kidapp.ViewModel;
import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kidapp.Repository.UserRepository;
import com.example.kidapp.models.User;
import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    public UserViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }
    // Get all users
    public LiveData<List<User>> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public LiveData<User> getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    public void updateAchievements(String userEmail, java.util.List<String> achievements) {
        userRepository.updateAchievements(userEmail, achievements);
    }

    public void addStoryRead(String userEmail, String storyId) {
        userRepository.addStoryRead(userEmail, storyId);
    }

    public void updateListeningTime(String userEmail, int totalListeningTime) {
        userRepository.updateListeningTime(userEmail, totalListeningTime);
    }

    public void updateGameProgress(String userEmail, String gameKey, int newLevel, int newScore) {
        userRepository.updateGameProgress(userEmail, gameKey, newLevel, newScore);
    }

    public void updateUser(String email, String gender, String dateOfBirth, String phone, String avatarUrl) {
        userRepository.updateUser(email, gender, dateOfBirth, phone, avatarUrl);
    }

    public void addPuzzleScore(String userEmail, int score) {
        userRepository.addPuzzleScore(userEmail, score);
    }
}
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
}
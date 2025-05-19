package com.example.kidapp.ViewModel;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kidapp.Repository.AuthRepository;
import com.example.kidapp.models.User;

public class RegisterViewModel extends ViewModel {
    private AuthRepository authRepository;
    private MutableLiveData<String> emailError = new MutableLiveData<>();
    private MutableLiveData<String> usernameError = new MutableLiveData<>();
    private MutableLiveData<String> passwordError = new MutableLiveData<>();
    private MutableLiveData<String> repeatPasswordError = new MutableLiveData<>();
    private MutableLiveData<Boolean> isFormValid = new MutableLiveData<>(false);

    public RegisterViewModel() {
        authRepository = AuthRepository.getInstance();
    }

    public LiveData<String> getEmailError() {
        return emailError;
    }

    public LiveData<String> getUsernameError() {
        return usernameError;
    }

    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    public LiveData<String> getRepeatPasswordError() {
        return repeatPasswordError;
    }

    public LiveData<Boolean> getIsFormValid() {
        return isFormValid;
    }

    public LiveData<User> getUserLiveData() {
        return authRepository.getUserLiveData();
    }

    public LiveData<String> getErrorLiveData() {
        return authRepository.getErrorLiveData();
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return authRepository.getIsLoadingLiveData();
    }

    public boolean validateForm(String email, String username, String password, String repeatPassword) {
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            emailError.setValue("Email is required");
            valid = false;
        } else if (!isValidEmail(email)) {
            emailError.setValue("Invalid email format");
            valid = false;
        } else {
            emailError.setValue(null);
        }

        if (TextUtils.isEmpty(username)) {
            usernameError.setValue("Username is required");
            valid = false;
        } else {
            usernameError.setValue(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordError.setValue("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            passwordError.setValue("Password must be at least 6 characters");
            valid = false;
        } else {
            passwordError.setValue(null);
        }

        if (TextUtils.isEmpty(repeatPassword)) {
            repeatPasswordError.setValue("Repeat password is required");
            valid = false;
        } else if (!password.equals(repeatPassword)) {
            repeatPasswordError.setValue("Passwords do not match");
            valid = false;
        } else {
            repeatPasswordError.setValue(null);
        }

        isFormValid.setValue(valid);
        return valid;
    }

    public void registerUser(String email, String username, String password) {
        authRepository.registerUser(email, username, password);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}

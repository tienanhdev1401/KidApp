package com.example.kidapp.ViewModel;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kidapp.models.User;
import com.example.kidapp.Repository.AuthRepository;

public class LoginViewModel extends ViewModel {
    private AuthRepository authRepository;
    private MutableLiveData<String> emailError;
    private MutableLiveData<String> passwordError;
    private MutableLiveData<Boolean> isFormValid;

    public LoginViewModel() {
        authRepository = AuthRepository.getInstance();
        emailError = new MutableLiveData<>();
        passwordError = new MutableLiveData<>();
        isFormValid = new MutableLiveData<>(false);
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

    public LiveData<String> getEmailError() {
        return emailError;
    }

    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    public LiveData<Boolean> getIsFormValid() {
        return isFormValid;
    }

    public boolean validateForm(String email, String password) {
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            emailError.setValue("Email is required");
            isValid = false;
        } else if (!isValidEmail(email)) {
            emailError.setValue("Invalid email format");
            isValid = false;
        } else {
            emailError.setValue(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordError.setValue("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordError.setValue("Password must be at least 6 characters");
            isValid = false;
        } else {
            passwordError.setValue(null);
        }

        isFormValid.setValue(isValid);
        return isValid;
    }

    public void login(String email, String password) {
        if (validateForm(email, password)) {
            authRepository.loginUser(email, password);
        }
    }
    public void logout() {
        authRepository.logout();
    }

    public void resetPassword(String email) {
        if (TextUtils.isEmpty(email)) {
            emailError.setValue("Please enter your email address");
        } else if (!isValidEmail(email)) {
            emailError.setValue("Invalid email format");
        } else {
            authRepository.resetPassword(email);
        }
    }

    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
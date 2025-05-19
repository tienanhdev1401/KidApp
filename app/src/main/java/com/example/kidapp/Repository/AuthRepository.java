package com.example.kidapp.Repository;

import androidx.lifecycle.MutableLiveData;
import com.example.kidapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {
    private static AuthRepository instance;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private MutableLiveData<User> userLiveData;
    private MutableLiveData<String> errorLiveData;
    private MutableLiveData<Boolean> isLoadingLiveData;

    private AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        isLoadingLiveData = new MutableLiveData<>(false);

        // Check if user is already logged in
        if (firebaseAuth.getCurrentUser() != null) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            User user = new User(
                    firebaseUser.getEmail(),
                    firebaseUser.getUid(),
                    firebaseUser.getDisplayName()
            );
            userLiveData.setValue(user);
        }
    }

    public static synchronized AuthRepository getInstance() {
        if (instance == null) {
            instance = new AuthRepository();
        }
        return instance;
    }

    public MutableLiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public MutableLiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public void loginUser(String email, String password) {
        isLoadingLiveData.setValue(true);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    isLoadingLiveData.setValue(false);

                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        User user = new User(
                                firebaseUser.getEmail(),
                                firebaseUser.getUid(),
                                firebaseUser.getDisplayName()
                        );
                        userLiveData.setValue(user);
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Authentication failed";
                        errorLiveData.setValue(errorMessage);
                    }
                });
    }

    public void resetPassword(String email) {
        isLoadingLiveData.setValue(true);

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    isLoadingLiveData.setValue(false);

                    if (task.isSuccessful()) {
                        errorLiveData.setValue("Password reset email sent");
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Failed to send reset email";
                        errorLiveData.setValue(errorMessage);
                    }
                });
    }

    public void logout() {
        firebaseAuth.signOut();
        userLiveData.setValue(null);
    }

    // AuthRepository.java

    public void registerUser(String email, String username, String password) {
        isLoadingLiveData.setValue(true);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            // Save user info to Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("username", username);
                            userData.put("createdAt", System.currentTimeMillis());

                            db.collection("users")
                                    .document(firebaseUser.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Update LiveData on success
                                        User user = new User(firebaseUser.getUid(), username, email);
                                        userLiveData.setValue(user);
                                        errorLiveData.setValue(null);
                                        isLoadingLiveData.setValue(false);
                                    })
                                    .addOnFailureListener(e -> {
                                        errorLiveData.setValue("Failed to save user info: " + e.getMessage());
                                        isLoadingLiveData.setValue(false);
                                    });
                        } else {
                            errorLiveData.setValue("User is null after registration.");
                            isLoadingLiveData.setValue(false);
                        }
                    } else {
                        errorLiveData.setValue("Registration failed: " + task.getException().getMessage());
                        isLoadingLiveData.setValue(false);
                    }
                });
    }

    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
}
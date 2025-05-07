package com.example.kidapp.Repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserRepository {
    private final FirebaseFirestore db;

    public UserRepository(Application application) {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> liveData = new MutableLiveData<>();
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<User> userList = queryDocumentSnapshots.toObjects(User.class);
                        liveData.setValue(userList);
                    } else {
                        liveData.setValue(null); // If no users found
                    }
                })
                .addOnFailureListener(e -> liveData.setValue(null));
        return liveData;
    }
}

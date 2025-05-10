package com.example.kidapp.Repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.models.FlipCardLevel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FlipCardLevelRepository {
    private final FirebaseFirestore db;

    public FlipCardLevelRepository(Application application) {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<FlipCardLevel>> getAllLevels() {
        MutableLiveData<List<FlipCardLevel>> liveData = new MutableLiveData<>();
        db.collection("flipcardlevel")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<FlipCardLevel> levelList = queryDocumentSnapshots.toObjects(FlipCardLevel.class);
                        liveData.setValue(levelList);
                    } else {
                        liveData.setValue(null); // Nếu không có level nào
                    }
                })
                .addOnFailureListener(e -> liveData.setValue(null));
        return liveData;
    }
} 
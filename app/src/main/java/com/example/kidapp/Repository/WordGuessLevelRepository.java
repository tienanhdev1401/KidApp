package com.example.kidapp.Repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.kidapp.models.WordGuessLevel;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class WordGuessLevelRepository {
    private final FirebaseFirestore db;

    public WordGuessLevelRepository(Application application) {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<WordGuessLevel>> getAllLevels() {
        MutableLiveData<List<WordGuessLevel>> liveData = new MutableLiveData<>();
        db.collection("wordguesslevel")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<WordGuessLevel> list = queryDocumentSnapshots.toObjects(WordGuessLevel.class);
                    liveData.setValue(list);
                } else {
                    liveData.setValue(null);
                }
            })
            .addOnFailureListener(e -> liveData.setValue(null));
        return liveData;
    }
} 
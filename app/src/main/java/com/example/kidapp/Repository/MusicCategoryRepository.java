package com.example.kidapp.Repository;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.models.Music;
import com.example.kidapp.models.MusicCategory;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MusicCategoryRepository {
    private final FirebaseFirestore db;

    public MusicCategoryRepository(Application application) {
        db = FirebaseFirestore.getInstance();
    }


    public LiveData<List<MusicCategory>> getAllMusicCategories() {
        MutableLiveData<List<MusicCategory>> liveData = new MutableLiveData<>();
        db.collection("musicCategory")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<MusicCategory> category = queryDocumentSnapshots.toObjects(MusicCategory.class);
                        liveData.setValue(category);
                    } else {
                        liveData.setValue(null); // If no users found
                    }
                })
                .addOnFailureListener(e -> liveData.setValue(null));
        return liveData;

    }





}

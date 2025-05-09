package com.example.kidapp.Repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.models.Music;
import com.example.kidapp.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository {
    private final FirebaseFirestore db;

    public MusicRepository(Application application) {
        db = FirebaseFirestore.getInstance();

    }

    public LiveData<List<Music>> getAllMusics() {
        MutableLiveData<List<Music>> liveData = new MutableLiveData<>();
        db.collection("musics")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Music> musicList = queryDocumentSnapshots.toObjects(Music.class);
                        liveData.setValue(musicList);
                    } else {
                        liveData.setValue(null); // If no users found
                    }
                })
                .addOnFailureListener(e -> liveData.setValue(null));
        return liveData;
    }

    public LiveData<Music> getMusicById (String id) {
        MutableLiveData<Music> liveData = new MutableLiveData<>();

        db.collection("musics")
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Music music = documentSnapshot.toObject(Music.class);
                    liveData.setValue(music);
                })
                .addOnFailureListener(e -> liveData.setValue(null));
        return liveData;

    }

    public LiveData<List<Music>> getMusicByType (String type) {
        MutableLiveData<List<Music>> liveData = new MutableLiveData<>();
        db.collection("musics")
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Music> musics = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        musics.add(doc.toObject(Music.class));
                    }
                    liveData.setValue(musics);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));
        return liveData;

    }

    public LiveData<String> insertMusic(Music music) {
        MutableLiveData<String> result = new MutableLiveData<>();
        db.collection("musics")
                .add(music)
                .addOnSuccessListener(documentReference -> {
                    // Set the generated document ID as the result.
                    result.setValue(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductRepository", "Error inserting product", e);
                    result.setValue(null);
                });
        return result;
    }

    public LiveData<List<Music>> getMusicByCategoryId(String categoryId) {
        MutableLiveData<List<Music>> liveData = new MutableLiveData<>();
        db.collection("musics")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Music> musics = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        musics.add(doc.toObject(Music.class));
                    }
                    liveData.setValue(musics);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));
        return liveData;
    }


}

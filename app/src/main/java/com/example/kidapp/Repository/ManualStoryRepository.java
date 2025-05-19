package com.example.kidapp.Repository;

import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.models.ManualStory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ManualStoryRepository {
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;
    private static final String COLLECTION_MANUAL_STORIES = "manual_stories";

    public ManualStoryRepository() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return (currentUser != null) ? currentUser.getUid() : "anonymous";
    }

    private CollectionReference getStoryCollection() {
        return db.collection("users").document(getCurrentUserId()).collection(COLLECTION_MANUAL_STORIES);
    }

    public MutableLiveData<List<ManualStory>> getAllStories() {
        MutableLiveData<List<ManualStory>> storiesLiveData = new MutableLiveData<>();

        getStoryCollection()
                .orderBy("lastModifiedTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ManualStory> stories = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        ManualStory story = document.toObject(ManualStory.class);
                        if (story != null) {
                            story.setId(document.getId());
                            stories.add(story);
                        }
                    }
                    storiesLiveData.setValue(stories);
                })
                .addOnFailureListener(e -> storiesLiveData.setValue(new ArrayList<>()));

        return storiesLiveData;
    }

    public MutableLiveData<ManualStory> getStoryById(String storyId) {
        MutableLiveData<ManualStory> storyLiveData = new MutableLiveData<>();

        getStoryCollection().document(storyId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        ManualStory story = document.toObject(ManualStory.class);
                        if (story != null) {
                            story.setId(document.getId());
                            storyLiveData.setValue(story);
                        } else {
                            storyLiveData.setValue(null);
                        }
                    } else {
                        storyLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> storyLiveData.setValue(null));

        return storyLiveData;
    }

    public MutableLiveData<Boolean> saveStory(ManualStory story) {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();

        if (story.getId() == null || story.getId().isEmpty()) {
            // Tạo mới
            DocumentReference newStoryRef = getStoryCollection().document();
            story.setId(newStoryRef.getId());
            
            newStoryRef.set(story)
                    .addOnSuccessListener(aVoid -> resultLiveData.setValue(true))
                    .addOnFailureListener(e -> resultLiveData.setValue(false));
        } else {
            // Cập nhật
            getStoryCollection().document(story.getId())
                    .set(story)
                    .addOnSuccessListener(aVoid -> resultLiveData.setValue(true))
                    .addOnFailureListener(e -> resultLiveData.setValue(false));
        }

        return resultLiveData;
    }

    public MutableLiveData<Boolean> deleteStory(String storyId) {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();

        getStoryCollection().document(storyId)
                .delete()
                .addOnSuccessListener(aVoid -> resultLiveData.setValue(true))
                .addOnFailureListener(e -> resultLiveData.setValue(false));

        return resultLiveData;
    }

    public MutableLiveData<String> uploadImage(File imageFile) {
        MutableLiveData<String> urlLiveData = new MutableLiveData<>();

        try {
            // Sử dụng đường dẫn file local thay vì Storage URL
            String localPath = "file://" + imageFile.getAbsolutePath();
            
            // Trả về đường dẫn file local
            urlLiveData.setValue(localPath);
        } catch (Exception e) {
            urlLiveData.setValue("");
        }

        return urlLiveData;
    }
} 
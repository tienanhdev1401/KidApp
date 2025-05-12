package com.example.kidapp.Repository;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.models.Music;
import com.example.kidapp.models.Story;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StoryRepository {
    private final FirebaseFirestore db;

    public StoryRepository(Application application) {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Story>> getAllStories() {

        MutableLiveData<List<Story>> liveData = new MutableLiveData<>();
        db.collection("stories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Story> stories = queryDocumentSnapshots.toObjects(Story.class);
                        liveData.setValue(stories);
                    } else {
                        liveData.setValue(null); // If no users found
                }
                    })
                .addOnFailureListener(e -> liveData.setValue(null));

        return  liveData;
    }

    public LiveData<Story> getStoryById(String id) {
        MutableLiveData<Story> liveData = new MutableLiveData<>();

        db.collection("stories")
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Story story = documentSnapshot.toObject(Story.class);
                    liveData.setValue(story);
                })
                .addOnFailureListener(e -> liveData.setValue(null));
        return liveData;
    }

    public LiveData<String> insertStory(Story story) {
        MutableLiveData<String> result = new MutableLiveData<>();
        db.collection("stories")
                .add(story)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();  // Lấy ID được tạo từ Firestore
                    story.setStoryId(generatedId);  // Cập nhật ID vào đối tượng Product
                    // Set the generated document ID as the result.
                    result.setValue(generatedId);
                    db.collection("stories").document(generatedId)
                            .set(story)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Product created and ID updated successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating product ID", e));
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductRepository", "Error inserting product", e);
                    result.setValue(null);
                });
        return result;
    }

    public LiveData<List<Story>> getStoryByCategoryId(String categoryId) {
        MutableLiveData<List<Story>> liveData = new MutableLiveData<>();
        db.collection("stories")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Story> stories = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        stories.add(doc.toObject(Story.class));
                    }
                    liveData.setValue(stories);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));
        return liveData;
    }

}

package com.example.kidapp.Repository;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.models.Music;
import com.example.kidapp.models.Story;
import com.example.kidapp.models.StoryCategory;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class StoryCategoryRepository {
    private final FirebaseFirestore db;

    public StoryCategoryRepository(Application application) {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<StoryCategory>> getAllStoryCategories() {
        MutableLiveData<List<StoryCategory>> liveData = new MutableLiveData<>();
        db.collection("storyCategory")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<StoryCategory> categories = queryDocumentSnapshots.toObjects(StoryCategory.class);
                        liveData.setValue(categories);
                    } else {
                        liveData.setValue(null); // If no users found
                    }
                })
                .addOnFailureListener(e -> liveData.setValue(null));
        return liveData;
    }

    public LiveData<List<Story>> getStoryByCategoryName(String categoryName) {
        Log.d(TAG, "getStoryByCategoryName in repository: " + categoryName);
        MutableLiveData<List<Story>> liveData = new MutableLiveData<>();
        db.collection("storyCategory")
                .whereEqualTo("categoryName", categoryName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String id = queryDocumentSnapshots.getDocuments().get(0).getId();
                        Log.d(TAG, "getStoryByCategoryName in repository: " + id);
                        db.collection("stories")
                                .whereEqualTo("storyCategoryId", id)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    if (!queryDocumentSnapshots1.isEmpty()) {
                                        List<Story> story = queryDocumentSnapshots1.toObjects(Story.class);
                                        liveData.setValue(story);
                                    } else {
                                        liveData.setValue(null); // If no users found
                                    }
                                })
                                .addOnFailureListener(e -> liveData.setValue(null));
                    }

                });
        return liveData;
    }

    public LiveData<String> insertStoryCategory(StoryCategory storyCategory) {
        MutableLiveData<String> result = new MutableLiveData<>();
        db.collection("storyCategory")
                .add(storyCategory)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();  // Lấy ID được tạo từ Firestore
                    storyCategory.setCategoryId(generatedId);  // Cập nhật ID vào đối tượng Product
                    // Set the generated document ID as the result.
                    result.setValue(generatedId);
                    db.collection("storyCategory").document(generatedId)
                            .set(storyCategory)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Product created and ID updated successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating product ID", e));
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductRepository", "Error inserting product", e);
                    result.setValue(null);
                });
        return result;
    }
}

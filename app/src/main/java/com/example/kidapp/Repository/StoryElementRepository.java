package com.example.kidapp.Repository;

import android.util.Log;

import com.example.kidapp.models.StoryElement;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class StoryElementRepository {
    private static final String TAG = "StoryElementRepository";
    private final FirebaseFirestore firestore;
    private final String COLLECTION_NAME = "story_elements";

    public StoryElementRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        Log.d(TAG, "Repository initialized");
        countAllElements();
    }

    public void countAllElements() {
        Log.d(TAG, "Starting to count all elements...");
        firestore.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    Log.d(TAG, "Total elements count: " + count);
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        Log.d(TAG, String.format("Document ID: %s, Data: %s", 
                            doc.getId(), doc.getData()));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error counting elements: " + e.getMessage());
                });
    }

    public Task<List<StoryElement>> getElementsByType(StoryElement.ElementType type) {
        Log.d(TAG, "Loading elements of type: " + type.name());
        
        return firestore.collection(COLLECTION_NAME)
                .whereEqualTo("type", type.name())
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<StoryElement> elements = new ArrayList<>();
                        QuerySnapshot snapshot = task.getResult();
                        
                        Log.d(TAG, "Query successful, documents count: " + snapshot.size());
                        Log.d(TAG, "Query criteria - type: " + type.name());
                        
                        for (var doc : snapshot.getDocuments()) {
                            try {
                                Log.d(TAG, "Document ID: " + doc.getId() + ", data: " + doc.getData());
                                StoryElement element = new StoryElement();
                                element.setId(doc.getId());
                                element.setName(doc.getString("name"));
                                element.setImageUrl(doc.getString("imageUrl"));
                                element.setType(doc.getString("type"));
                                
                                elements.add(element);
                                Log.d(TAG, "Loaded element: " + element.getName() + ", type: " + element.getType());
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document: " + e.getMessage());
                            }
                        }
                        
                        Log.d(TAG, "Successfully loaded " + elements.size() + " elements");
                        return elements;
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "Failed to load elements: " + errorMessage);
                        return new ArrayList<>();
                    }
                });
    }

    public Task<Void> addElement(StoryElement element) {
        return firestore.collection(COLLECTION_NAME).add(element)
                .continueWith(task -> null);
    }

    public Task<Void> updateElement(StoryElement element) {
        return firestore.collection(COLLECTION_NAME)
                .document(element.getId())
                .set(element);
    }

    public Task<Void> deleteElement(String elementId) {
        return firestore.collection(COLLECTION_NAME)
                .document(elementId)
                .delete();
    }
} 
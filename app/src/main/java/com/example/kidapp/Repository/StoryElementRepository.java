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
                        
                        if (snapshot.isEmpty()) {
                            Log.w(TAG, "No documents found for type: " + type.name() + ". Adding sample data.");
                            // Thêm dữ liệu mẫu nếu không có dữ liệu từ Firebase
                            switch (type) {
                                case CHARACTER:
                                    elements.add(createSampleElement("Bé Na", "https://example.com/na.jpg", type));
                                    elements.add(createSampleElement("Bé Bin", "https://example.com/bin.jpg", type));
                                    elements.add(createSampleElement("Mèo Tom", "https://example.com/tom.jpg", type));
                                    break;
                                case SETTING:
                                    elements.add(createSampleElement("Trường học", "https://example.com/school.jpg", type));
                                    elements.add(createSampleElement("Rừng rậm", "https://example.com/forest.jpg", type));
                                    elements.add(createSampleElement("Bãi biển", "https://example.com/beach.jpg", type));
                                    break;
                                case ITEM:
                                    elements.add(createSampleElement("Bút chì", "https://example.com/pencil.jpg", type));
                                    elements.add(createSampleElement("Quả bóng", "https://example.com/ball.jpg", type));
                                    elements.add(createSampleElement("Đồ chơi", "https://example.com/toy.jpg", type));
                                    break;
                            }
                            Log.d(TAG, "Added " + elements.size() + " sample elements for type: " + type.name());
                            return elements;
                        }
                        
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
                                Log.e(TAG, "Error converting document: " + e.getMessage(), e);
                            }
                        }
                        
                        Log.d(TAG, "Successfully loaded " + elements.size() + " elements");
                        return elements;
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "Failed to load elements: " + errorMessage);
                        
                        // Trả về dữ liệu mẫu trong trường hợp lỗi
                        List<StoryElement> fallbackElements = new ArrayList<>();
                        Log.w(TAG, "Returning fallback sample data due to error");
                        switch (type) {
                            case CHARACTER:
                                fallbackElements.add(createSampleElement("Bé Na (Fallback)", "https://example.com/na.jpg", type));
                                fallbackElements.add(createSampleElement("Bé Bin (Fallback)", "https://example.com/bin.jpg", type));
                                break;
                            case SETTING:
                                fallbackElements.add(createSampleElement("Trường học (Fallback)", "https://example.com/school.jpg", type));
                                fallbackElements.add(createSampleElement("Rừng rậm (Fallback)", "https://example.com/forest.jpg", type));
                                break;
                            case ITEM:
                                fallbackElements.add(createSampleElement("Bút chì (Fallback)", "https://example.com/pencil.jpg", type));
                                fallbackElements.add(createSampleElement("Quả bóng (Fallback)", "https://example.com/ball.jpg", type));
                                break;
                        }
                        return fallbackElements;
                    }
                });
    }

    // Helper method to create sample elements
    private StoryElement createSampleElement(String name, String imageUrl, StoryElement.ElementType type) {
        StoryElement element = new StoryElement();
        element.setId("sample-" + type.name().toLowerCase() + "-" + name.toLowerCase().replace(" ", "-"));
        element.setName(name);
        element.setImageUrl(imageUrl);
        element.setType(type.name());
        return element;
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
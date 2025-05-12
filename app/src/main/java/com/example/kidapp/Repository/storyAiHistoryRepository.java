package com.example.kidapp.Repository;

import androidx.lifecycle.MutableLiveData;
import com.example.kidapp.models.storyAiHistoryModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class storyAiHistoryRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final String COLLECTION_STORIES = "storyAiHistory";
    private final String COLLECTION_SCENES = "scenes";

    public storyAiHistoryRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Lấy tham chiếu đến collection stories của user hiện tại
    private CollectionReference getStoriesCollection() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
        return db.collection("users").document(userId).collection(COLLECTION_STORIES);
    }

    // Lưu truyện mới vào Firestore
    public MutableLiveData<Boolean> saveStory(storyAiHistoryModel story) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        if (story.getId() == null || story.getId().isEmpty()) {
            story.setId(UUID.randomUUID().toString());
        }
        
        if (story.getCreatedAt() == null) {
            story.setCreatedAt(Timestamp.now());
        }
        
        DocumentReference storyRef = getStoriesCollection().document(story.getId());
        
        // Tạo bản sao của story không chứa scenes để lưu ở document chính
        storyAiHistoryModel storyWithoutScenes = new storyAiHistoryModel(
                story.getId(), story.getTitle(), story.getImageUrl(), 
                story.getSceneCount(), story.getCreatedAt()
        );
        
        storyRef.set(storyWithoutScenes)
                .addOnSuccessListener(aVoid -> {
                    // Lưu từng cảnh trong collection con
                    saveScenes(storyRef, story.getScenes(), result);
                })
                .addOnFailureListener(e -> {
                    result.setValue(false);
                });
        
        return result;
    }
    
    // Lưu các cảnh của truyện
    private void saveScenes(DocumentReference storyRef, List<storyAiHistoryModel.SceneModel> scenes, MutableLiveData<Boolean> result) {
        if (scenes == null || scenes.isEmpty()) {
            result.setValue(true);
            return;
        }
        
        CollectionReference scenesCollection = storyRef.collection(COLLECTION_SCENES);
        int[] savedCount = {0};
        int totalScenes = scenes.size();
        
        for (int i = 0; i < scenes.size(); i++) {
            final int index = i;
            storyAiHistoryModel.SceneModel scene = scenes.get(i);
            
            scenesCollection.document(String.valueOf(index))
                    .set(scene)
                    .addOnSuccessListener(aVoid -> {
                        savedCount[0]++;
                        if (savedCount[0] == totalScenes) {
                            result.setValue(true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        result.setValue(false);
                    });
        }
    }
    
    // Lấy danh sách tất cả truyện
    public MutableLiveData<List<storyAiHistoryModel>> getAllStories() {
        MutableLiveData<List<storyAiHistoryModel>> storiesLiveData = new MutableLiveData<>();
        
        getStoriesCollection()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<storyAiHistoryModel> stories = new ArrayList<>();
                    
                    for (var document : queryDocumentSnapshots.getDocuments()) {
                        storyAiHistoryModel story = document.toObject(storyAiHistoryModel.class);
                        stories.add(story);
                    }
                    
                    storiesLiveData.setValue(stories);
                })
                .addOnFailureListener(e -> {
                    storiesLiveData.setValue(new ArrayList<>());
                });
        
        return storiesLiveData;
    }
    
    // Lấy chi tiết truyện bao gồm các cảnh
    public MutableLiveData<storyAiHistoryModel> getStoryWithScenes(String storyId) {
        MutableLiveData<storyAiHistoryModel> storyLiveData = new MutableLiveData<>();
        
        getStoriesCollection().document(storyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    storyAiHistoryModel story = documentSnapshot.toObject(storyAiHistoryModel.class);
                    
                    if (story != null) {
                        // Lấy các cảnh
                        documentSnapshot.getReference().collection(COLLECTION_SCENES)
                                .orderBy(com.google.firebase.firestore.FieldPath.documentId())
                                .get()
                                .addOnSuccessListener(scenesSnapshot -> {
                                    List<storyAiHistoryModel.SceneModel> scenes = new ArrayList<>();
                                    
                                    for (var sceneDoc : scenesSnapshot.getDocuments()) {
                                        storyAiHistoryModel.SceneModel scene = sceneDoc.toObject(storyAiHistoryModel.SceneModel.class);
                                        scenes.add(scene);
                                    }
                                    
                                    story.setScenes(scenes);
                                    storyLiveData.setValue(story);
                                })
                                .addOnFailureListener(e -> {
                                    storyLiveData.setValue(story);
                                });
                    } else {
                        storyLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    storyLiveData.setValue(null);
                });
        
        return storyLiveData;
    }
    
    // Xóa truyện
    public MutableLiveData<Boolean> deleteStory(String storyId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        DocumentReference storyRef = getStoriesCollection().document(storyId);
        
        // Xóa tất cả các cảnh trước
        storyRef.collection(COLLECTION_SCENES)
                .get()
                .addOnSuccessListener(scenesSnapshot -> {
                    // Nếu không có cảnh nào, xóa document chính
                    if (scenesSnapshot.isEmpty()) {
                        deleteMainDocument(storyRef, result);
                        return;
                    }
                    
                    // Xóa từng cảnh
                    int[] deletedCount = {0};
                    int totalScenes = scenesSnapshot.size();
                    
                    for (var sceneDoc : scenesSnapshot.getDocuments()) {
                        sceneDoc.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    deletedCount[0]++;
                                    if (deletedCount[0] == totalScenes) {
                                        deleteMainDocument(storyRef, result);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    result.setValue(false);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    result.setValue(false);
                });
        
        return result;
    }
    
    private void deleteMainDocument(DocumentReference storyRef, MutableLiveData<Boolean> result) {
        storyRef.delete()
                .addOnSuccessListener(aVoid -> {
                    result.setValue(true);
                })
                .addOnFailureListener(e -> {
                    result.setValue(false);
                });
    }
} 
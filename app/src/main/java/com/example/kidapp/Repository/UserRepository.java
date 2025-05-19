package com.example.kidapp.Repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserRepository {
    private final FirebaseFirestore db;

    public UserRepository(Application application) {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> liveData = new MutableLiveData<>();
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<User> userList = queryDocumentSnapshots.toObjects(User.class);
                        liveData.setValue(userList);
                    } else {
                        liveData.setValue(null); // If no users found
                    }
                })
                .addOnFailureListener(e -> liveData.setValue(null));
        return liveData;
    }

    // Thêm phương thức getUserByEmail
    public LiveData<User> getUserByEmail(String email) {
        MutableLiveData<User> liveData = new MutableLiveData<>();
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        User user = queryDocumentSnapshots.getDocuments()
                                .get(0)
                                .toObject(User.class);
                        liveData.setValue(user);
                    } else {
                        liveData.setValue(null); // Không tìm thấy
                    }
                })
                .addOnFailureListener(e -> liveData.setValue(null));
        return liveData;
    }

    // Cập nhật tiến trình game cho user
    public void updateGameProgress(String userEmail, String gameKey, int newLevel, int newScore) {
        db.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    User user = documentSnapshot.toObject(User.class);
                    if (user.getGameProgress() == null) {
                        user.setGameProgress(new java.util.HashMap<>());
                    }
                    com.example.kidapp.models.GameProgress progress = user.getGameProgress().get(gameKey);
                    if (progress == null) {
                        progress = new com.example.kidapp.models.GameProgress();
                        progress.setScores(new java.util.HashMap<>());
                    }
                    // Cập nhật levelReached nếu level mới cao hơn
                    if (newLevel > progress.getLevelReached()) {
                        progress.setLevelReached(newLevel);
                    }
                    // Cập nhật điểm số level
                    progress.getScores().put(String.valueOf(newLevel), newScore);
                    // Lưu lại vào user
                    user.getGameProgress().put(gameKey, progress);
                    // Cập nhật Firestore
                    db.collection("users").document(documentSnapshot.getId())
                        .set(user);
                }
            });
    }

    // Cập nhật achievements cho user
    public void updateAchievements(String userEmail, java.util.List<String> achievements) {
        db.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                    db.collection("users")
                        .document(doc.getId())
                        .update("achievements", achievements);
                }
            });
    }

    // Thêm story đã đọc cho user
    public void addStoryRead(String userEmail, String storyId) {
        db.collection("users").document(userEmail)
            .update("storyIds", com.google.firebase.firestore.FieldValue.arrayUnion(storyId));
    }

    // Cập nhật tổng thời gian nghe cho user
    public void updateListeningTime(String email, int totalListeningTime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            // Lấy document ID
                            String docId = doc.getId();
                            // Update trường totalListeningTime
                            db.collection("users")
                                    .document(docId)
                                    .update("totalListeningTime", totalListeningTime)
                                    .addOnSuccessListener(aVoid -> Log.d("UserRepository", "Cập nhật thành công!"))
                                    .addOnFailureListener(e -> Log.e("UserRepository", "Lỗi cập nhật: " + e.getMessage()));
                        }
                    } else {
                        Log.e("UserRepository", "Không tìm thấy user với email: " + email);
                    }
                })
                .addOnFailureListener(e -> Log.e("UserRepository", "Lỗi truy vấn: " + e.getMessage()));
    }

    // Cập nhật thông tin cá nhân user
    public void updateUser(String email, String gender, String dateOfBirth, String phone, String avatarUrl) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                    db.collection("users")
                        .document(doc.getId())
                        .update("gender", gender,
                                "dateOfBirth", dateOfBirth,
                                "phone", phone,
                                "avatarUrl", avatarUrl);
                }
            });
    }

    public void addPuzzleScore(String userEmail, int score) {
        db.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    User user = documentSnapshot.toObject(User.class);
                    if (user.getGameProgress() == null) {
                        user.setGameProgress(new java.util.HashMap<>());
                    }
                    com.example.kidapp.models.GameProgress progress = user.getGameProgress().get("puzzle");
                    if (progress == null) {
                        progress = new com.example.kidapp.models.GameProgress();
                        progress.setScores(new java.util.HashMap<>());
                    }
                    java.util.Map<String, Integer> scores = progress.getScores();
                    if (scores == null) {
                        scores = new java.util.HashMap<>();
                    }
                    // Thêm entry mới với key là số thứ tự tiếp theo
                    String newKey = String.valueOf(scores.size() + 1);
                    scores.put(newKey, score);
                    progress.setScores(scores);
                    user.getGameProgress().put("puzzle", progress);
                    db.collection("users").document(documentSnapshot.getId())
                        .set(user);
                }
            });
    }

}

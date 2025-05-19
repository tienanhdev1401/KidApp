package com.example.kidapp.Repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.models.Music;
import com.example.kidapp.models.Story;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;


    public FavoriteRepository(Application application) {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }


    private String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return (currentUser != null) ? currentUser.getUid() : "anonymous";
    }
    public void toggleMusicFavorite(String userEmail, String musicId) {
        // Lấy document tương ứng trong subcollection "favorites" của user
        DocumentReference favoriteDocRef = db.collection("users")
                .document(getCurrentUserId())
                .collection("favorites")
                .document(musicId);

        favoriteDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Nếu sản phẩm đã là favorite, xoá nó đi
                        favoriteDocRef.delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d("FavoriteRepository", "Favorite removed successfully"))
                                .addOnFailureListener(e ->
                                        Log.e("FavoriteRepository", "Error removing favorite", e));
                    } else {
                        // Nếu sản phẩm chưa là favorite, thêm mới vào
                        Map<String, Object> data = new HashMap<>();
                        data.put("musicId", musicId);
                        data.put("dateAdded", new Date());

                        favoriteDocRef.set(data)
                                .addOnSuccessListener(aVoid ->
                                        Log.d("FavoriteRepository", "Favorite added successfully"))
                                .addOnFailureListener(e ->
                                        Log.e("FavoriteRepository", "Error adding favorite", e));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("FavoriteRepository", "Error toggling favorite", e));
    }

    public void toggleStoryFavorite(String userEmail, String storyId) {
        // Lấy document tương ứng trong subcollection "favorites" của user
        DocumentReference favoriteDocRef = db.collection("users")
                .document(getCurrentUserId())
                .collection("favorites")
                .document(storyId);

        favoriteDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Nếu sản phẩm đã là favorite, xoá nó đi
                        favoriteDocRef.delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d("FavoriteRepository", "Favorite removed successfully"))
                                .addOnFailureListener(e ->
                                        Log.e("FavoriteRepository", "Error removing favorite", e));
                    } else {
                        // Nếu sản phẩm chưa là favorite, thêm mới vào
                        Map<String, Object> data = new HashMap<>();
                        data.put("musicId", storyId);
                        data.put("dateAdded", new Date());

                        favoriteDocRef.set(data)
                                .addOnSuccessListener(aVoid ->
                                        Log.d("FavoriteRepository", "Favorite added successfully"))
                                .addOnFailureListener(e ->
                                        Log.e("FavoriteRepository", "Error adding favorite", e));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("FavoriteRepository", "Error toggling favorite", e));
    }


    public void removeMusicFavorite(String userEmail, String musicId) {
        db.collection("users")
                .document(getCurrentUserId())
                .collection("favorites")
                .document(musicId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("FavoriteRepository", "Favorite removed successfully"))
                .addOnFailureListener(e -> Log.e("FavoriteRepository", "Error removing favorite", e));
    }

    public void removeStoryFavorite(String userEmail, String storyId) {
        db.collection("users")
                .document(getCurrentUserId())
                .collection("favorites")
                .document(storyId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("FavoriteRepository", "Favorite removed successfully"))
                .addOnFailureListener(e -> Log.e("FavoriteRepository", "Error removing favorite", e));
    }


    public LiveData<Boolean> isMusicFavorite(String userEmail, String musicId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();

        db.collection("users")
                .document(getCurrentUserId())
                .collection("favorites")
                .document(musicId)
                .get()
                .addOnSuccessListener(documentSnapshot -> liveData.setValue(documentSnapshot.exists()))
                .addOnFailureListener(e -> liveData.setValue(false));

        return liveData;
    }

    public LiveData<Boolean> isStoryFavorite(String userEmail, String storyId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();

        db.collection("users")
                .document(getCurrentUserId())
                .collection("favorites")
                .document(storyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> liveData.setValue(documentSnapshot.exists()))
                .addOnFailureListener(e -> liveData.setValue(false));

        return liveData;
    }

    public LiveData<List<Music>>  getFavoriteMusic(String userEmail) {
        MutableLiveData<List<Music>> liveData = new MutableLiveData<>();
        db.collection("users")
                .document(getCurrentUserId())
                .collection("favorites")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> musicId = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        // Giả sử favorite document có trường "productId"
                        String prodId = doc.getString("musicId");
                        if (prodId != null) {
                            musicId.add(prodId);
                        }
                    }
                    if(musicId.isEmpty()){
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    // Truy vấn collection "products" để lấy thông tin sản phẩm dựa vào productIds
                    db.collection("musics")
                            .whereIn("musicId", musicId)
                            .get()
                            .addOnSuccessListener(productSnapshot -> {
                                List<Music> favorites = new ArrayList<>();
                                for (QueryDocumentSnapshot prodDoc : productSnapshot) {
                                    Music product = prodDoc.toObject(Music.class);
                                    favorites.add(product);
                                }
                                liveData.setValue(favorites);
                            })
                            .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));
        return liveData;
    }

    public LiveData<List<Story>>  getFavoriteStory(String userEmail) {
        MutableLiveData<List<Story>> liveData = new MutableLiveData<>();
        db.collection("users")
                .document(getCurrentUserId())
                .collection("favorites")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> storyId = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        // Giả sử favorite document có trường "productId"
                        String prodId = doc.getString("musicId");
                        if (prodId != null) {
                            storyId.add(prodId);
                        }
                    }
                    if(storyId.isEmpty()){
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    // Truy vấn collection "products" để lấy thông tin sản phẩm dựa vào productIds
                    db.collection("stories")
                            .whereIn("storyId", storyId)
                            .get()
                            .addOnSuccessListener(productSnapshot -> {
                                List<Story> favorites = new ArrayList<>();
                                for (QueryDocumentSnapshot prodDoc : productSnapshot) {
                                    Story story = prodDoc.toObject(Story.class);
                                    favorites.add(story);
                                }
                                liveData.setValue(favorites);
                            })
                            .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));
        return liveData;
    }
}

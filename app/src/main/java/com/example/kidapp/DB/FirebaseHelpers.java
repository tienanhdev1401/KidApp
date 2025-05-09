package com.example.kidapp.DB;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.kidapp.models.Music;
import com.example.kidapp.models.MusicCategory;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelpers {

    private static final String TAG = "FirebaseHelpers";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void insertMusicCategory(MusicCategory music) {
        db.collection("musicCategory")
                .add(music)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();  // Lấy ID được tạo từ Firestore
                    music.setCategoryId(generatedId);  // Cập nhật ID vào đối tượng Product

                    // Cập nhật lại đối tượng Product với ID mới
                    db.collection("musicCategory").document(generatedId)
                            .set(music)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Product created and ID updated successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating product ID", e));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating product", e);
                });
    }
    public static void insertMusic(Music music) {
        db.collection("musics")
                .add(music)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();  // Lấy ID được tạo từ Firestore
                    music.setMusicId(generatedId);  // Cập nhật ID vào đối tượng Product

                    // Cập nhật lại đối tượng Product với ID mới
                    db.collection("musics").document(generatedId)
                            .set(music)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Product created and ID updated successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating product ID", e));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating product", e);
                });
    }


    public static void createSampleMusicCategory () {
        MusicCategory musicCategory = new MusicCategory(
                "Lullabies",
                "android.resource://com.example.kidapp/drawable/lullaby"
        );

        insertMusicCategory(musicCategory);
        String id = musicCategory.getCategoryId();

        Music music1 = new Music(
                "Twinkle Twinkle Little Star",
                "android.resource://com.example.kidapp/drawable/twinkle_star",
                "https://www.youtube.com/watch?v=hqzvHfy-Ij0",
                "Nursery Rhymes",
                "https://i.pinimg.com/736x/55/d2/4b/55d24b2d9b7b56a7c3e959b1303a152e.jpg",
                "Popular",
                id
        );

        insertMusic(music1);


        Music music2 = new Music(
                "Wheels on the Bus",
                "android.resource://com.example.kidapp/drawable/wheels_on_bus",
                "https://www.youtube.com/watch?v=XqZsoesa55w",
                "PINKFONG Songs for Children",
                "android.resource://com.example.kidapp/drawable/wheels_on_bus",
                "Popular",
                "uTX03xofOVEJDULFGhhC"
        );

        insertMusic(music2);
    }


}

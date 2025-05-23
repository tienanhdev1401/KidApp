package com.example.kidapp.DB;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.ViewModel.StoryCategoryViewModel;
import com.example.kidapp.models.Music;
import com.example.kidapp.models.MusicCategory;
import com.example.kidapp.models.Story;
import com.example.kidapp.models.StoryCategory;
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

    public static void insertStoryCategory(StoryCategory storyCategory) {

        db.collection("storyCategory")
                .add(storyCategory)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();  // Lấy ID được tạo từ Firestore
                    storyCategory.setCategoryId(generatedId);  // Cập nhật ID vào đối tượng Product
                    // Set the generated document ID as the result.
                    db.collection("storyCategory").document(generatedId)
                            .set(storyCategory)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Product created and ID updated successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating product ID", e));
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductRepository", "Error inserting product", e);
                });

    }

    public static void insertStory(Story story) {
        db.collection("stories")
                .add(story)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();  // Lấy ID được tạo từ Firestore
                    story.setStoryId(generatedId);  // Cập nhật ID vào đối tượng Product
                    // Set the generated document ID as the result.

                    db.collection("stories").document(generatedId)
                            .set(story)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Product created and ID updated successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating product ID", e));
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductRepository", "Error inserting product", e);

                });

    }


    public static void createSampleMusicCategory () {


           Story story2 = new Story( "s6M1cckdhy2uVFx174sV",
                   "The Three Little Pigs",
                   "Once upon a time, there were three little pigs who set out to build their own houses. The first pig built a house of straw, the second a house of sticks, and the third a house of bricks. \n" +
                           "\n" +
                           "One day, a big bad wolf came. He blew down the straw house and then the stick house. But no matter how hard he tried, he could not blow down the brick house.\n" +
                           "\n" +
                           "The third pig had worked hard and smart, and his strong house kept all three pigs safe from the wolf.",
                   "Hard work and wise planning lead to safety and success. Quick and easy solutions may not be strong enough when challenges come.",
                   "https://res.cloudinary.com/dnfegdcb9/image/upload/v1746985714/three_little_pigs_example.png",
                   "https://res.cloudinary.com/dnfegdcb9/video/upload/v1746984936/Three_Little_Pigs_Story_Example.mp4",
                   "Why couldn't the wolf blow down the third pig's house?",
                   "It was protected by magic",
                   "It was hidden in the forest",
                   "It was made of bricks",
                   "It was made of bricks");

           insertStory(story2);


    }


}

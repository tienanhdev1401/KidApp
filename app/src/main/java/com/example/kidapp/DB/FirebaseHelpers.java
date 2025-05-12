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
//        MusicCategory musicCategory = new MusicCategory(
//                "Lullabies",
//                "android.resource://com.example.kidapp/drawable/lullaby"
//        );
//
//        insertMusicCategory(musicCategory);
//        String id = musicCategory.getCategoryId();
//
//        Music music1 = new Music(
//                "Twinkle Twinkle Little Star",
//                "android.resource://com.example.kidapp/drawable/twinkle_star",
//                "https://www.youtube.com/watch?v=hqzvHfy-Ij0",
//                "Nursery Rhymes",
//                "https://i.pinimg.com/736x/55/d2/4b/55d24b2d9b7b56a7c3e959b1303a152e.jpg",
//                "Popular",
//                id
//        );
//
//        insertMusic(music1);
//
//
//        Music music2 = new Music(
//                "Wheels on the Bus",
//                "android.resource://com.example.kidapp/drawable/wheels_on_bus",
//                "https://www.youtube.com/watch?v=XqZsoesa55w",
//                "PINKFONG Songs for Children",
//                "android.resource://com.example.kidapp/drawable/wheels_on_bus",
//                "Popular",
//                "uTX03xofOVEJDULFGhhC"
//        );
//
//        insertMusic(music2);

//                StoryCategory musicCategory = new StoryCategory(
//                "Adventure",
//                "https://res.cloudinary.com/dnfegdcb9/image/upload/v1746903151/adventure_ljozoy.png"
//        );
//
//        StoryCategory storyCategory = new StoryCategory(
//                "Fairy Tales",
//                "https://res.cloudinary.com/dnfegdcb9/image/upload/v1746903094/fairy_tale_icon_zyhgzj.png"
//        );
//
//        StoryCategory storyCategory1 = new StoryCategory(
//                "Animals",
//                "https://res.cloudinary.com/dnfegdcb9/image/upload/v1746903042/animals_icon_dfnq60.png"
//        );
//        StoryCategory storyCategory2 = new StoryCategory(
//                "Moral",
//                "https://res.cloudinary.com/dnfegdcb9/image/upload/v1746903136/ethics_n1jtd3.png"
//        );
//
//
//        insertStoryCategory(musicCategory);
//        insertStoryCategory(storyCategory);
//        insertStoryCategory(storyCategory1);
//        insertStoryCategory(storyCategory2);


            Story story = new Story("5enOcEHGsZ86wyx9tezN","Cinderela","Once upon a time, there was a kind girl named Cinderella who lived with her wicked stepmother and stepsisters. They treated her poorly, making her work all day. One day, the King invited all young ladies to a royal ball. With the help of her fairy godmother, Cinderella attended the ball in a magical gown and glass slippers. She danced with the Prince but had to leave at midnight, losing one glass slipper. The Prince searched the kingdom to find the slipper's owner, and when it fit Cinderella, they married and lived happily ever after.","","https://res.cloudinary.com/dnfegdcb9/image/upload/c_thumb,w_200,g_face/v1746985710/cinderella_vn2nku.png","https://res.cloudinary.com/dnfegdcb9/video/upload/v1746984180/Cinderella_Fairy_Tales_Gigglebox_kg0phi.mp4","What did Cinderella lose at the ball?","Glass slipper","A necklace","Glass slipper","A ring");
            insertStory(story);

            Story story1 = new Story("hdcGJ8UU0ehIK9bTvhJO","Pinocchio","Once upon a time, a poor woodcarver named Geppetto created a wooden puppet named Pinocchio. A fairy brought Pinocchio to life, telling him he could become a real boy if he proved himself brave, truthful, and unselfish. \n" +
                    "\n" +
                    "Pinocchio struggled with obedience - his nose grew longer whenever he lied. He was tricked by villains, skipped school, and ended up in dangerous situations. After being swallowed by a giant whale while searching for Geppetto, Pinocchio finally learned responsibility. He rescued his father, showed courage, and transformed into a real human boy through his good deeds.",
                    "Honesty is the best policy. Good behavior and truthfulness lead to positive outcomes, while lies and disobedience bring consequences. Every choice shapes who we become.",
                    "https://res.cloudinary.com/dnfegdcb9/image/upload/w_1000,ar_1:1,c_fill,g_auto,e_art:hokusai/v1746985710/pinocchio_frmyi1.png",
                    "https://res.cloudinary.com/dnfegdcb9/video/upload/v1746984788/Pinocchio_Fairy_Tales_and_Bedtime_Stories_for_Kids_Adventure_Story_zhcprd.mp4",
                    "What happens when Pinocchio tells a lie?","His nose grows longer","His ears turn red","His nose grows longer","He turns back to wood");
           insertStory(story1);

           Story story2 = new Story("s6M1cckdhy2uVFx174sV","The Lion and the Mouse","One day, a mighty lion was sleeping in the forest when a tiny mouse accidentally ran over his paw. The lion woke up angrily and caught the mouse. \n" +
                   "\n" +
                   "'Please spare me!' begged the mouse. 'I promise to help you someday!'\n" +
                   "\n" +
                   "The lion laughed at the idea but let the mouse go. Days later, hunters trapped the lion in a net. Hearing his roars, the mouse came and gnawed through the ropes. \n" +
                   "\n" +
                   "'You laughed when I said I would repay you,' said the mouse. 'Now you see even a mouse can help a lion!'",
                   "No act of kindness is ever wasted. Everyone has value regardless of size. Help comes from unexpected places when we show compassion",
                   "https://res.cloudinary.com/dnfegdcb9/image/upload/v1746985714/the_lion_and_the_mouse_i2ines.png",
                   "https://res.cloudinary.com/dnfegdcb9/video/upload/v1746984936/The_Lion_and_the_Mouse_in_English_EnglishFairyTales_a8aedv.mp4",
                   "How did the mouse help the lion?","Fought the hunters","Brought him food","Gnawed through the net","Fought the hunters");

           insertStory(story2);

           Story story3 = new Story("v8vbNMvVMQ3Zj3zcCpF2","The Ant and the Grasshopper","\"One hot summer day, when the sun was shining brightly, a red-whiskered bulbul was hopping around the fields, singing happily and enjoying the relaxing moments. Meanwhile, a small ant was working hard, gently pushing each grain of food back to its nest to prepare for the coming winter.\\n\\nThe red-whiskered bulbul saw the busy ant and did not understand why it did not stop to play with him. So the red-whiskered bulbul immediately complained to himself:\\n\\n“Why don’t you stop and play with me? Enjoy this summer!”\\n\\nBut the ant persistently replied:\\n\\n“I am storing food for the winter. You should enjoy now, but remember that tomorrow is never known!”\\n\\nThe red-whiskered bulbul just sneered and thought that winter was still far away, so there was no need to worry. Time passed, and the cold winter finally came. The fields suddenly became bare, food was scarce, and the whole nature was covered in ice.\\n\\nHungry and cold, the red-whiskered bulbul was forced to remember the ant's advice. He went to the ant's nest to ask for some food. Although busy with his own work, the ant still shared the food with the red-whiskered bulbul, at the same time reminding him:\\n\\n“If you have listened to me, then you should spend your summer working to prepare for winter. Remember that today's hard work will help you overcome the difficulties of tomorrow.”\" ",
                   "Prepare for the future today. Hard work, discipline and knowing how to orient your work while you still have the means will help you overcome difficult times later.",
                   "https://res.cloudinary.com/dnfegdcb9/image/upload/v1746985714/the_ant_and_the_grasshopper_l6hkb2.png",
                   "https://res.cloudinary.com/dnfegdcb9/video/upload/v1746983139/the_ant_and_the_grasshopper_short_firm_fvlgap.mp4",
                    "What did the ant do in the summer?","Food storage","Play!","sleep all day","Food storage");

        insertStory(story3);
        Story story4 = new Story("v8vbNMvVMQ3Zj3zcCpF2","The Tortoise and the Hare","A boastful hare challenged a slow-moving tortoise to a race. Confident in his speed, the hare raced ahead and soon left the tortoise far behind. Deciding to take a nap mid-race, the hare slept under a tree while the tortoise plodded steadily forward.\n" +
                "\n" +
                "When the hare finally woke up, he dashed to the finish line only to find the tortoise had already won the race. The moral? Slow and steady wins the race!",
                "Consistency beats overconfidence. Success comes through persistent effort rather than relying solely on natural talent. Never underestimate your opponent.",
                "https://res.cloudinary.com/dnfegdcb9/image/upload/v1746985711/story_tortoise_hare_xpthwc.png",
                "https://res.cloudinary.com/dnfegdcb9/video/upload/v1746983864/Aesop_s_Fables_The_Tortoise_and_the_Hare_Short_Film_kzpgak.mp4",
                "Why did the tortoise win the race?","Steady persistence","The hare got lost","Steady persistence","The tortoise was faster");

        insertStory(story4);

    }


}

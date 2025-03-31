package com.example.kidapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private RecyclerView vocabularyRecyclerView;
    private RecyclerView readingRecyclerView;
    private SkillAdapter vocabularyAdapter;
    private SkillAdapter readingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Nút quay lại
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Nút cài đặt
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MathQuizActivity.class);
            startActivity(intent);
        });

        // Khởi tạo RecyclerView cho Vocabulary Skills
        vocabularyRecyclerView = findViewById(R.id.vocabulary_recycler);
        vocabularyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<SkillItem> vocabularyList = new ArrayList<>();
        vocabularyList.add(new SkillItem("Word 1", R.drawable.word1_image));
        vocabularyList.add(new SkillItem("Word 2", R.drawable.word2_image));
        vocabularyList.add(new SkillItem("Word 3", R.drawable.word3_image));

        vocabularyAdapter = new SkillAdapter(vocabularyList);
        vocabularyRecyclerView.setAdapter(vocabularyAdapter);

        // Khởi tạo RecyclerView cho Reading Skills
        readingRecyclerView = findViewById(R.id.reading_recycler);
        readingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<SkillItem> readingList = new ArrayList<>();
        readingList.add(new SkillItem("Passage 1", R.drawable.passage1_image));
        readingList.add(new SkillItem("Passage 2", R.drawable.passage2_image));
        readingList.add(new SkillItem("Passage 3", R.drawable.passage3_image));

        readingAdapter = new SkillAdapter(readingList);
        readingRecyclerView.setAdapter(readingAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.Adapter.GuessWordLevelAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.WordGuessLevelViewModel;
import com.example.kidapp.models.WordGuessLevel;
import com.example.kidapp.models.WordGuessStage;

public class GuessWordLevelListActivity extends AppCompatActivity implements GuessWordLevelAdapter.OnLevelClickListener {

    private WordGuessLevelViewModel viewModel;
    private GuessWordLevelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_list);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewLevels);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuessWordLevelAdapter(this);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(WordGuessLevelViewModel.class);
        viewModel.getAllLevels().observe(this, levels -> {
            if (levels != null) {
                for (WordGuessLevel level : levels) {
                    android.util.Log.d("GuessWordLevelListActivity", "Level: id=" + level.getId() + ", name=" + level.getName() + ", title=" + level.getTitle());
                    if (level.getStages() != null) {
                        for (WordGuessStage stage : level.getStages()) {
                            android.util.Log.d("GuessWordLevelListActivity", "   Stage: id=" + stage.getId() + ", answer=" + stage.getAnswer() + ", imageUrl=" + stage.getImageUrl() + ", suggestion=" + stage.getSuggestion());
                        }
                    }
                }
                adapter.setLevels(levels);
            }
        });
    }

    @Override
    public void onLevelClick(WordGuessLevel level) {
        Intent intent = new Intent(this, GameDoanChuActivity.class);
        intent.putExtra("level", level);
        startActivity(intent);
    }
} 
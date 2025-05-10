package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.FlipCardLevelViewModel;
import com.example.kidapp.models.FlipCardLevel;
import com.example.kidapp.Adapter.FlipCardLevelAdapter;

public class FlipCardLevelListActivity extends AppCompatActivity implements FlipCardLevelAdapter.OnLevelClickListener {

    private FlipCardLevelViewModel viewModel;
    private FlipCardLevelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filp_card_level_list);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewLevels);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FlipCardLevelAdapter(this);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(FlipCardLevelViewModel.class);
        viewModel.getAllLevels().observe(this, levels -> {
            if (levels != null) {
                for (FlipCardLevel level : levels) {
                    android.util.Log.d("FlipCardLevelListActivity", "Level: id=" + level.getId() + ", topic=" + level.getTopic() + ", cards=" + (level.getCards() != null ? level.getCards().size() : 0));
                    if (level.getCards() != null) {
                        for (com.example.kidapp.models.FlipCard card : level.getCards()) {
                            android.util.Log.d("FlipCardLevelListActivity", "   Card: text=" + card.getCardText() + ", imageUrl=" + card.getCardImageUrl());
                        }
                    }
                }
                adapter.setLevelList(levels);
            }
        });
    }

    @Override
    public void onLevelClick(FlipCardLevel level) {
        Intent intent = new Intent(this, GameLatTheActivity.class);
        intent.putExtra("level", level); // FlipCardLevel phải implements Serializable
        startActivity(intent);
    }
}
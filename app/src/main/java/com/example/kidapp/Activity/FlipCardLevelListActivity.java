package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.FlipCardLevelViewModel;
import com.example.kidapp.ViewModel.UserViewModel;
import com.example.kidapp.models.FlipCardLevel;
import com.example.kidapp.Adapter.FlipCardLevelAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FlipCardLevelListActivity extends AppCompatActivity implements FlipCardLevelAdapter.OnLevelClickListener {

    private FlipCardLevelViewModel viewModel;
    private FlipCardLevelAdapter adapter;
    private UserViewModel userViewModel;
    private int levelReached = 0;
    private String userEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filp_card_level_list);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewLevels);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FlipCardLevelAdapter(this);
        recyclerView.setAdapter(adapter);

        // Lấy email user hiện tại từ FirebaseAuth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        }

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        viewModel = new ViewModelProvider(this).get(FlipCardLevelViewModel.class);

        // Lấy levelReached của user
        if (userEmail != null) {
            userViewModel.getUserByEmail(userEmail).observe(this, user -> {
                if (user != null && user.getGameProgress() != null && user.getGameProgress().containsKey("flipcard")) {
                    levelReached = user.getGameProgress().get("flipcard").getLevelReached();
                } else {
                    levelReached = 0;
                }
                adapter.setLevelReached(levelReached);
            });
        } else {
            adapter.setLevelReached(0);
        }

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
package com.example.kidapp.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kidapp.Adapter.FavoriteViewPagerAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.FavoriteViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FavoriteActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FavoriteViewModel favoriteViewModel;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorite);

        // Get current user email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        } else {
            // Handle case when user is not logged in
            finish();
            return;
        }

        // Initialize ViewModel
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        
        // Set up back button
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Set up ViewPager with adapter
        FavoriteViewPagerAdapter pagerAdapter = new FavoriteViewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Music");
                    tab.setIcon(R.drawable.listen);
                    break;
                case 1:
                    tab.setText("Stories");
                    tab.setIcon(R.drawable.lullaby);
                    break;
            }
        }).attach();
    }
}
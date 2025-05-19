package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kidapp.Fragment.AIStoryFragment;
import com.example.kidapp.Fragment.ManualStoryFragment;
import com.example.kidapp.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class StoryHistoryActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ExtendedFloatingActionButton fabCreateStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_history);
        
        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Ánh xạ các view
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        fabCreateStory = findViewById(R.id.fabCreateStory);

        // Thiết lập ViewPager2 với adapter
        viewPager.setAdapter(new StoryPagerAdapter(this));

        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Truyện AI");
            } else {
                tab.setText("Truyện thủ công");
            }
        }).attach();

        // Nút tạo truyện mới
        fabCreateStory.setOnClickListener(v -> {
            // Chuyển đến màn hình tạo truyện mới
            navigateToStoryCreation();
        });
    }

    // Adapter cho ViewPager2
    private static class StoryPagerAdapter extends FragmentStateAdapter {
        public StoryPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Trả về fragment tương ứng với position
            if (position == 0) {
                return new AIStoryFragment();
            } else {
                return new ManualStoryFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Có 2 tab: Truyện AI và Truyện thủ công
        }
    }

    private void navigateToStoryCreation() {
        // Chuyển đến màn hình tạo truyện mới
        Intent intent = new Intent(this, StoryCreationActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
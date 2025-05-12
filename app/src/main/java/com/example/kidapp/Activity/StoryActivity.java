package com.example.kidapp.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kidapp.Adapter.ImageSliderAdapter;
import com.example.kidapp.Adapter.StoryAdapter;
import com.example.kidapp.R;
import com.example.kidapp.Service.StoryService;
import com.example.kidapp.ViewModel.StoryViewModel;
import com.example.kidapp.models.Music;
import com.example.kidapp.models.Story;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class StoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private StoryAdapter storyAdapter;
    private List<Story> storyList = new ArrayList<>();
    private List<Story> allStoryList = new ArrayList<>();
    private StoryViewModel storyViewModel;
    private CardView playerCard;
    private ImageView playPauseButton, expandButton;
    private TextView storyTitle, storyDuration;
    private StoryService storyService;
    private boolean isServiceBound = false;

    // Service Connection để kết nối với StoryService
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StoryService.StoryBinder binder = (StoryService.StoryBinder) service;
            storyService = binder.getService();
            isServiceBound = true;
            
            // Cập nhật UI khi kết nối thành công
            updatePlayerUI();
            
            // Đăng ký listener từ service
            storyService.setUpdateListener(new StoryService.StoryUpdateListener() {
                @Override
                public void onStoryUpdate() {
                    updatePlayerUI();
                }
                
                @Override
                public void onProgressUpdate(int currentPosition, int duration) {
                    updateProgressUI(currentPosition, duration);
                }
            });
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            storyService = null;
            isServiceBound = false;
        }
    };
    
    // BroadcastReceiver để nhận các cập nhật từ StoryService
    private BroadcastReceiver storyUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            
            switch (action) {
                case "STORY_UPDATE":
                case "STORY_PLAY_STATUS":
                case "STORY_CHANGED":
                    updatePlayerUI();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_story);
        storyViewModel = new ViewModelProvider(this).get(StoryViewModel.class);
        loadStoryFromDB();
        setupImageSlider();
        setView();
        loadPopularStories();
        setBtnControl();
        
        // Kết nối với StoryService
        bindToStoryService();
    }
    
    private void bindToStoryService() {
        Intent serviceIntent = new Intent(this, StoryService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("STORY_UPDATE");
        filter.addAction("STORY_CHANGED");
        filter.addAction("STORY_PLAY_STATUS");
        registerReceiver(storyUpdateReceiver, filter, Context.RECEIVER_EXPORTED);
        
        // Cập nhật UI ngay khi resume Activity
        updatePlayerUI();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(storyUpdateReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver chưa được đăng ký
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    private void loadPopularStories() {
        RecyclerView recyclerViewNewItems = findViewById(R.id.popular_stories_view);
        LinearLayoutManager newItemsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewNewItems.setLayoutManager(newItemsLayoutManager);
        storyAdapter = new StoryAdapter(this, storyList, this);
        recyclerViewNewItems.setAdapter(storyAdapter);
        recyclerViewNewItems.setHasFixedSize(true);
    }

    private void loadStoryFromDB() {
        storyViewModel.getAllStories().observe(this, stories -> {
            if (stories != null && !stories.isEmpty()) {
                Log.d( "loadStoryFromDB: ", stories.toString());
               List<Story> limitedStories = stories.size() > 3 ? stories.subList(0, 3) : stories;
                storyList.clear();
                storyList.addAll(limitedStories);
                storyAdapter.notifyDataSetChanged();
            }
        });

        storyViewModel.getAllStories().observe(this, stories -> {
            if (stories != null && !stories.isEmpty()) {
                allStoryList.clear();
                allStoryList.addAll(stories);
                storyAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setBtnControl() {
        btnBack.setOnClickListener(v -> finish());

        storyAdapter.setOnItemClickListener((position, story) -> {
            Intent intent = new Intent(StoryActivity.this, StoryDetailActivity.class);
            intent.putExtra("story", story);
            intent.putParcelableArrayListExtra("playlist", new ArrayList<>(allStoryList));
            intent.putExtra("storyPosition", position);
            startActivity(intent);
        });
        
        // Thiết lập điều khiển player
        setupPlayerControls();
    }
    
    private void setupPlayerControls() {
        // Play/Pause button
        playPauseButton.setOnClickListener(v -> {
            if (isServiceBound && storyService != null) {
                if (storyService.isPlaying()) {
                    storyService.pauseVideo();
                } else {
                    storyService.resumeVideo();
                }
                // Cập nhật UI ngay lập tức
                updatePlayerUI();
            }
        });
        
        // Expand button để mở màn hình chi tiết
        expandButton.setOnClickListener(v -> {
            if (isServiceBound && storyService != null) {
                try {
                    // Lấy thông tin mới nhất từ Service
                    List<Story> currentPlaylist = storyService.getPlaylist();
                    Story currentStory = storyService.getCurrentStory();

                    // Kiểm tra dữ liệu hợp lệ
                    if (currentPlaylist == null || currentPlaylist.isEmpty() || currentStory == null) {
                        Log.e("StoryActivity", "Không thể mở player chi tiết: playlist rỗng hoặc story null");
                        return;
                    }

                    // Tìm vị trí chính xác của story hiện tại trong playlist
                    int currentPosition = storyService.getCurrentPosition();
                    
                    // Đảm bảo position hợp lệ
                    if (currentPosition < 0 || currentPosition >= currentPlaylist.size()) {
                        currentPosition = 0;
                    }

                    Intent intent = new Intent(StoryActivity.this, StoryDetailActivity.class);
                    intent.putParcelableArrayListExtra("playlist", new ArrayList<>(currentPlaylist));
                    intent.putExtra("storyPosition", currentPosition);
                    intent.putExtra("story", currentStory);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("StoryActivity", "Lỗi khi mở StoryDetailActivity: " + e.getMessage());
                    Toast.makeText(this, "Không thể mở chi tiết truyện", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Không có truyện đang phát", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setView() {
        btnBack = findViewById(R.id.backButton);
        playerCard = findViewById(R.id.playerCard);
        playPauseButton = findViewById(R.id.playPauseBtn);
        expandButton = findViewById(R.id.expandBtn);
        storyTitle = findViewById(R.id.storyTitle);
        storyDuration = findViewById(R.id.storyDuration);
        
        // Ẩn playerCard ban đầu cho đến khi có story chạy
        playerCard.setVisibility(View.GONE);
    }
    
    private void updatePlayerUI() {
        runOnUiThread(() -> {
            try {
                if (isServiceBound && storyService != null && storyService.getCurrentStory() != null) {
                    Story currentStory = storyService.getCurrentStory();
                    
                    // Hiển thị tên story
                    storyTitle.setText(currentStory.getStoryTitle());
                    
                    // Cập nhật icon play/pause
                    playPauseButton.setImageResource(
                            storyService.isPlaying() 
                                ? R.drawable.notification_pause 
                                : R.drawable.detail_music_play
                    );
                    
                    // Hiển thị playerCard
                    playerCard.setVisibility(View.VISIBLE);
                } else {
                    // Ẩn playerCard nếu không có story nào đang chạy
                    playerCard.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.e("StoryActivity", "Lỗi cập nhật UI: " + e.getMessage());
            }
        });
    }
    
    private void updateProgressUI(int currentPosition, int duration) {
        runOnUiThread(() -> {
            if (duration > 0) {
                String formattedCurrentTime = formatTime(currentPosition);
                String formattedTotalTime = formatTime(duration);
                storyDuration.setText(formattedCurrentTime + " / " + formattedTotalTime);
            }
        });
    }
    
    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void setupImageSlider() {
        // Khởi tạo danh sách ảnh
        List<Integer> sliderImages = Arrays.asList(
                R.drawable.story_tortoise_hare,
                R.drawable.the_lion_and_the_mouse,
                R.drawable.the_ant_and_the_grasshopper
        );

        // Ánh xạ ViewPager2 và Indicator
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        CircleIndicator3 indicator = findViewById(R.id.indicator);

        // Thiết lập Adapter
        ImageSliderAdapter adapter = new ImageSliderAdapter(this, sliderImages);
        viewPager.setAdapter(adapter);

        // Liên kết Indicator với ViewPager
        indicator.setViewPager(viewPager);

        // Thêm hiệu ứng chuyển trang
        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setAlpha(0.5f);
                page.setScaleY(0.9f);

                if (position < -1 || position > 1) {
                    page.setAlpha(0f);
                } else {
                    page.setAlpha(1 - Math.abs(position));
                    page.setScaleY(Math.max(0.9f, 1 - Math.abs(position)));
                }
            }
        });
    }
}
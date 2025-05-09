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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kidapp.Adapter.ImageSliderAdapter;
import com.example.kidapp.Adapter.MusicAdapter;
import com.example.kidapp.R;
import com.example.kidapp.Service.MusicService;
import com.example.kidapp.ViewModel.MusicViewModel;
import com.example.kidapp.models.Music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class MusicActivity extends AppCompatActivity {

    private MusicAdapter musicAdapter;
    private List<Music> musicList = new ArrayList<>();

    private MusicViewModel musicViewModel;
    private CardView playerCard;
    private ImageView playPauseButton, previousButton, nextButton, expandButton;
    private TextView songTitle, artistName;
    private MusicService musicService;
    private ServiceConnection serviceConnection;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_music);
        setupImageSlider();
        setView();

        musicViewModel = new ViewModelProvider(this).get(MusicViewModel.class);
        btnBack.setOnClickListener(v -> finish());
        loadMusicFromDB();
        loadPopularMusics();

        Intent serviceIntent = new Intent(this, MusicService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Xử lý sự kiện nút
        setupPlayerControls();

        musicAdapter.setOnItemClickListener((position, music) -> {
            Log.d("MusicActivity", "Playlist sent: " + musicList.size());
            Intent intent = new Intent(MusicActivity.this, MusicDetailActivity.class);
            intent.putParcelableArrayListExtra("playlist", new ArrayList<>(musicList));
            intent.putExtra("musicPosition", position);
            intent.putExtra("music", music);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupPlayerControls() {
        // Play/Pause
        playPauseButton.setOnClickListener(v -> {
            if (musicService != null) {
                if (musicService.isPlaying()) {
                    musicService.pauseMusic();
                } else {
                    musicService.resumeMusic();
                }
                // Cập nhật UI ngay lập tức
                updatePlayerUI();
            }
        });

        // Previous
        previousButton.setOnClickListener(v -> {
            if(musicService != null) {
                musicService.playPrevious();
            }
        });

        // Next
        nextButton.setOnClickListener(v -> {
            if(musicService != null) {
                musicService.playNext();
            }
        });

        // Mở màn hình chi tiết
        expandButton.setOnClickListener(v -> {
            if (musicService != null) {
                // Lấy thông tin mới nhất từ Service
                List<Music> currentPlaylist = musicService.getPlaylist();
                Music currentMusic = musicService.getCurrentMusic();

                // Kiểm tra dữ liệu hợp lệ
                if (currentPlaylist == null || currentMusic == null) {
                    Log.e("MusicActivity", "Không thể mở player chi tiết: playlist hoặc music null");
                    return;
                }

                // Tìm vị trí chính xác của bài hát hiện tại trong playlist
                int currentPosition = -1;
                for (int i = 0; i < currentPlaylist.size(); i++) {
                    Music music = currentPlaylist.get(i);
                    if (music.getMusicUrl() != null && currentMusic.getMusicUrl() != null &&
                        music.getMusicUrl().equals(currentMusic.getMusicUrl())) {
                        currentPosition = i;
                        break;
                    }
                }

                if (currentPosition == -1) {
                    Log.e("MusicActivity", "Không tìm thấy vị trí bài hát trong playlist");
                    // Fallback: Sử dụng currentPosition từ service
                    currentPosition = musicService.getCurrentPosition();
                }

                Log.d("MusicActivity", "Mở chi tiết với bài hát: " + currentMusic.getMusicName() + 
                                        ", position: " + currentPosition + 
                                        ", playlist size: " + currentPlaylist.size());

                Intent intent = new Intent(MusicActivity.this, MusicDetailActivity.class);
                intent.putParcelableArrayListExtra("playlist", new ArrayList<>(currentPlaylist));
                intent.putExtra("musicPosition", currentPosition);
                intent.putExtra("music", currentMusic);
                startActivity(intent);
                overridePendingTransition(R.animator.slide_up, 0);
            }
        });

    }

    private void updatePlayerUI() {
        runOnUiThread(() -> {
            try {
                if (musicService != null && musicService.getCurrentMusic() != null) {
                    Music currentMusic = musicService.getCurrentMusic();
                    songTitle.setText(currentMusic.getMusicName());
                    artistName.setText(currentMusic.getAuthor());

                    // Cập nhật nút play/pause
                    playPauseButton.setImageResource(
                            musicService.isPlaying()
                                    ? R.drawable.notification_pause
                                    : R.drawable.detail_music_play
                    );

                    // Hiển thị player card khi có nhạc
                    playerCard.setVisibility(View.VISIBLE);
                } else {
                    playerCard.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.e("MusicActivity", "Lỗi cập nhật UI: " + e.getMessage());
            }
        });
    }
    // Thêm BroadcastReceiver
    private BroadcastReceiver musicUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MusicActivity", "Nhận broadcast cập nhật UI");
            String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case "MUSIC_UPDATE":
                    updatePlayerUI();
                    break;
                case "TRACK_CHANGED":
                    Music newMusic = intent.getParcelableExtra("currentMusic");
                    if (newMusic != null) {
                        songTitle.setText(newMusic.getMusicName());
                        artistName.setText(newMusic.getAuthor());
                    }
                    break;
            }

            // Cập nhật thêm trạng thái phát
            if (musicService != null) {
                boolean isPlaying = musicService.isPlaying();
                playPauseButton.setImageResource(
                        isPlaying
                                ? R.drawable.notification_pause
                                : R.drawable.detail_music_play
                );
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("MUSIC_UPDATE");
        filter.addAction("TRACK_CHANGED");
        registerReceiver(musicUpdateReceiver, filter, Context.RECEIVER_EXPORTED);
        // Cập nhật UI ngay lập tức khi trở lại
        updatePlayerUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(musicUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void setView() {
        // Ánh xạ view
        playerCard = findViewById(R.id.playerCard);
        playPauseButton = findViewById(R.id.playPauseButton);
        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        expandButton = findViewById(R.id.expandButton);
        songTitle = findViewById(R.id.songTitle);
        artistName = findViewById(R.id.artistName);
        btnBack = findViewById(R.id.backButton);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
                musicService = binder.getService();

                // Đăng ký listener trực tiếp từ service
                musicService.setUpdateListener(new MusicService.MusicUpdateListener() {
                    @Override
                    public void onMusicUpdate() {
                        updatePlayerUI();
                    }
                });

                updatePlayerUI();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicService = null;
            }
        };
    }

    private void loadMusicFromDB() {
        // Observe and limit to 3 music items
        musicViewModel.getAllMusics().observe(this, musics -> {
            if (musics != null && !musics.isEmpty()) {
                // Only take up to 3 items
                List<Music> limitedMusics = musics.size() > 3 ? musics.subList(0, 3) : musics;
                musicList.clear();
                musicList.addAll(limitedMusics);
                musicAdapter.notifyDataSetChanged();
            }
        });
    }


    private void loadPopularMusics() {
        // Khởi tạo danh sách bài hát
        RecyclerView recyclerViewNewItems = findViewById(R.id.popular_music_view);
        LinearLayoutManager newItemsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewNewItems.setLayoutManager(newItemsLayoutManager);
        musicAdapter = new MusicAdapter(this, musicList,this);
        recyclerViewNewItems.setAdapter(musicAdapter);
        recyclerViewNewItems.setHasFixedSize(true);


    }

    private void setupImageSlider() {
        // Khởi tạo danh sách ảnh
        List<Integer> sliderImages = Arrays.asList(
                R.drawable.music_background,
                R.drawable.music_background2,
                R.drawable.music_background3
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
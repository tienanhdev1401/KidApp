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
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.Adapter.MusicAdapter;
import com.example.kidapp.R;
import com.example.kidapp.Service.MusicService;
import com.example.kidapp.ViewModel.MusicCategoryViewModel;
import com.example.kidapp.ViewModel.MusicViewModel;
import com.example.kidapp.models.Music;

import java.util.ArrayList;
import java.util.List;

public class CategorySongsActivity extends AppCompatActivity {

    private ImageView backBtn, playPauseBtn, nextBtn, previousBtn, expandBtn;
    private String categoryName;
    private TextView categoryTitle, songTitle, artistName;
    private MusicService musicService;
    private List<Music> musicList = new ArrayList<>();
    private MusicAdapter musicAdapter;
    private ServiceConnection serviceConnection;
    private MusicViewModel musicViewModel;
    private MusicCategoryViewModel musicCategoryViewModel;
    private CardView playerCard;
    private RecyclerView recyclerViewMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category_songs);
        setUI();
        loadMusicsFromDb();
        setBtnControl();
        Intent serviceIntent = new Intent(this, MusicService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void  loadMusicsFromDb() {
        musicCategoryViewModel.getMusicByCategoryName(categoryName).observe(this, musics -> {
            if (musics != null && !musics.isEmpty()) {
                // Only take up to 3 items
                musicList.clear();
                musicList.addAll(musics);
                musicAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setUI() {
        backBtn = findViewById(R.id.categoryBackButton);
        playPauseBtn = findViewById(R.id.categoryPlayPauseButton);
        nextBtn = findViewById(R.id.categoryNextButton);
        previousBtn = findViewById(R.id.categoryPreviousButton);
        expandBtn = findViewById(R.id.categoryExpandButton);
        categoryTitle = findViewById(R.id.categoryTitleText);
        songTitle = findViewById(R.id.categorySongTitle);
        artistName = findViewById(R.id.categoryArtistName);
        playerCard = findViewById(R.id.categoryPlayerCard);
        recyclerViewMusic = findViewById(R.id.categoryRecyclerView);
        musicViewModel = new ViewModelProvider(this).get(MusicViewModel.class);
        musicCategoryViewModel = new ViewModelProvider(this).get(MusicCategoryViewModel.class);

        categoryName = getIntent().getStringExtra("categoryName");
        categoryTitle.setText(categoryName);

        LinearLayoutManager newItemsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewMusic.setLayoutManager(newItemsLayoutManager);
        musicAdapter = new MusicAdapter(this, musicList,this);
        recyclerViewMusic.setAdapter(musicAdapter);
        recyclerViewMusic.setHasFixedSize(true);

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

    public void setBtnControl() {
        backBtn.setOnClickListener(v -> {
            finish();
        });

        musicAdapter.setOnItemClickListener((position, music) -> {
            Intent intent = new Intent(this, MusicDetailActivity.class);
            intent.putParcelableArrayListExtra("playlist", new ArrayList<>(musicList));
            intent.putExtra("musicPosition", position);
            intent.putExtra("music", music);
            startActivity(intent);
        });

        playPauseBtn.setOnClickListener(v -> {
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
        previousBtn.setOnClickListener(v -> {
            if(musicService != null) {
                musicService.playPrevious();
            }
        });

        // Next
        nextBtn.setOnClickListener(v -> {
            if(musicService != null) {
                musicService.playNext();
            }
        });

        // Mở màn hình chi tiết
        expandBtn.setOnClickListener(v -> {
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

                Intent intent = new Intent(this, MusicDetailActivity.class);
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
                    playPauseBtn.setImageResource(
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
                playPauseBtn.setImageResource(
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


}
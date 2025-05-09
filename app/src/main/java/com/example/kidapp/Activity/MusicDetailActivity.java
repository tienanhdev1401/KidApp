package com.example.kidapp.Activity;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.Service.MusicService;
import com.example.kidapp.models.Music;

import java.util.ArrayList;
import java.util.List;

public class MusicDetailActivity extends AppCompatActivity {

    private ImageView btnplayPauseButton, backButton, btnPrevious, btnNext;
    private List<Music> playlist;

    private boolean isPlaying = false; // Trạng thái phát nhạc
    private MusicService musicService;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvTotalTime;
    private final Handler handler = new Handler();
    private MediaPlayer mediaPlayer;

    private CardView albumArtContainer;
    private ImageView albumArt;
    private ObjectAnimator rotationAnimator;
    private ObjectAnimator rotationAnimatorAlbumArt;

    private Music music;
    private int musicPosition;
    TextView songTitle, artistName;



    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();

            // Cập nhật UI ngay lập tức với bài hát hiện tại
            Music currentMusic = musicService.getCurrentMusic();
            if(currentMusic != null){
                updateUIWithNewTrack(currentMusic);
            }
            updatePlayPauseButton(true);
            rotationAnimator.start();
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };

    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (musicService != null && musicService.isPlaying()) {
                int currentPosition = musicService.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                tvCurrentTime.setText(formatTime(currentPosition));
            }
            // Lặp lại sau mỗi 500ms
            handler.postDelayed(this, 500);
        }
    };

    private BroadcastReceiver playStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPlaying = intent.getBooleanExtra("isPlaying", false);
            runOnUiThread(() -> {
                updatePlayPauseButton(isPlaying);
                handleRotationAnimation(isPlaying);
            });

        }
    };

    private void handleRotationAnimation(boolean isPlaying) {
        if (isPlaying) {
            if (rotationAnimator != null) {
                if (rotationAnimator.isPaused()) {
                    rotationAnimator.resume();
                    rotationAnimatorAlbumArt.resume();
                } else if (!rotationAnimator.isRunning()) {
                    rotationAnimator.start();
                    rotationAnimatorAlbumArt.start();
                }
            }
        } else {
            if (rotationAnimator != null && rotationAnimator.isRunning()) {
                rotationAnimator.pause();
                rotationAnimatorAlbumArt.pause();
            }
        }
    }
    private BroadcastReceiver errorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String errorMessage = intent.getStringExtra("errorMessage");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(MusicDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                // Update UI to show error state
                updatePlayPauseButton(false);
            }
        }
    };

    private BroadcastReceiver trackChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MusicDetail", "Intent nhận được: " + intent.toString());
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Log.d("MusicDetail", "Các key trong extras: " + extras.keySet());
            }
            // Music newMusic = intent.getParcelableExtra("currentMusic");
            Music newMusic = extras.getParcelable("currentMusic");
            if (newMusic != null) {
                runOnUiThread(() -> {
                    // Cập nhật UI
                    music = newMusic;
                    songTitle.setText(newMusic.getMusicName());
                    artistName.setText(newMusic.getAuthor());

                    // Load ảnh mới
                    Glide.with(MusicDetailActivity.this)
                            .load(newMusic.getMusicAvtUrl())
                            .override(920, 950)
                            .fitCenter()
                            .into(albumArt);

                    // Reset seekbar
                    seekBar.setProgress(0);
                    seekBar.setMax(musicService.getDuration());
                    tvTotalTime.setText(formatTime(musicService.getDuration()));
                });
            }
        }
    };
//private BroadcastReceiver trackChangedReceiver = new BroadcastReceiver() {
//    @Override
//    public void onReceive(Context context, Intent intent) {
//
//        try {
//            Bundle extras = intent.getExtras();
//            if (extras != null) {
//                Log.d("MusicDetail", "Extras keys: " + extras.keySet().toString());
//                Music newMusic = extras.getParcelable("currentMusic");
//                Log.d("MusicDetail", "Nhận được Music object: " + (newMusic != null ? newMusic.toString() : "null"));
//
//                if (newMusic != null) {
//                    runOnUiThread(() -> {
//                        Log.d("MusicDetail", "Bắt đầu cập nhật UI...");
//                        updateUIWithNewTrack(newMusic);
//                    });
//                }
//            }
//        } catch (Exception e) {
//            Log.e("MusicDetail", "Lỗi xử lý broadcast: " + e.getMessage());
//        }
//    }
//};

    private void updateUIWithNewTrack(Music music) {
        if (music == null) {
            Log.e("MusicDetailActivity", "Cannot update UI with null music object");
            return;
        }

        runOnUiThread(() -> {
            try {

                Log.d("MusicDetailActivity", "Updating UI with new track: " + music.getMusicName());


                songTitle.setText(music.getMusicName());
                artistName.setText(music.getAuthor());

                // Load ảnh với Glide và xử lý lỗi
                Glide.with(this)
                        .load(music.getMusicAvtUrl())
                        .override(920, 950)
                        .fitCenter()
                        .into(albumArt);

                // Khởi động lại animation
                if (rotationAnimator != null) {
                    rotationAnimator.start();
                    rotationAnimatorAlbumArt.start();
                }
            } catch (Exception e) {
                Log.e("MusicDetailActivity", "Error updating UI: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_music_detail);
        
        // Lấy dữ liệu từ intent
        playlist = getIntent().getParcelableArrayListExtra("playlist");
        musicPosition = getIntent().getIntExtra("musicPosition", -1);
        music = getIntent().getParcelableExtra("music");


        if (playlist == null || playlist.isEmpty()) {
            // Nếu playlist null thì tạo playlist chứa bài hát hiện tại
            if (music != null) {
                playlist = new ArrayList<>();
                playlist.add(music);
                musicPosition = 0;
            }
        }

        // Kiểm tra vị trí hợp lệ
        if (musicPosition < 0 || musicPosition >= playlist.size()) {
            Log.e("MusicDetailActivity", "Vị trí bài hát không hợp lệ: " + musicPosition);
            if (music != null && playlist != null) {
                // Tìm vị trí của bài hát trong playlist
                for (int i = 0; i < playlist.size(); i++) {
                    if (playlist.get(i).getMusicUrl().equals(music.getMusicUrl())) {
                        musicPosition = i;
                        Log.d("MusicDetailActivity", "Đã tìm thấy vị trí chính xác: " + musicPosition);
                        break;
                    }
                }
            }
            
            // Nếu vẫn không tìm thấy, sử dụng vị trí 0
            if (musicPosition < 0 || musicPosition >= playlist.size()) {
                musicPosition = 0;
                Log.d("MusicDetailActivity", "Sử dụng vị trí mặc định: 0");
            }
        }
        
        // Đảm bảo music luôn trỏ đến bài hát hiện tại trong playlist
        if (playlist != null && musicPosition >= 0 && musicPosition < playlist.size()) {
            music = playlist.get(musicPosition);
            Log.d("MusicDetailActivity", "Bài hát hiện tại: " + music.getMusicName());
        }

        // Ánh xạ các view
        songTitle = findViewById(R.id.songTitle);
        artistName = findViewById(R.id.artistName);
        seekBar = findViewById(R.id.seekBar);
        btnplayPauseButton = findViewById(R.id.playPauseButton);
        backButton = findViewById(R.id.backButton);
        tvCurrentTime = findViewById(R.id.currentTime);
        tvTotalTime = findViewById(R.id.totalTime);
        albumArtContainer = findViewById(R.id.albumArtContainer);
        albumArt = findViewById(R.id.albumArt);
        btnPrevious = findViewById(R.id.previousButton);
        btnNext = findViewById(R.id.nextButton);
        
        // Gửi playlist tới Service trước khi phát nhạc
        Intent serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.putExtra("playlist", new ArrayList<>(playlist));
        serviceIntent.putExtra("position", musicPosition);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        // Phát bài hát hiện tại sau khi đã gửi playlist
        if (music != null && music.getMusicUrl() != null) {
            Intent playIntent = new Intent(this, MusicService.class);
            playIntent.setAction(MusicService.ACTION_PLAY);
            playIntent.putExtra("musicUrl", music.getMusicUrl());
            startService(playIntent);
        }

        setUpUI();
        setupListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setUpUI() {
        if (music != null) {
            songTitle.setText(music.getMusicName());
            artistName.setText(music.getAuthor());

            // Load album artwork
            if (music.getMusicAvtUrl() != null && !music.getMusicAvtUrl().isEmpty()) {
                Glide.with(this)
                        .load(music.getMusicAvtUrl())
                        .override(920, 950)
                        .fitCenter()
                        .into(albumArt);
            }
        }

        setupRotationAnimation();
    }

    private void setupRotationAnimation() {
        // Tạo hoạt ảnh xoay
        rotationAnimator = ObjectAnimator.ofFloat(albumArtContainer, "rotation", 0f, 360f);
        rotationAnimator.setDuration(15000); // Thời gian để xoay 1 vòng tròn (15 giây)
        rotationAnimator.setInterpolator(new LinearInterpolator()); // Tốc độ xoay đều
        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE); // Lặp vô hạn
        rotationAnimatorAlbumArt = ObjectAnimator.ofFloat(albumArt, "rotation", 0f, 360f);
        rotationAnimatorAlbumArt.setDuration(15000);
        rotationAnimatorAlbumArt.setInterpolator(new LinearInterpolator());
        rotationAnimatorAlbumArt.setRepeatCount(ObjectAnimator.INFINITE);

        // Bắt đầu hoạt ảnh
        if (isPlaying) {
            rotationAnimator.start();
        }
    }

    private void setupListeners() {
        btnplayPauseButton.setOnClickListener(v -> {
            if (musicService != null) {
                boolean shouldPlay = !musicService.isPlaying();
                updatePlayPauseButton(shouldPlay); // Cập nhật UI ngay lập tức
                handleRotationAnimation(shouldPlay);

                if (shouldPlay) {
                    // Nếu đang pause thì tiếp tục, nếu không thì play bài mới
                    if (music != null && music.getMusicUrl() != null) {
                        // Thay vì luôn gọi ACTION_PLAY, gọi resumeMusic nếu URL giống nhau
                        if (musicService.getCurrentMusicUrl() != null &&
                                musicService.getCurrentMusicUrl().equals(music.getMusicUrl())) {
                            // Nếu URL giống nhau, chỉ cần resume
                            musicService.resumeMusic();
                        } else {
                            // URL khác nhau, phát bài mới
                            sendActionToServiceWithUrl(MusicService.ACTION_PLAY, music.getMusicUrl());
                        }
                    } else {
                        // Trường hợp không có URL cụ thể
                        musicService.resumeMusic();
                    }
                } else {
                    sendActionToService(MusicService.ACTION_PAUSE);
                }
            }
        });

        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, R.animator.slide_down);

        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicService != null) {
                    musicService.seekTo(progress); // Cập nhật vị trí bài hát
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBar); // Tạm dừng cập nhật khi kéo
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.seekTo(seekBar.getProgress()); // Tua đến vị trí mới
                handler.post(updateSeekBar); // Tiếp tục cập nhật sau khi kéo xong
            }
        });

        btnPrevious.setOnClickListener(v -> {
            Log.d("MusicDetailActivity", "Previous button clicked");
            if (musicService != null) {
                // Hiệu ứng khi nhấn nút
                btnPrevious.setAlpha(0.5f);
                btnPrevious.postDelayed(() -> btnPrevious.setAlpha(1.0f), 150);
                updateUIWithNewTrack(music);
                updateUI();
                sendActionToService(MusicService.ACTION_PREVIOUS);
            }
        });

        btnNext.setOnClickListener(v -> {
            Log.d("MusicDetailActivity", "Next button clicked");
            if (musicService != null) {
                // Hiệu ứng khi nhấn nút
                btnNext.setAlpha(0.5f);
                btnNext.postDelayed(() -> btnNext.setAlpha(1.0f), 150);
                updateUIWithNewTrack(music);
                updateUI();
                sendActionToService(MusicService.ACTION_NEXT);
            }
        });
    }

    private void updateUI() {
        if (musicService != null && musicService.isPlaying()) {
            int duration = musicService.getDuration();
            seekBar.setMax(duration);
            tvTotalTime.setText(formatTime(duration));

            handler.post(updateSeekBar); // Bắt đầu cập nhật seekBar
        } else if (musicService != null) {
            // The service is connected but not playing

            int duration = musicService.getDuration();
            if (duration > 0) {
                seekBar.setMax(duration);
                tvTotalTime.setText(formatTime(duration));
                handler.post(updateSeekBar);
            } else {
                // Still waiting for the media to be prepared
                handler.postDelayed(this::updateUI, 500);
            }
        } else {
            // MusicService not connected yet
            handler.postDelayed(this::updateUI, 500); // Chờ 500ms nếu chưa có dữ liệu
        }
    }

    private void updatePlayPauseButton(boolean playing) {

        // đổi icon
        runOnUiThread(() -> {
            isPlaying = playing;

            btnplayPauseButton.setImageResource(
                    playing
                            ? R.drawable.detail_music_pause
                            : R.drawable.detail_music_play
            );
            handleRotationAnimation(playing);

        });
    }

    private void sendActionToService(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        startService(intent); // Khởi động Service với action tương ứng
    }

    private void sendActionToServiceWithUrl(String action, String musicUrl) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        intent.putExtra("musicUrl", musicUrl);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Đăng ký nhận các sự kiện từ MusicService
        try {
            if (musicService != null) {
                playlist = musicService.getPlaylist();
                musicPosition = musicService.getCurrentPosition();
                music = musicService.getCurrentMusic();
                updateUIWithNewTrack(music);
            }
            // Broadcast receiver cho trạng thái phát nhạc
            IntentFilter playStatusFilter = new IntentFilter("MUSIC_PLAY_STATUS");
            ContextCompat.registerReceiver(
                    this,
                    playStatusReceiver,
                    playStatusFilter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
            );

            // Broadcast receiver cho lỗi
            IntentFilter errorFilter = new IntentFilter("MUSIC_ERROR");
            ContextCompat.registerReceiver(
                    this,
                    errorReceiver,
                    errorFilter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
            );

            // Broadcast receiver cho việc thay đổi bài hát - QUAN TRỌNG cho nút next/previous
//            IntentFilter trackChangedFilter = new IntentFilter("TRACK_CHANGED");
//            ContextCompat.registerReceiver(
//                    this,
//                    trackChangedReceiver,
//                    trackChangedFilter,
//                    ContextCompat.RECEIVER_NOT_EXPORTED
//            );

            IntentFilter trackChangedFilter = new IntentFilter("TRACK_CHANGED");
            registerReceiver(trackChangedReceiver, trackChangedFilter, Context.RECEIVER_EXPORTED);

        } catch (Exception e) {
            Log.e("MusicDetailActivity", "Error registering receivers: " + e.getMessage());
        }

        // Bind service
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister receivers
        try {
            unregisterReceiver(playStatusReceiver);
            unregisterReceiver(errorReceiver);
            unregisterReceiver(trackChangedReceiver);
        } catch (IllegalArgumentException e) {
            Log.e("MusicDetailActivity", "Receiver not registered: " + e.getMessage());
        }

        if (rotationAnimator != null && rotationAnimator.isRunning()) {
            rotationAnimator.pause();
        }

        if (musicService != null) {
            unbindService(serviceConnection); // Hủy kết nối Service
        }

        handler.removeCallbacks(updateSeekBar); // Dừng cập nhật SeekBar khi Activity ẩn
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy hoạt ảnh khi Activity bị hủy
        if (rotationAnimator != null) {
            rotationAnimator.cancel();
            rotationAnimator = null;
        }
        if (rotationAnimatorAlbumArt != null) {
            rotationAnimatorAlbumArt.cancel();
            rotationAnimatorAlbumArt = null;
        }
    }
    private void updateSeekBarProgress() {
        handler.postDelayed(() -> {
            if (musicService != null) {
                seekBar.setProgress(musicService.getCurrentPosition());
                updateSeekBarProgress();
            }
        }, 500);
    }
    private String formatTime(int millis) {
        return String.format("%02d:%02d", (millis / 1000) / 60, (millis / 1000) % 60);
    }
}
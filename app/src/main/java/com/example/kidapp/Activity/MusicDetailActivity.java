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
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.Service.MusicService;

public class MusicDetailActivity extends AppCompatActivity {

    private ImageView btnplayPauseButton, backButton;
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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            updatePlayPauseButton(musicService.isPlaying());
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
            updatePlayPauseButton(isPlaying); // Cập nhật UI khi trạng thái thay đổi
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_music_detail);

        seekBar = findViewById(R.id.seekBar);
        btnplayPauseButton = findViewById(R.id.playPauseButton);
        backButton = findViewById(R.id.backButton);
        tvCurrentTime = findViewById(R.id.currentTime);
        tvTotalTime = findViewById(R.id.totalTime);
        albumArtContainer = findViewById(R.id.albumArtContainer);
        albumArt = findViewById(R.id.albumArt);

        // bỏ đoaạn này laf ảnh thành bình thường nhé
        Glide.with(this)
                .asGif()
                .load(R.drawable.squirrel)
                .override(1200, 1200) // Tuỳ chỉnh kích thước
                .fitCenter()
                .into(albumArt);
        setupRotationAnimation();

        setupListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
            if (isPlaying) {
                rotationAnimator.pause();
                sendActionToService(MusicService.ACTION_PAUSE);
                updatePlayPauseButton(false);
            } else {
                rotationAnimator.resume();
                sendActionToService(MusicService.ACTION_PLAY);
                updatePlayPauseButton(true);
            }
        });

        backButton.setOnClickListener(v -> {
            finish();
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

    }

    private void updateUI() {
        if (musicService != null && musicService.isPlaying()) {
            int duration = musicService.getDuration();
            seekBar.setMax(duration);
            tvTotalTime.setText(formatTime(duration));

            handler.post(updateSeekBar); // Bắt đầu cập nhật seekBar
        } else {
            handler.postDelayed(this::updateUI, 500); // Chờ 500ms nếu chưa có dữ liệu
        }
    }



    private void updatePlayPauseButton(boolean playing) {
        isPlaying = playing;
        if (playing) {

            btnplayPauseButton.setImageResource(
                    R.drawable.detail_music_pause
            );

            if (rotationAnimator != null) {
                if (rotationAnimator.isPaused()) {
                    rotationAnimator.resume();
                } else {
                    rotationAnimator.start();
                }
            }
        } else {
            btnplayPauseButton.setImageResource(
                    R.drawable.detail_music_play
            );
            if (rotationAnimator != null && rotationAnimator.isStarted()) {
                rotationAnimator.pause();
            }
        }
    }

    private void sendActionToService(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        startService(intent); // Khởi động Service với action tương ứng
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        registerReceiver(
                playStatusReceiver,
                new IntentFilter("MUSIC_PLAY_STATUS"),
                Context.RECEIVER_NOT_EXPORTED
        );
        handler.post(updateSeekBar); // Đảm bảo SeekBar luôn cập nhật

    }


    @Override
    protected void onPause() {
        super.onPause();
        // Hủy đăng ký
        unregisterReceiver(playStatusReceiver);
        if (rotationAnimator != null && rotationAnimator.isRunning()) {
            rotationAnimator.pause();
        }
        if (musicService != null) {
            unbindService(serviceConnection); // Hủy kết nối Service
        }
        handler.removeCallbacks(updateSeekBar); // Dừng cập nhật SeekBar

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy hoạt ảnh khi Activity bị hủy
        if (rotationAnimator != null) {
            rotationAnimator.cancel();
            rotationAnimator = null;
        }
    }


    private String formatTime(int millis) {
        return String.format("%02d:%02d", (millis / 1000) / 60, (millis / 1000) % 60);
    }
}
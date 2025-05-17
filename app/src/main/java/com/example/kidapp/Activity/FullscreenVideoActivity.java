package com.example.kidapp.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.kidapp.R;
import com.example.kidapp.models.Story;

import java.util.ArrayList;

public class FullscreenVideoActivity extends AppCompatActivity {
    private static final String TAG = "FullscreenVideoActivity";
    private VideoView videoView;
    private ImageButton btnPlayPause, btnFullscreenExit, btnPrevious, btnNext;
    private SeekBar seekBar;
    private TextView tvDuration;
    private ConstraintLayout controlsLayout;
    
    private String videoUrl;
    private int currentPosition = 0;
    private boolean isPlaying = false;
    private ArrayList<Story> playlist;
    private int storyPosition;
    
    private boolean isControlsVisible = true;
    private final long AUTO_HIDE_DELAY_MILLIS = 5000; // 5 giây
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable hideControlsRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Thiết lập toàn màn hình
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_fullscreen_video);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        // Lấy dữ liệu từ intent
        Intent intent = getIntent();
        videoUrl = intent.getStringExtra("videoUrl");
        currentPosition = intent.getIntExtra("currentPosition", 0);
        isPlaying = intent.getBooleanExtra("isPlaying", true);
        playlist = intent.getParcelableArrayListExtra("playlist");
        storyPosition = intent.getIntExtra("storyPosition", 0);
        
        setupViews();
        setupControlsHandling();
        setupVideo();
    }
    
    private void setupViews() {
        videoView = findViewById(R.id.fullscreenVideoView);
        btnPlayPause = findViewById(R.id.btnFullscreenPlayPause);
        btnFullscreenExit = findViewById(R.id.btnFullscreenExit);
        btnPrevious = findViewById(R.id.btnFullscreenPrevious);
        btnNext = findViewById(R.id.btnFullscreenNext);
        seekBar = findViewById(R.id.fullscreenSeekBar);
        tvDuration = findViewById(R.id.fullscreenTvDuration);
        controlsLayout = findViewById(R.id.fullscreenControlsLayout);
        
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnFullscreenExit.setOnClickListener(v -> exitFullscreen());
        btnPrevious.setOnClickListener(v -> playPrevious());
        btnNext.setOnClickListener(v -> playNext());
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void setupControlsHandling() {
        // Runnable để ẩn controls sau khoảng thời gian không tương tác
        hideControlsRunnable = () -> {
            if (isPlaying) {
                hideControls();
            }
        };
        
        // Hiển thị controls khi chạm vào màn hình
        videoView.setOnClickListener(v -> {
            if (isControlsVisible) {
                hideControls();
            } else {
                showControls();
            }
        });
    }
    
    private void setupVideo() {
        if (videoUrl == null || videoUrl.isEmpty()) {
            Log.e(TAG, "Video URL is null or empty");
            finish();
            return;
        }
        
        try {
            Uri uri = Uri.parse(videoUrl);
            videoView.setVideoURI(uri);
            
            videoView.setOnPreparedListener(mp -> {
                // Cập nhật seekbar
                int duration = mp.getDuration();
                seekBar.setMax(duration);
                seekBar.setProgress(currentPosition);
                tvDuration.setText(formatTime(currentPosition) + " / " + formatTime(duration));
                
                // Thiết lập vị trí và bắt đầu phát nếu cần
                videoView.seekTo(currentPosition);
                if (isPlaying) {
                    videoView.start();
                    scheduleHideControls();
                }
                
                // Cập nhật tiến trình liên tục
                startProgressUpdates();
            });
            
            videoView.setOnCompletionListener(mp -> {
                btnPlayPause.setImageResource(R.drawable.detail_music_play);
                isPlaying = false;
                showControls();
                
                // Tự động chuyển bài tiếp theo
                playNext();
            });
            
            videoView.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Error playing video: " + what + ", " + extra);
                return false;
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up video: " + e.getMessage());
            finish();
        }
    }
    
    private void startProgressUpdates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (videoView.isPlaying()) {
                    int currentPos = videoView.getCurrentPosition();
                    int duration = videoView.getDuration();
                    
                    seekBar.setProgress(currentPos);
                    tvDuration.setText(formatTime(currentPos) + " / " + formatTime(duration));
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }
    
    private void togglePlayPause() {
        if (videoView.isPlaying()) {
            videoView.pause();
            btnPlayPause.setImageResource(R.drawable.detail_music_play);
            isPlaying = false;
            showControls();
            handler.removeCallbacks(hideControlsRunnable);
        } else {
            videoView.start();
            btnPlayPause.setImageResource(R.drawable.detail_music_pause);
            isPlaying = true;
            scheduleHideControls();
        }
    }
    
    private void playPrevious() {
        if (playlist != null && !playlist.isEmpty()) {
            storyPosition = (storyPosition - 1 + playlist.size()) % playlist.size();
            Story story = playlist.get(storyPosition);
            videoUrl = story.getStoryVideoUrl();
            currentPosition = 0;
            setupVideo();
        }
    }
    
    private void playNext() {
        if (playlist != null && !playlist.isEmpty()) {
            storyPosition = (storyPosition + 1) % playlist.size();
            Story story = playlist.get(storyPosition);
            videoUrl = story.getStoryVideoUrl();
            currentPosition = 0;
            setupVideo();
        }
    }
    
    private void exitFullscreen() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("currentPosition", videoView.getCurrentPosition());
        resultIntent.putExtra("isPlaying", videoView.isPlaying());
        resultIntent.putExtra("storyPosition", storyPosition);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    private void showControls() {
        controlsLayout.setVisibility(View.VISIBLE);
        isControlsVisible = true;
    }
    
    private void hideControls() {
        controlsLayout.setVisibility(View.GONE);
        isControlsVisible = false;
    }
    
    private void scheduleHideControls() {
        handler.removeCallbacks(hideControlsRunnable);
        handler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
    }
    
    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exitFullscreen();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.pause();
            isPlaying = false;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
} 
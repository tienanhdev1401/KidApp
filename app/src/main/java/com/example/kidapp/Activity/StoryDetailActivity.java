package com.example.kidapp.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.Service.StoryService;
import com.example.kidapp.models.Story;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class StoryDetailActivity extends AppCompatActivity implements StoryService.StoryUpdateListener {

    private ArrayList<Story> playlist;
    private int storyPosition;
    private Story story;
    private boolean isPlaying = false;
    private int totalDuration = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ImageButton btnPlayVideo, btnPlayPause, btnFullscreen, btnBookmark, btnPrevious, btnNext, btnPLayAudio;
    private ImageView thumbnailImageView, btnBack;
    private SeekBar seekBar;
    private VideoView videoView;
    private TextView tvDuration, storyContent, moralContent, quizContent, storyTitle;
    private LinearLayout videoControlsLayout;
    private ViewPager2 viewPager;
    private TabLayout storyTabLayout;
    private CardView storyContentCard, moralCard, quizCard;
    private Button btnSubmitAnswer;
    private RadioButton rbOption1, rbOption2, rbOption3;
    private LottieAnimationView animationView;
    // Service related
    private StoryService storyService;
    private boolean isServiceBound = false;
    private Intent serviceIntent;

    // Broadcast receiver to update UI based on service state
    private BroadcastReceiver storyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "STORY_PLAY_STATUS":
                        boolean isPlaying = intent.getBooleanExtra("isPlaying", false);
                        updatePlayPauseButton(isPlaying);
                        break;
                    case "STORY_ERROR":
                        String errorMessage = intent.getStringExtra("errorMessage");
                        Toast.makeText(StoryDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        break;
                    case "STORY_CHANGED":
                        // Cập nhật UI khi chuyển sang video khác
                        if (storyService != null) {
                            story = storyService.getCurrentStory();
                            if (story != null) {
                                updateUI();
                            }
                        }
                        break;
                }
            }
        }
    };
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StoryService.StoryBinder binder = (StoryService.StoryBinder) service;
            storyService = binder.getService();
            storyService.setUpdateListener(StoryDetailActivity.this);
            isServiceBound = true;
            
            // Kết nối surface của VideoView với MediaPlayer trong Service
            storyService.setDisplay(videoView.getHolder());
            
            // Start playing the current story when service is bound
            if (storyService != null) {
                if (playlist != null && !playlist.isEmpty()) {
                    storyService.setPlaylist(playlist, storyPosition);
                    // Không tự động phát, chờ người dùng nhấn play
                }
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            storyService = null;
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.story_detail_activity);

        playlist = getIntent().getParcelableArrayListExtra("playlist");
        storyPosition = getIntent().getIntExtra("storyPosition", -1);
        story = getIntent().getParcelableExtra("story");

        setupViews();
        registerBroadcastReceivers();
        updateUI();
        setBtnControl();
        startAndBindService();

        // Thiết lập animation cho câu trả lời
        animationView = findViewById(R.id.animationView);
        btnSubmitAnswer.setOnClickListener(v -> checkAnswer());
    }
    
    private void startAndBindService() {
        serviceIntent = new Intent(this, StoryService.class);
        serviceIntent.putParcelableArrayListExtra("playlist", playlist);
        serviceIntent.putExtra("position", storyPosition);
        
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void registerBroadcastReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("STORY_PLAY_STATUS");
        filter.addAction("STORY_ERROR");
        filter.addAction("STORY_CHANGED");
        registerReceiver(storyReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    private void updateUI() {
        storyTitle.setText(story.getStoryTitle());
        storyContent.setText(story.getStoryContent());
        moralContent.setText(story.getStoryMoral());
        quizContent.setText(story.getStoryQuestion());
        rbOption1.setText(story.getOptionA());
        rbOption2.setText(story.getOptionB());
        rbOption3.setText(story.getOptionC());

        if(!story.getStoryImgUrl().isEmpty()) {
            String firstImage = story.getStoryImgUrl();
            Glide.with(this)
                    .load(firstImage)
                    .into(thumbnailImageView);
        }
    }

    private void setBtnControl() {
        storyTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        storyContentCard.setVisibility(View.VISIBLE);
                        moralCard.setVisibility(View.GONE);
                        quizCard.setVisibility(View.GONE);
                        break;
                    case 1:
                        storyContentCard.setVisibility(View.GONE);
                        moralCard.setVisibility(View.VISIBLE);
                        quizCard.setVisibility(View.GONE);
                        break;
                    case 2:
                        storyContentCard.setVisibility(View.GONE);
                        moralCard.setVisibility(View.GONE);
                        quizCard.setVisibility(View.VISIBLE);
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnBack.setOnClickListener(v -> finish());

        btnBookmark.setOnClickListener(v ->
                Toast.makeText(this, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show()
        );

        btnPrevious.setOnClickListener(v -> {
            if (isServiceBound && storyService != null) {
                storyService.playPrevious();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (isServiceBound && storyService != null) {
                storyService.playNext();
            }
        });

        btnSubmitAnswer.setOnClickListener(v -> checkAnswer());

        btnFullscreen.setOnClickListener(v ->
                Toast.makeText(this, "Đang chuyển sang chế độ toàn màn hình", Toast.LENGTH_SHORT).show()
        );

        btnPlayVideo.setOnClickListener(v -> {
            if (isServiceBound && storyService != null) {
                thumbnailImageView.setVisibility(View.GONE);
                btnPlayVideo.setVisibility(View.GONE);
                videoControlsLayout.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.VISIBLE);
                storyService.playCurrentStory();
            }
        });

        btnPlayPause.setOnClickListener(v -> {
            if (isServiceBound && storyService != null) {
                if (storyService.isPlaying()) {
                    storyService.pauseVideo();
                } else {
                    storyService.resumeVideo();
                }
            }
        });

        btnPLayAudio.setOnClickListener(v -> {
            if (isServiceBound && storyService != null) {
                if (storyService.isPlaying()) {
                    storyService.pauseVideo();
                } else {
                    storyService.resumeVideo();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isServiceBound && storyService != null) {
                    storyService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void setupViews() {
        storyTitle = findViewById(R.id.tvStoryTitle);
        btnPlayVideo = findViewById(R.id.btnPlayVideo);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPLayAudio = findViewById(R.id.btnAudio);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        videoView = findViewById(R.id.videoView);
        seekBar = findViewById(R.id.seekBar);
        tvDuration = findViewById(R.id.tvDuration);
        thumbnailImageView = findViewById(R.id.thumbnailImageView);
        videoControlsLayout = findViewById(R.id.videoControlsLayout);
        viewPager  = findViewById(R.id.viewPager);
        storyTabLayout = findViewById(R.id.storyTabLayout);
        storyContentCard = findViewById(R.id.storyContentCard);
        moralCard = findViewById(R.id.moralCard);
        quizCard = findViewById(R.id.quizCard);
        btnBack = findViewById(R.id.btnBack);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnSubmitAnswer = findViewById(R.id.btnSubmitAnswer);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        storyContent = findViewById(R.id.tvStoryContent);
        moralContent = findViewById(R.id.tvMoralContent);
        quizContent = findViewById(R.id.tvQuizQuestion);
        animationView = findViewById(R.id.animationView);
        storyContentCard.setVisibility(View.VISIBLE);
        moralCard.setVisibility(View.GONE);
        quizCard.setVisibility(View.GONE);
        
        // Thay vì ẩn VideoView, ta sẽ cấu hình nó để kết nối với MediaPlayer của Service
        videoView.setOnPreparedListener(null); // Xóa các listener mặc định
        videoView.setOnCompletionListener(null);
        videoView.setOnErrorListener(null);
        
        videoView.getHolder().addCallback(new android.view.SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(android.view.SurfaceHolder holder) {
                if (isServiceBound && storyService != null) {
                    storyService.setDisplay(holder);
                }
            }

            @Override
            public void surfaceChanged(android.view.SurfaceHolder holder, int format, int width, int height) {
                // Nothing to do
            }

            @Override
            public void surfaceDestroyed(android.view.SurfaceHolder holder) {
                // Nothing to do
            }
        });
    }
    
    private void updatePlayPauseButton(boolean isPlaying) {
        this.isPlaying = isPlaying;
        if (isPlaying) {
            btnPlayPause.setImageResource(R.drawable.detail_music_pause);
            btnPLayAudio.setImageResource(R.drawable.detail_music_pause);
        } else {
            btnPlayPause.setImageResource(R.drawable.detail_music_play);
            btnPLayAudio.setImageResource(R.drawable.detail_music_play);
        }
    }

    private void checkAnswer() {
        String correctAnswer = story.getStoryAnswer();
        
        String selectedAnswer = null;
        if (rbOption1.isChecked()) {
            selectedAnswer = story.getOptionA();
        } else if (rbOption2.isChecked()) {
            selectedAnswer = story.getOptionB();
        } else if (rbOption3.isChecked()) {
            selectedAnswer = story.getOptionC();
        }
        
        if (selectedAnswer != null && selectedAnswer.equals(correctAnswer)) {
            Toast.makeText(this, "Chính xác!", Toast.LENGTH_SHORT).show();
            if (animationView != null) {
                animationView.setVisibility(View.VISIBLE);
                animationView.playAnimation();
            }
        } else {
            Toast.makeText(this, "Sai rồi! Hãy thử lại.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStoryUpdate() {
        if (storyService != null) {
            story = storyService.getCurrentStory();
            if (story != null) {
                runOnUiThread(this::updateUI);
            }
        }
    }

    @Override
    public void onProgressUpdate(int currentPosition, int duration) {
        runOnUiThread(() -> {
            seekBar.setMax(duration);
            seekBar.setProgress(currentPosition);
            tvDuration.setText(formatTime(currentPosition) + " / " + formatTime(duration));
        });
    }

    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        unregisterReceiver(storyReceiver);
    }
}
package com.example.kidapp.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.Service.StoryService;
import com.example.kidapp.ViewModel.FavoriteViewModel;
import com.example.kidapp.models.Story;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class StoryDetailActivity extends AppCompatActivity implements StoryService.StoryUpdateListener {

    private ArrayList<Story> playlist;
    private int storyPosition;
    private Story story;
    private boolean isPlaying = false;
    private int totalDuration = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ImageButton btnPlayVideo, btnPlayPause, btnFullscreen, btnNextVideo, btnPreviousVideo, btnPLayAudio;
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

    private boolean isFullscreen = false;
    private ConstraintLayout headerLayout, navigationLayout;
    private NestedScrollView scrollView;
    private CardView videoPlayerCard;
    private ViewGroup.LayoutParams originalVideoParams;
    private String userEmail;
    private MaterialButton btnFavorite;
    private FavoriteViewModel favoriteViewModel;
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

    // Thêm thuộc tính mới để quản lý việc hiển thị controls
    private boolean isControlsVisible = true;
    private final long AUTO_HIDE_DELAY_MILLIS = 5000; // 5 giây
    private Runnable hideControlsRunnable;
    private View.OnClickListener showControlsListener;

    // Constants
    private static final int REQUEST_CODE_FULLSCREEN = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.story_detail_activity);

        playlist = getIntent().getParcelableArrayListExtra("playlist");
        storyPosition = getIntent().getIntExtra("storyPosition", -1);
        story = getIntent().getParcelableExtra("story");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        }
        setupViews();
        setupControlsHandling();
        registerBroadcastReceivers();
        updateUI();
        setBtnControl();
        startAndBindService();
        setupObservers();
        // Thiết lập animation cho câu trả lời
        animationView = findViewById(R.id.animationView);
    }

    private void setupObservers() {
        favoriteViewModel.isStoryFavorite(userEmail, story.getStoryId()).observe(this, isFavorite -> {
            if (isFavorite != null) {
                btnFavorite.setIconResource(isFavorite ? R.drawable.love : R.drawable.non_favorite);
                if (isFavorite) {
                    btnFavorite.setIconTint(getResources().getColorStateList(R.color.favorite));
                }
            }
        });
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


        btnSubmitAnswer.setOnClickListener(v -> checkAnswer());

        btnFullscreen.setOnClickListener(v -> toggleFullscreen());

        btnPlayVideo.setOnClickListener(v -> startPlayingVideo());

        if (btnPreviousVideo != null) {
            btnPreviousVideo.setOnClickListener(v -> {
                if (isServiceBound && storyService != null) {
                    storyService.playPrevious();
                }
            });
        }

        if (btnNextVideo != null) {
            btnNextVideo.setOnClickListener(v -> {
                if (isServiceBound && storyService != null) {
                    storyService.playNext();
                }
            });
        }

        btnPlayPause.setOnClickListener(v -> {
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

        btnFavorite.setOnClickListener(v -> {
            // Lấy trạng thái hiện tại và đảo ngược
            favoriteViewModel.isStoryFavorite(userEmail, story.getStoryId()).observe(this, isFavorite -> {
                if (isFavorite != null) {
                    // Cập nhật UI ngay lập tức trước khi cập nhật database
                    boolean newState = !isFavorite;
                    btnFavorite.setIconResource(newState ? R.drawable.love : R.drawable.non_favorite);
                    if (newState) {
                        btnFavorite.setIconTint(getResources().getColorStateList(R.color.favorite));
                    }

                    // Sau đó cập nhật trong database
                    favoriteViewModel.toggleStoryFavorite(userEmail, story.getStoryId());
                }
            });
        });
    }

    private void setupControlsHandling() {
        // Khởi tạo Runnable để ẩn controls sau một khoảng thời gian
        hideControlsRunnable = () -> {
            if (isServiceBound && storyService != null && storyService.isPlaying()) {
                hideControls();
            }
        };
        // Thiết lập click listener cho VideoView để hiển thị controls
        showControlsListener = v -> {
            if (isControlsVisible) {
                hideControls();
            } else {
                showControls();
                // Đặt lịch ẩn controls sau khoảng thời gian
                scheduleHideControls();
            }
        };
        // Gán click listener cho video player container
        videoView.setOnClickListener(showControlsListener);
        videoPlayerCard.setOnClickListener(showControlsListener);
    }
    
    private void scheduleHideControls() {
        // Hủy lệnh ẩn trước đó nếu có
        handler.removeCallbacks(hideControlsRunnable);
        // Đặt lịch ẩn controls sau khoảng thời gian
        handler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
    }
    
    private void showControls() {
        videoControlsLayout.setVisibility(View.VISIBLE);
        isControlsVisible = true;
    }
    
    private void hideControls() {
        if (isServiceBound && storyService != null && storyService.isPlaying()) {
            videoControlsLayout.setVisibility(View.GONE);
            isControlsVisible = false;
        }
    }

    private void toggleFullscreen() {
        // Chuyển đến FullscreenVideoActivity thay vì thay đổi layout hiện tại
        if (isServiceBound && storyService != null) {
            Story currentStory = storyService.getCurrentStory();
            if (currentStory != null) {
                // Tạm dừng phát video hiện tại
                boolean isPlaying = storyService.isPlaying();
                int currentPosition = storyService.getCurrentVideoPosition();
                
                if (isPlaying) {
                    storyService.pauseVideo();
                }
                
                // Chuyển sang activity fullscreen
                Intent intent = new Intent(this, FullscreenVideoActivity.class);
                intent.putExtra("videoUrl", currentStory.getStoryVideoUrl());
                intent.putExtra("currentPosition", currentPosition);
                intent.putExtra("isPlaying", isPlaying);
                intent.putParcelableArrayListExtra("playlist", playlist);
                intent.putExtra("storyPosition", storyService.getCurrentPosition());
                
                startActivityForResult(intent, REQUEST_CODE_FULLSCREEN);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_FULLSCREEN && resultCode == RESULT_OK && data != null) {
            // Nhận kết quả từ FullscreenVideoActivity
            int currentPosition = data.getIntExtra("currentPosition", 0);
            boolean isPlaying = data.getBooleanExtra("isPlaying", false);
            int storyPosition = data.getIntExtra("storyPosition", 0);
            
            // Cập nhật trạng thái trong StoryService
            if (isServiceBound && storyService != null) {
                // Nếu vị trí story đã thay đổi, cập nhật story mới
                if (storyPosition != storyService.getCurrentPosition()) {
                    storyService.setPlaylist(playlist, storyPosition);
                    storyService.playCurrentStory();
                    if (!isPlaying) {
                        storyService.pauseVideo();
                    }
                } else {
                    // Tiếp tục phát từ vị trí đã dừng
                    storyService.seekTo(currentPosition);
                    if (isPlaying) {
                        storyService.resumeVideo();
                    } else {
                        storyService.pauseVideo();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
            // Nếu đang phát video, dừng phát trước khi thoát
        if (isServiceBound && storyService != null && storyService.isPlaying()) {
            storyService.pauseVideo();
        }
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("FULLSCREEN", "Configuration changed to: " + 
              (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape" : "portrait"));

        // Xử lý thay đổi orientation khi ở chế độ fullscreen
        if (isFullscreen) {
            // Hủy các callback đang chờ
            handler.removeCallbacks(hideControlsRunnable);
            
            // Lưu trạng thái video trước khi thay đổi layout
            final boolean wasPlaying;
            final int currentPosition;
            
            if (isServiceBound && storyService != null) {
                wasPlaying = storyService.isPlaying();
                currentPosition = storyService.getCurrentVideoPosition();
                // Tạm dừng video trong khi điều chỉnh layout
                if (wasPlaying) {
                    storyService.pauseVideo();
                }
            } else {
                wasPlaying = false;
                currentPosition = 0;
            }
            
            // Các tác vụ cụ thể cho từng hướng màn hình
            try {
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    // Landscape mode
                    videoPlayerCard.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    ));
                    
                    if (videoPlayerCard instanceof CardView) {
                        ((CardView) videoPlayerCard).setRadius(0);
                    }
                    
                    // Ẩn tất cả UI khác
                    headerLayout.setVisibility(View.GONE);
                    // navigationLayout đã bị ẩn/comment trong layout
                    scrollView.setVisibility(View.GONE);
                } else {
                    // Portrait mode - nếu vẫn đang ở chế độ fullscreen
                    // Gọi exitFullscreen sẽ tốt hơn ở đây
                    if (isFullscreen) {
                        //exitFullscreen();
                        isFullscreen = false;
                        return;
                    }
                }
                
                // Đảm bảo VideoView visible
                videoView.setVisibility(View.VISIBLE);
                showControls(); // Hiện controls khi thay đổi orientation
                
                // Cập nhật lại surface holder và tiếp tục phát
                videoView.invalidate();
                handler.postDelayed(() -> {
                    if (isServiceBound && storyService != null) {
                        try {
                            storyService.setDisplay(videoView.getHolder());
                            storyService.updateVideoSize();
                            
                            // Tiếp tục phát từ vị trí trước đó nếu đang phát
                            if (wasPlaying) {
                                Log.d("FULLSCREEN", "Resuming video after config change, position: " + currentPosition);
                                storyService.seekTo(currentPosition);
                                storyService.resumeVideo();
                                
                                // Lên lịch ẩn controls sau khi tiếp tục phát
                                scheduleHideControls();
                            }
                        } catch (Exception e) {
                            Log.e("FULLSCREEN", "Error updating display: " + e.getMessage());
                        }
                    }
                }, 500);
            } catch (Exception e) {
                Log.e("FULLSCREEN", "Error in configuration change: " + e.getMessage());
            }
        }
    }
    private void setupViews() {
        storyTitle = findViewById(R.id.tvStoryTitle);
        btnPlayVideo = findViewById(R.id.btnPlayVideo);
        btnPlayPause = findViewById(R.id.btnPlayPause);
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
        btnSubmitAnswer = findViewById(R.id.btnSubmitAnswer);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        storyContent = findViewById(R.id.tvStoryContent);
        moralContent = findViewById(R.id.tvMoralContent);
        quizContent = findViewById(R.id.tvQuizQuestion);
        animationView = findViewById(R.id.animationView);
        btnPreviousVideo = findViewById(R.id.btnPreviousVideo);
        btnNextVideo = findViewById(R.id.btnNextVideo);
        storyContentCard.setVisibility(View.VISIBLE);
        moralCard.setVisibility(View.GONE);
        quizCard.setVisibility(View.GONE);
        scrollView = findViewById(R.id.scrollView);
        btnFavorite = findViewById(R.id.btnFavorite);
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);


        // Cấu hình VideoView để kết nối với MediaPlayer của Service
        videoView.getHolder().addCallback(new android.view.SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(android.view.SurfaceHolder holder) {
                if (isServiceBound && storyService != null) {
                    storyService.setDisplay(holder);
                }
            }

            @Override
            public void surfaceChanged(android.view.SurfaceHolder holder, int format, int width, int height) {
                if (isServiceBound && storyService != null) {
                    storyService.setDisplay(holder);
                }
            }

            @Override
            public void surfaceDestroyed(android.view.SurfaceHolder holder) {
                if (isServiceBound && storyService != null) {
                    storyService.setDisplay(null);
                }
            }
        });
        
        headerLayout = findViewById(R.id.headerLayout);
     //   navigationLayout = findViewById(R.id.navigationLayout);
        videoPlayerCard = findViewById(R.id.videoPlayerCard);
        
        // Lưu LayoutParams gốc của videoPlayerCard để khôi phục sau khi thoát fullscreen
        originalVideoParams = new ViewGroup.LayoutParams(
                videoPlayerCard.getLayoutParams().width,
                videoPlayerCard.getLayoutParams().height
        );
        
        videoView.setVisibility(View.VISIBLE);
        videoControlsLayout.setVisibility(View.VISIBLE);
    }
    
    private void updatePlayPauseButton(boolean isPlaying) {
        this.isPlaying = isPlaying;
        if (isPlaying) {
            btnPlayPause.setImageResource(R.drawable.detail_music_pause);
        } else {
            btnPlayPause.setImageResource(R.drawable.detail_music_play);

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
            
            // Ẩn controls nếu đang phát video và đã hiển thị quá lâu
            if (storyService != null && storyService.isPlaying() && isControlsVisible) {
                // Đảm bảo controls sẽ tự động ẩn khi video đang phát
                scheduleHideControls();
            }
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

    // Cập nhật phương thức này để xử lý việc hiển thị nút khi bắt đầu phát video
    private void startPlayingVideo() {
        if (isServiceBound && storyService != null) {
            thumbnailImageView.setVisibility(View.GONE);
            btnPlayVideo.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            
            // Hiển thị controls ban đầu
            showControls();
            
            // Bắt đầu phát video
            storyService.playCurrentStory();
            
            // Lên lịch ẩn nút sau khi phát
            scheduleHideControls();
        }
    }
}
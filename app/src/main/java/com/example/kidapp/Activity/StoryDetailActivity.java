package com.example.kidapp;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.material.tabs.TabLayout;

public class StoryDetailActivity extends AppCompatActivity {

    private boolean isPlaying = false;
    private int totalDuration = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBar;
    private ImageButton btnPlayVideo, btnPlayPause,btnFullscreen, btnBookmark, btnPrevious, btnNext;
    private ImageView thumbnailImageView, btnBack;
    private SeekBar seekBar;
    private VideoView videoView;
    private TextView tvDuration;
    private LinearLayout videoControlsLayout;
    private ViewPager2 viewPager;
    private TabLayout storyTabLayout;
    private CardView storyContentCard, moralCard, quizCard;
    private Button btnSubmitAnswer;
    private RadioButton rbOption1, rbOption2, rbOption3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.story_detail_activity);
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
        btnBookmark = findViewById(R.id.btnBookmark);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnSubmitAnswer = findViewById(R.id.btnSubmitAnswer);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);

        storyContentCard.setVisibility(View.VISIBLE);
        moralCard.setVisibility(View.GONE);
        quizCard.setVisibility(View.GONE);

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
        setupVideoPlayer();
        //setupTabs();
        setupNavigation();



        LottieAnimationView animationView = findViewById(R.id.animationView);
        btnSubmitAnswer.setOnClickListener(v -> {
            animationView.setVisibility(View.VISIBLE);
            animationView.playAnimation();
        });
    }
    private void setupVideoPlayer() {
        btnPlayVideo.setOnClickListener(v -> startVideo());

        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                videoView.pause();
                btnPlayPause.setImageResource(R.drawable.detail_music_play);
                isPlaying = false;
            } else {
                videoView.start();
                btnPlayPause.setImageResource(R.drawable.detail_music_pause);
                isPlaying = true;
            }
        });

        btnFullscreen.setOnClickListener(v ->
                Toast.makeText(this, "Đang chuyển sang chế độ toàn màn hình", Toast.LENGTH_SHORT).show()
        );

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        btnPlayVideo.setOnClickListener(v -> startVideo());

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (videoView.isPlaying()) {
                    int currentPosition = videoView.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    String currentTime = formatTime(currentPosition);
                    String totalTime = formatTime(totalDuration);
                    tvDuration.setText(currentTime + " / " + totalTime);
                }
                handler.postDelayed(this, 1000);
            }
        };
    }

    private void startVideo() {
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.the_ant_and_the_grasshopper_short_firm;
        videoView.setVideoURI(Uri.parse(videoPath));
        videoView.setOnPreparedListener(mediaPlayer -> {
            thumbnailImageView.setVisibility(View.GONE);
            btnPlayVideo.setVisibility(View.GONE);
            videoControlsLayout.setVisibility(View.VISIBLE);

            totalDuration = mediaPlayer.getDuration();
            seekBar.setMax(totalDuration);

            videoView.start();
            isPlaying = true;

            handler.post(updateSeekBar);
        });

        videoView.setOnCompletionListener(mediaPlayer -> {
            thumbnailImageView.setVisibility(View.VISIBLE);
            btnPlayVideo.setVisibility(View.VISIBLE);
            videoControlsLayout.setVisibility(View.GONE);
            isPlaying = false;
            handler.removeCallbacks(updateSeekBar);
        });
    }

    private void setupTabs() {
//        StoryPagerAdapter pagerAdapter = new StoryPagerAdapter(this);
//        viewPager.setAdapter(pagerAdapter);
//
//        new TabLayoutMediator(storyTabLayout, viewPager,
//                (tab, position) -> {
//                    switch (position) {
//                        case 0:
//                            tab.setText("Nội dung");
//                            break;
//                        case 1:
//                            tab.setText("Bài học");
//                            break;
//                        case 2:
//                            tab.setText("Câu hỏi");
//                            break;
//                    }
//                }).attach();
//
//        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//                storyContentCard.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
//                moralCard.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
//                quizCard.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
//            }
//        });
    }

    private void setupNavigation() {
        btnBack.setOnClickListener(v -> finish());

        btnBookmark.setOnClickListener(v ->
                Toast.makeText(this, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show()
        );

        btnPrevious.setOnClickListener(v ->
                Toast.makeText(this, "Đang chuyển đến câu chuyện trước", Toast.LENGTH_SHORT).show()
        );

        btnNext.setOnClickListener(v ->
                Toast.makeText(this, "Đang chuyển đến câu chuyện tiếp theo", Toast.LENGTH_SHORT).show()
        );

        btnSubmitAnswer.setOnClickListener(v -> checkAnswer());
    }

    private void checkAnswer() {
        int selectedOption = 0;
        if (rbOption1.isChecked()) selectedOption = 1;
        else if (rbOption2.isChecked()) selectedOption = 2;
        else if (rbOption3.isChecked()) selectedOption = 3;

        if (selectedOption == 0) {
            Toast.makeText(this, "Vui lòng chọn một đáp án", Toast.LENGTH_SHORT).show();
        } else {
            // Add your answer-checking logic here
            Toast.makeText(this, "Bạn đã chọn đáp án: " + selectedOption, Toast.LENGTH_SHORT).show();

        }
    }

    private String formatTime(int millis) {
        long minutes = millis / 1000 / 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
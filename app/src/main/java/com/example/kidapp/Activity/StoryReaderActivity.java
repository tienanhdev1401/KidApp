package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kidapp.R;
import com.example.kidapp.Adapter.ScenesPagerAdapter;
import com.example.kidapp.models.StoryModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class StoryReaderActivity extends AppCompatActivity {

    private TextView storyTitle;
    private ViewPager2 scenesViewPager;
    private FloatingActionButton fabPlayPause, fabShare, fabSave;
    private Button createNewStoryButton;
    
    private TextToSpeech textToSpeech;
    private boolean isPlaying = false;
    private StoryModel story;
    private ScenesPagerAdapter scenesAdapter;
    private int currentScenePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_reader);

        // Thiết lập toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Khởi tạo các thành phần UI
        storyTitle = findViewById(R.id.storyTitle);
        scenesViewPager = findViewById(R.id.scenesViewPager);
        fabPlayPause = findViewById(R.id.fabPlayPause);
        fabShare = findViewById(R.id.fabShare);
        fabSave = findViewById(R.id.fabSave);
        createNewStoryButton = findViewById(R.id.createNewStoryButton);

        // Lấy dữ liệu truyện từ Intent
        story = getIntent().getParcelableExtra("story");
        if (story != null) {
            displayStory(story);
        } else {
            Toast.makeText(this, "Không thể tải truyện", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Khởi tạo TextToSpeech
        initTextToSpeech();

        // Thiết lập các sự kiện
        setupListeners();
    }

    private void displayStory(StoryModel story) {
        storyTitle.setText(story.getTitle());
        
        // Thiết lập ViewPager2 để hiển thị các cảnh
        setupScenesViewPager(story);
    }
    
    private void setupScenesViewPager(StoryModel story) {
        scenesAdapter = new ScenesPagerAdapter(this, story.getScenes());
        scenesViewPager.setAdapter(scenesAdapter);
        
        // Lắng nghe sự kiện chuyển trang để cập nhật vị trí hiện tại
        scenesViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentScenePosition = position;
                
                // Dừng đọc nếu đang phát khi chuyển cảnh
                if (isPlaying) {
                    stopSpeaking();
                }
            }
        });
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Ưu tiên ngôn ngữ tiếng Việt
                int result = textToSpeech.setLanguage(new Locale("vi", "VN"));
                
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Ngôn ngữ không được hỗ trợ", Toast.LENGTH_SHORT).show();
                    fabPlayPause.setEnabled(false);
                }
                
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {}

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> {
                            isPlaying = false;
                            fabPlayPause.setImageResource(android.R.drawable.ic_media_play);
                            
                            // Chuyển đến cảnh tiếp theo nếu còn
                            if (currentScenePosition < story.getScenes().size() - 1 && 
                                    utteranceId.equals("scene_" + currentScenePosition)) {
                                scenesViewPager.setCurrentItem(currentScenePosition + 1, true);
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {}
                });
            } else {
                Toast.makeText(this, "Khởi tạo TTS thất bại", Toast.LENGTH_SHORT).show();
                fabPlayPause.setEnabled(false);
            }
        });
    }

    private void setupListeners() {
        // Nút play/pause
        fabPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                stopSpeaking();
            } else {
                startSpeaking();
            }
        });

        // Nút chia sẻ
        fabShare.setOnClickListener(v -> shareStory());

        // Nút lưu
        fabSave.setOnClickListener(v -> saveStory());

        // Nút tạo truyện mới
        createNewStoryButton.setOnClickListener(v -> {
            // Quay lại màn hình chọn yếu tố truyện
            Intent intent = new Intent(this, AIStoryCreatorActivity.class);
            // Xóa tất cả các activity trước đó trong stack
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void startSpeaking() {
        if (story != null && !story.getScenes().isEmpty()) {
            isPlaying = true;
            fabPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            
            // Đọc nội dung của cảnh hiện tại
            StoryModel.SceneModel currentScene = story.getScenes().get(currentScenePosition);
            String textToRead = currentScene.getVietnameseContent();
            
            // Sử dụng vị trí cảnh làm utteranceId để biết cảnh nào đã đọc xong
            textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "scene_" + currentScenePosition);
        }
    }

    private void stopSpeaking() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            isPlaying = false;
            fabPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void shareStory() {
        if (story != null) {
            StringBuilder fullContent = new StringBuilder();
            fullContent.append(story.getTitle()).append("\n\n");
            
            for (int i = 0; i < story.getScenes().size(); i++) {
                fullContent.append(story.getScenes().get(i).getVietnameseContent()).append("\n\n");
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, story.getTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT, fullContent.toString());
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ truyện"));
        }
    }

    private void saveStory() {
        // TODO: Implement save story functionality
        Toast.makeText(this, "Tính năng đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
} 
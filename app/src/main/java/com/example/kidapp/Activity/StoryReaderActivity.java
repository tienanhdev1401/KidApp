package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kidapp.R;
import com.example.kidapp.Adapter.ScenesPagerAdapter;
import com.example.kidapp.ViewModel.storyAiHistoryViewModel;
import com.example.kidapp.models.StoryByAiModel;
import com.example.kidapp.models.storyAiHistoryModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StoryReaderActivity extends AppCompatActivity {

    private TextView storyTitle;
    private ViewPager2 scenesViewPager;
    private FloatingActionButton fabPlayPause, fabShare, fabSave;
    private Button createNewStoryButton;
    private ProgressBar progressBar;
    
    private TextToSpeech textToSpeech;
    private boolean isPlaying = false;
    private StoryByAiModel story;
    private ScenesPagerAdapter scenesAdapter;
    private int currentScenePosition = 0;
    private storyAiHistoryViewModel historyViewModel;
    private boolean isFromHistory = false;
    private String storyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_reader);

        // Thiết lập toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Khởi tạo ViewModel
        historyViewModel = new ViewModelProvider(this).get(storyAiHistoryViewModel.class);

        // Khởi tạo các thành phần UI
        storyTitle = findViewById(R.id.storyTitle);
        scenesViewPager = findViewById(R.id.scenesViewPager);
        fabPlayPause = findViewById(R.id.fabPlayPause);
        fabShare = findViewById(R.id.fabShare);
        fabSave = findViewById(R.id.fabSave);
        createNewStoryButton = findViewById(R.id.createNewStoryButton);
        progressBar = findViewById(R.id.progressBar);

        // Kiểm tra xem truy cập từ History hay từ StoryCreator
        isFromHistory = getIntent().getBooleanExtra("FROM_HISTORY", false);
        storyId = getIntent().getStringExtra("STORY_ID");

        if (isFromHistory && storyId != null) {
            // Nếu mở từ lịch sử, load truyện từ Firestore
            loadStoryFromHistory(storyId);
        } else {
            // Nếu là truyện mới tạo, lấy từ Intent
            story = getIntent().getParcelableExtra("story");
            if (story != null) {
                displayStory(story);
            } else {
                Toast.makeText(this, "Không thể tải truyện", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        // Khởi tạo TextToSpeech
        initTextToSpeech();

        // Thiết lập các sự kiện
        setupListeners();
    }

    private void loadStoryFromHistory(String storyId) {
        progressBar.setVisibility(android.view.View.VISIBLE);
        
        historyViewModel.getStoryWithScenes(storyId).observe(this, historyStory -> {
            progressBar.setVisibility(android.view.View.GONE);
            
            if (historyStory != null) {
                // Chuyển đổi từ storyAiHistoryModel sang StoryByAiModel
                convertAndDisplayStory(historyStory);
            } else {
                Toast.makeText(this, "Không thể tải truyện từ lịch sử", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void convertAndDisplayStory(storyAiHistoryModel historyStory) {
        // Tạo một StoryByAiModel từ dữ liệu trong historyStory
        StoryByAiModel convertedStory = new StoryByAiModel();
        convertedStory.setTitle(historyStory.getTitle());
        
        List<StoryByAiModel.SceneModel> scenesList = new ArrayList<>();
        for (storyAiHistoryModel.SceneModel historyScene : historyStory.getScenes()) {
            StoryByAiModel.SceneModel scene = new StoryByAiModel.SceneModel();
            scene.setImageUrl(historyScene.getImageUrl());
            scene.setVietnameseContent(historyScene.getContent());
            scenesList.add(scene);
        }
        
        // Chuyển đổi List<SceneModel> thành ArrayList<SceneModel> trước khi set cho StoryByAiModel
        ArrayList<StoryByAiModel.SceneModel> scenesArrayList = new ArrayList<>(scenesList);
        convertedStory.setScenes(scenesArrayList);
        
        // Hiển thị truyện đã chuyển đổi
        story = convertedStory;
        displayStory(story);
        
        // Đánh dấu truyện đã được lưu
        fabSave.setImageResource(R.drawable.ic_saved);
        fabSave.setOnClickListener(v -> {
            Toast.makeText(this, "Truyện này đã được lưu", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayStory(StoryByAiModel story) {
        storyTitle.setText(story.getTitle());
        
        // Thiết lập ViewPager2 để hiển thị các cảnh
        setupScenesViewPager(story);
    }
    
    private void setupScenesViewPager(StoryByAiModel story) {
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
        // Xử lý sự kiện khi chuyển cảnh
        scenesViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentScenePosition = position;
                stopSpeaking();
            }
        });

        // Xử lý sự kiện khi nhấn nút play/pause
        fabPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                stopSpeaking();
            } else {
                startSpeaking();
            }
        });

        // Xử lý sự kiện khi nhấn nút chia sẻ
        fabShare.setOnClickListener(v -> {
            shareStory();
        });

        // Xử lý sự kiện khi nhấn nút lưu
        fabSave.setOnClickListener(v -> {
            saveStory();
        });

        // Xử lý sự kiện khi nhấn nút tạo truyện mới
        createNewStoryButton.setOnClickListener(v -> {
            // Quay lại màn hình tạo truyện mới
            finish();
        });
    }

    private void startSpeaking() {
        if (story != null && !story.getScenes().isEmpty()) {
            isPlaying = true;
            fabPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            
            // Đọc nội dung của cảnh hiện tại
            StoryByAiModel.SceneModel currentScene = story.getScenes().get(currentScenePosition);
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
        if (story != null) {
            progressBar.setVisibility(android.view.View.VISIBLE);
            
            // Chuyển đổi từ StoryByAiModel sang storyAiHistoryModel
            storyAiHistoryModel historyModel = new storyAiHistoryModel();
            historyModel.setTitle(story.getTitle());
            historyModel.setSceneCount(story.getScenes().size());
            historyModel.setCreatedAt(Timestamp.now());
            
            // Lấy ảnh đại diện từ cảnh đầu tiên nếu có
            if (!story.getScenes().isEmpty()) {
                historyModel.setImageUrl(story.getScenes().get(0).getImageUrl());
            }
            
            // Chuyển đổi các cảnh
            List<storyAiHistoryModel.SceneModel> scenes = new ArrayList<>();
            ArrayList<StoryByAiModel.SceneModel> storyScenes = story.getScenes();
            for (StoryByAiModel.SceneModel storyScene : storyScenes) {
                storyAiHistoryModel.SceneModel scene = new storyAiHistoryModel.SceneModel(
                        storyScene.getImageUrl(),
                        storyScene.getVietnameseContent()
                );
                scenes.add(scene);
            }
            historyModel.setScenes(scenes);
            
            // Lưu vào Firestore thông qua ViewModel
            historyViewModel.saveStory(historyModel).observe(this, success -> {
                progressBar.setVisibility(android.view.View.GONE);
                
                if (success) {
                    Toast.makeText(this, "Đã lưu truyện thành công", Toast.LENGTH_SHORT).show();
                    // Ẩn nút lưu sau khi đã lưu thành công
                    fabSave.setImageResource(R.drawable.ic_saved);
                    // Thay đổi chức năng của nút khi đã lưu
                    fabSave.setOnClickListener(v -> {
                        Toast.makeText(this, "Truyện này đã được lưu", Toast.LENGTH_SHORT).show();
                    });
                    
                    // Set kết quả để cập nhật lại Activity History
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("REFRESH_HISTORY", true);
                    setResult(RESULT_OK, resultIntent);
                } else {
                    Toast.makeText(this, "Không thể lưu truyện", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
        
        // Nếu activity đang trả về RESULT_OK, giữ nguyên
        // Nếu không, đảm bảo trả về kết quả REFRESH_HISTORY nếu activity được mở từ history
        if (isFromHistory && storyId != null && getIntent().getBooleanExtra("FROM_HISTORY", false)) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("REFRESH_HISTORY", true);
            setResult(RESULT_OK, resultIntent);
        }
        
        super.onDestroy();
    }
} 
package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kidapp.Adapter.ManualStoryReaderAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.ManualStoryViewModel;
import com.example.kidapp.models.ManualStory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class ManualStoryReaderActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager2 viewPager;
    private TextView tvTitle;
    private FloatingActionButton fabPlayPause, fabPrevious, fabNext, fabShare;
    private View loadingView;
    
    private ManualStoryViewModel viewModel;
    private ManualStoryReaderAdapter adapter;
    private ManualStory story;
    private String storyId;
    
    private TextToSpeech textToSpeech;
    private boolean isPlaying = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_story_reader);
        
        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ManualStoryViewModel.class);
        
        // Ánh xạ view
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewPager);
        tvTitle = findViewById(R.id.tvTitle);
        fabPlayPause = findViewById(R.id.fabPlayPause);
        fabPrevious = findViewById(R.id.fabPrevious);
        fabNext = findViewById(R.id.fabNext);
        fabShare = findViewById(R.id.fabShare);
        loadingView = findViewById(R.id.loadingView);
        
        // Thiết lập toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        
        // Lấy ID truyện từ Intent
        storyId = getIntent().getStringExtra("STORY_ID");
        if (storyId == null || storyId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy truyện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Khởi tạo TextToSpeech
        initTextToSpeech();
        
        // Thiết lập sự kiện click
        setupListeners();
        
        // Tải truyện
        loadStory();
        
        // Theo dõi trạng thái loading
        viewModel.getIsLoading().observe(this, isLoading -> {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }
    
    private void loadStory() {
        viewModel.getStoryById(storyId).observe(this, story -> {
            if (story != null) {
                this.story = story;
                tvTitle.setText(story.getTitle());
                setupAdapter();
            } else {
                Toast.makeText(this, "Không thể tải truyện", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void setupAdapter() {
        adapter = new ManualStoryReaderAdapter(this, story.getPages());
        viewPager.setAdapter(adapter);
        
        // Cập nhật trạng thái các nút điều hướng
        updateNavigationButtons(0);
        
        // Bắt sự kiện chuyển trang
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateNavigationButtons(position);
                
                // Dừng đọc khi chuyển trang
                if (isPlaying) {
                    stopSpeaking();
                }
            }
        });
    }
    
    private void updateNavigationButtons(int position) {
        // Disable nút Previous ở trang đầu tiên
        fabPrevious.setEnabled(position > 0);
        fabPrevious.setAlpha(position > 0 ? 1.0f : 0.5f);
        
        // Disable nút Next ở trang cuối cùng
        boolean hasNextPage = position < story.getPages().size() - 1;
        fabNext.setEnabled(hasNextPage);
        fabNext.setAlpha(hasNextPage ? 1.0f : 0.5f);
    }
    
    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Ưu tiên tiếng Việt
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
                            fabPlayPause.setImageResource(R.drawable.ic_play);
                            
                            // Chuyển đến trang tiếp theo sau khi đọc xong
                            int currentPosition = viewPager.getCurrentItem();
                            if (currentPosition < story.getPages().size() - 1) {
                                viewPager.setCurrentItem(currentPosition + 1, true);
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
        // Nút phát/dừng
        fabPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                stopSpeaking();
            } else {
                startSpeaking();
            }
        });
        
        // Nút trang trước
        fabPrevious.setOnClickListener(v -> {
            int currentPosition = viewPager.getCurrentItem();
            if (currentPosition > 0) {
                viewPager.setCurrentItem(currentPosition - 1, true);
            }
        });
        
        // Nút trang sau
        fabNext.setOnClickListener(v -> {
            int currentPosition = viewPager.getCurrentItem();
            if (currentPosition < story.getPages().size() - 1) {
                viewPager.setCurrentItem(currentPosition + 1, true);
            }
        });
        
        // Nút chia sẻ
        fabShare.setOnClickListener(v -> {
            shareStory();
        });
    }
    
    private void startSpeaking() {
        int currentPosition = viewPager.getCurrentItem();
        if (currentPosition < story.getPages().size()) {
            String content = story.getPages().get(currentPosition).getContent();
            if (content != null && !content.isEmpty()) {
                isPlaying = true;
                fabPlayPause.setImageResource(R.drawable.ic_pause);
                textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, "Page_" + currentPosition);
            } else {
                Toast.makeText(this, "Không có nội dung để đọc", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void stopSpeaking() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        isPlaying = false;
        fabPlayPause.setImageResource(R.drawable.ic_play);
    }
    
    private void shareStory() {
        int currentPosition = viewPager.getCurrentItem();
        if (currentPosition >= 0 && currentPosition < story.getPages().size()) {
            ManualStory.Page currentPage = story.getPages().get(currentPosition);
            
            // Nếu trang hiện tại có hình ảnh, chia sẻ hình ảnh kèm với nội dung
            if (currentPage.getImageUrl() != null && currentPage.getImageUrl().startsWith("file://")) {
                try {
                    String imagePath = currentPage.getImageUrl().substring(7); // Bỏ "file://"
                    java.io.File imageFile = new java.io.File(imagePath);
                    
                    if (imageFile.exists()) {
                        // Tạo URI thông qua FileProvider để tuân thủ Android 7.0+
                        android.net.Uri imageUri = androidx.core.content.FileProvider.getUriForFile(
                                this,
                                getPackageName() + ".provider",
                                imageFile);
                        
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                        
                        // Thêm nội dung văn bản
                        String textContent = story.getTitle() + "\n\n" + 
                                            "Trang " + (currentPosition + 1) + ":\n" + 
                                            currentPage.getContent();
                        shareIntent.putExtra(Intent.EXTRA_TEXT, textContent);
                        
                        // Cấp quyền đọc cho các ứng dụng nhận
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        startActivity(Intent.createChooser(shareIntent, "Chia sẻ trang truyện"));
                        return;
                    }
                } catch (Exception e) {
                    // Nếu chia sẻ hình ảnh lỗi, quay lại chia sẻ văn bản
                    Toast.makeText(this, "Không thể chia sẻ hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            
            // Chia sẻ nội dung văn bản nếu không có hình ảnh hoặc chia sẻ hình ảnh thất bại
            StringBuilder storyContent = new StringBuilder();
            storyContent.append(story.getTitle()).append("\n\n");
            storyContent.append("Trang ").append(currentPosition + 1).append(":\n");
            storyContent.append(currentPage.getContent());
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, story.getTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT, storyContent.toString());
            
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ trang truyện"));
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
        super.onDestroy();
    }
} 
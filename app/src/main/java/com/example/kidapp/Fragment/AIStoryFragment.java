package com.example.kidapp.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.Activity.StoryCreationActivity;
import com.example.kidapp.Activity.StoryReaderActivity;
import com.example.kidapp.Adapter.storyAiHistoryAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.storyAiHistoryViewModel;
import com.example.kidapp.models.storyAiHistoryModel;

public class AIStoryFragment extends Fragment implements storyAiHistoryAdapter.OnStoryClickListener, storyAiHistoryAdapter.OnDeleteClickListener {

    private RecyclerView recyclerViewStories;
    private storyAiHistoryAdapter adapter;
    private storyAiHistoryViewModel viewModel;
    private LinearLayout emptyView;
    private ProgressBar progressBar;
    private Button btnCreateFirst;
    private static final int REQUEST_CODE_STORY_READER = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_list, container, false);
        
        // Ánh xạ các view
        recyclerViewStories = view.findViewById(R.id.recyclerViewStories);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);
        btnCreateFirst = view.findViewById(R.id.btnCreateFirst);
        
        // Thiết lập nút tạo truyện đầu tiên
        btnCreateFirst.setOnClickListener(v -> navigateToStoryCreation());
        
        // Thiết lập RecyclerView
        recyclerViewStories.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new storyAiHistoryAdapter(getContext(), this, this);
        recyclerViewStories.setAdapter(adapter);
        
        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(storyAiHistoryViewModel.class);
        
        // Theo dõi trạng thái loading
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Thiết lập subtitle cho empty view
        View tvEmptySubtitle = view.findViewById(R.id.tvEmptySubtitle);
        if (tvEmptySubtitle != null) {
            ((android.widget.TextView) tvEmptySubtitle).setText("Hãy nhấn nút + để tạo truyện AI mới nhé!");
        }
        
        // Thiết lập tiêu đề cho empty view
        View tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        if (tvEmptyTitle != null) {
            ((android.widget.TextView) tvEmptyTitle).setText("Bạn chưa có truyện AI nào");
        }
        
        // Load dữ liệu
        loadStories();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Làm mới danh sách truyện khi quay lại từ màn hình khác
        if (viewModel != null) {
            viewModel.refreshStories();
        }
    }
    
    private void loadStories() {
        viewModel.getAllStories().observe(getViewLifecycleOwner(), stories -> {
            adapter.setStories(stories);
            
            // Hiển thị emptyView nếu không có truyện nào
            if (stories == null || stories.isEmpty()) {
                recyclerViewStories.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerViewStories.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });
    }
    
    @Override
    public void onStoryClick(storyAiHistoryModel story) {
        // Chuyển đến màn hình đọc truyện với story đã chọn
        navigateToStoryReader(story);
    }
    
    @Override
    public void onDeleteClick(storyAiHistoryModel story) {
        // Hiển thị dialog xác nhận xóa
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa truyện")
                .setMessage("Bạn có chắc muốn xóa truyện này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteStory(story);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void deleteStory(storyAiHistoryModel story) {
        viewModel.deleteStory(story.getId()).observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Đã xóa truyện", Toast.LENGTH_SHORT).show();
                // Xóa truyện khỏi adapter trước để cập nhật UI ngay lập tức
                adapter.removeStory(story.getId());
                // Sau đó làm mới danh sách từ Firebase
                viewModel.refreshStories();
            } else {
                Toast.makeText(getContext(), "Không thể xóa truyện", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void navigateToStoryReader(storyAiHistoryModel story) {
        Intent intent = new Intent(getContext(), StoryReaderActivity.class);
        // Truyền ID của truyện để StoryReaderActivity có thể load dữ liệu
        intent.putExtra("STORY_ID", story.getId());
        intent.putExtra("FROM_HISTORY", true);
        startActivityForResult(intent, REQUEST_CODE_STORY_READER);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_STORY_READER && resultCode == android.app.Activity.RESULT_OK) {
            if (data != null && data.getBooleanExtra("REFRESH_HISTORY", false)) {
                // Làm mới danh sách nếu có thay đổi (lưu hoặc xóa)
                viewModel.refreshStories();
            }
        }
    }
    
    private void navigateToStoryCreation() {
        // Chuyển đến màn hình tạo truyện mới (trước khi đến StoryReaderActivity)
        Intent intent = new Intent(getContext(), StoryCreationActivity.class);
        startActivity(intent);
    }
} 
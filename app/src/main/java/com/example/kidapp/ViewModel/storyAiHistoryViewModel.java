package com.example.kidapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kidapp.Repository.storyAiHistoryRepository;
import com.example.kidapp.models.storyAiHistoryModel;

import java.util.List;

public class storyAiHistoryViewModel extends ViewModel {
    private final storyAiHistoryRepository repository;
    private MutableLiveData<List<storyAiHistoryModel>> allStories;
    private MutableLiveData<storyAiHistoryModel> selectedStory;
    private MutableLiveData<Boolean> saveResult;
    private MutableLiveData<Boolean> deleteResult;
    private MutableLiveData<Boolean> isLoading;

    public storyAiHistoryViewModel() {
        repository = new storyAiHistoryRepository();
        isLoading = new MutableLiveData<>(false);
    }

    // Lấy danh sách tất cả truyện
    public LiveData<List<storyAiHistoryModel>> getAllStories() {
        isLoading.setValue(true);
        if (allStories == null) {
            allStories = repository.getAllStories();
        }
        isLoading.setValue(false);
        return allStories;
    }

    // Làm mới danh sách truyện
    public void refreshStories() {
        isLoading.setValue(true);
        
        // Tạo một observer tạm thời để lấy dữ liệu mới nhất
        MutableLiveData<List<storyAiHistoryModel>> tempData = repository.getAllStories();
        
        // Sử dụng một observer cụ thể để có thể gỡ bỏ sau khi hoàn thành
        androidx.lifecycle.Observer<List<storyAiHistoryModel>> tempObserver = new androidx.lifecycle.Observer<List<storyAiHistoryModel>>() {
            @Override
            public void onChanged(List<storyAiHistoryModel> stories) {
                // Cập nhật dữ liệu vào allStories
                if (stories != null) {
                    if (allStories == null) {
                        allStories = new MutableLiveData<>();
                    }
                    allStories.setValue(stories);
                }
                
                // Giải phóng observer để tránh rò rỉ bộ nhớ
                tempData.removeObserver(this);
                isLoading.setValue(false);
            }
        };
        
        // Đăng ký observer
        tempData.observeForever(tempObserver);
    }

    // Lưu truyện mới
    public LiveData<Boolean> saveStory(storyAiHistoryModel story) {
        isLoading.setValue(true);
        saveResult = repository.saveStory(story);
        saveResult.observeForever(result -> {
            if (result != null) {
                isLoading.setValue(false);
            }
        });
        return saveResult;
    }

    // Lấy thông tin chi tiết của một truyện
    public LiveData<storyAiHistoryModel> getStoryWithScenes(String storyId) {
        isLoading.setValue(true);
        selectedStory = repository.getStoryWithScenes(storyId);
        selectedStory.observeForever(story -> {
            isLoading.setValue(false);
        });
        return selectedStory;
    }

    // Xóa truyện
    public LiveData<Boolean> deleteStory(String storyId) {
        isLoading.setValue(true);
        deleteResult = repository.deleteStory(storyId);
        deleteResult.observeForever(result -> {
            if (result != null) {
                isLoading.setValue(false);
            }
        });
        return deleteResult;
    }

    // Trạng thái loading
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
} 
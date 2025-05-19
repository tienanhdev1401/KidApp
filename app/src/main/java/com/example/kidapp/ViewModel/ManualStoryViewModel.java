package com.example.kidapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kidapp.Repository.ManualStoryRepository;
import com.example.kidapp.models.ManualStory;

import java.io.File;
import java.util.List;

public class ManualStoryViewModel extends ViewModel {
    private final ManualStoryRepository repository;
    private MutableLiveData<List<ManualStory>> allStories;
    private MutableLiveData<ManualStory> currentStory;
    private MutableLiveData<Boolean> saveResult;
    private MutableLiveData<Boolean> deleteResult;
    private MutableLiveData<String> uploadImageResult;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;

    public ManualStoryViewModel() {
        repository = new ManualStoryRepository();
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<List<ManualStory>> getAllStories() {
        isLoading.setValue(true);
        if (allStories == null) {
            allStories = new MutableLiveData<>();
            loadAllStories();
        }
        return allStories;
    }

    public void refreshStories() {
        isLoading.setValue(true);
        loadAllStories();
    }

    private void loadAllStories() {
        MutableLiveData<List<ManualStory>> tempData = repository.getAllStories();
        tempData.observeForever(stories -> {
            allStories.setValue(stories);
            isLoading.setValue(false);
            tempData.removeObserver(stories1 -> {});
        });
    }

    public LiveData<ManualStory> getStoryById(String storyId) {
        isLoading.setValue(true);
        currentStory = repository.getStoryById(storyId);
        currentStory.observeForever(story -> {
            isLoading.setValue(false);
        });
        return currentStory;
    }

    public LiveData<Boolean> saveStory(ManualStory story) {
        isLoading.setValue(true);
        saveResult = repository.saveStory(story);
        saveResult.observeForever(result -> {
            if (result != null) {
                isLoading.setValue(false);
                if (!result) {
                    errorMessage.setValue("Không thể lưu truyện. Vui lòng thử lại.");
                }
            }
        });
        return saveResult;
    }

    public LiveData<Boolean> deleteStory(String storyId) {
        isLoading.setValue(true);
        deleteResult = repository.deleteStory(storyId);
        deleteResult.observeForever(result -> {
            if (result != null) {
                isLoading.setValue(false);
                if (!result) {
                    errorMessage.setValue("Không thể xóa truyện. Vui lòng thử lại.");
                }
            }
        });
        return deleteResult;
    }

    public LiveData<String> uploadImage(File imageFile) {
        // Thay vì upload vào Firebase Storage, chỉ trả về đường dẫn cục bộ
        MutableLiveData<String> result = new MutableLiveData<>();
        if (imageFile != null && imageFile.exists()) {
            result.setValue("file://" + imageFile.getAbsolutePath());
        } else {
            result.setValue("");
            errorMessage.setValue("Không thể tải lên hình ảnh vì file không tồn tại.");
        }
        return result;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void createNewStory(String title) {
        ManualStory story = new ManualStory();
        story.setTitle(title);
        currentStory = new MutableLiveData<>(story);
    }

    public ManualStory getCurrentStory() {
        if (currentStory == null || currentStory.getValue() == null) {
            createNewStory("Truyện mới");
        }
        return currentStory.getValue();
    }

    public void setCurrentStory(ManualStory story) {
        if (currentStory == null) {
            currentStory = new MutableLiveData<>();
        }
        currentStory.setValue(story);
    }

    public void addPage(ManualStory.Page page) {
        ManualStory story = getCurrentStory();
        story.addPage(page);
        currentStory.setValue(story);
    }

    public void updatePage(int position, ManualStory.Page updatedPage) {
        ManualStory story = getCurrentStory();
        if (position >= 0 && position < story.getPages().size()) {
            story.getPages().set(position, updatedPage);
            story.setLastModifiedTimestamp(System.currentTimeMillis());
            currentStory.setValue(story);
        }
    }

    public void removePage(int position) {
        ManualStory story = getCurrentStory();
        story.removePage(position);
        currentStory.setValue(story);
    }

    public void movePage(int fromPosition, int toPosition) {
        ManualStory story = getCurrentStory();
        story.movePage(fromPosition, toPosition);
        currentStory.setValue(story);
    }
} 
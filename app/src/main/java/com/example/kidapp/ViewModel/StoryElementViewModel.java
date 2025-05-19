package com.example.kidapp.ViewModel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.kidapp.Repository.StoryElementRepository;
import com.example.kidapp.models.StoryElement;

import java.util.List;

public class StoryElementViewModel extends ViewModel {
    private final StoryElementRepository repository;
    
    private final MutableLiveData<List<StoryElement>> characters = new MutableLiveData<>();
    private final MutableLiveData<List<StoryElement>> settings = new MutableLiveData<>();
    private final MutableLiveData<List<StoryElement>> items = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public StoryElementViewModel(StoryElementRepository repository) {
        this.repository = repository;
        loadAllElements();
    }

    private void loadAllElements() {
        isLoading.setValue(true);

        // Load characters
        repository.getElementsByType(StoryElement.ElementType.CHARACTER)
                .addOnSuccessListener(characters::setValue)
                .addOnFailureListener(e -> error.setValue("Không thể tải nhân vật: " + e.getMessage()));

        // Load settings
        repository.getElementsByType(StoryElement.ElementType.SETTING)
                .addOnSuccessListener(settings::setValue)
                .addOnFailureListener(e -> error.setValue("Không thể tải bối cảnh: " + e.getMessage()));

        // Load items
        repository.getElementsByType(StoryElement.ElementType.ITEM)
                .addOnSuccessListener(result -> {
                    items.setValue(result);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    error.setValue("Không thể tải vật phẩm: " + e.getMessage());
                    isLoading.setValue(false);
                });
    }

    public void refreshData() {
        isLoading.setValue(true);
        
        Log.d("ManualStory", "Bắt đầu refreshData trong StoryElementViewModel");
        
        // Sử dụng một bộ đếm để theo dõi khi nào tất cả các truy vấn đã hoàn thành
        final int[] completedQueries = {0};
        final int totalQueries = 3; // tổng cộng 3 truy vấn (characters, settings, items)
        
        // Hàm kiểm tra để đặt isLoading = false khi tất cả truy vấn đã hoàn thành
        Runnable checkAllQueriesCompleted = () -> {
            completedQueries[0]++;
            if (completedQueries[0] >= totalQueries) {
                Log.d("ManualStory", "Tất cả truy vấn đã hoàn thành. Đặt isLoading = false");
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> isLoading.setValue(false));
            }
        };

        // Load characters
        repository.getElementsByType(StoryElement.ElementType.CHARACTER)
                .addOnSuccessListener(charactersList -> {
                    Log.d("ManualStory", "Tải thành công " + charactersList.size() + " nhân vật");
                    // Đảm bảo cập nhật UI trên main thread
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> characters.setValue(charactersList));
                    checkAllQueriesCompleted.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("ManualStory", "Lỗi tải nhân vật: " + e.getMessage());
                    error.setValue("Không thể tải nhân vật: " + e.getMessage());
                    checkAllQueriesCompleted.run();
                });

        // Load settings
        repository.getElementsByType(StoryElement.ElementType.SETTING)
                .addOnSuccessListener(settingsList -> {
                    Log.d("ManualStory", "Tải thành công " + settingsList.size() + " bối cảnh");
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> settings.setValue(settingsList));
                    checkAllQueriesCompleted.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("ManualStory", "Lỗi tải bối cảnh: " + e.getMessage());
                    error.setValue("Không thể tải bối cảnh: " + e.getMessage());
                    checkAllQueriesCompleted.run();
                });

        // Load items
        repository.getElementsByType(StoryElement.ElementType.ITEM)
                .addOnSuccessListener(itemsList -> {
                    Log.d("ManualStory", "Tải thành công " + itemsList.size() + " vật phẩm");
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> items.setValue(itemsList));
                    checkAllQueriesCompleted.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("ManualStory", "Lỗi tải vật phẩm: " + e.getMessage());
                    error.setValue("Không thể tải vật phẩm: " + e.getMessage());
                    checkAllQueriesCompleted.run();
                });
    }

    public LiveData<List<StoryElement>> getCharacters() {
        return characters;
    }

    public LiveData<List<StoryElement>> getSettings() {
        return settings;
    }

    public LiveData<List<StoryElement>> getItems() {
        return items;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public static class Factory implements ViewModelProvider.Factory {
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(StoryElementViewModel.class)) {
                return (T) new StoryElementViewModel(new StoryElementRepository());
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
} 
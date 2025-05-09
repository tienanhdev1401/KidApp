package com.example.kidapp.ViewModel;
import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kidapp.Repository.PuzzleRepository;

import com.example.kidapp.models.Puzzle;
import com.example.kidapp.models.User;

import java.util.List;
public class PuzzleViewModel extends AndroidViewModel {
    private final PuzzleRepository puzzleRepository;
    public PuzzleViewModel(Application application) {
        super(application);
        puzzleRepository = new PuzzleRepository(application);
    }
    // Get all puzzles
    public LiveData<List<Puzzle>> getAllPuzzles() {
        Log.d("ALL puzzle", "Đang lấy danh sách puzzle");
        return puzzleRepository.getAllPuzzles();
    }
    
    // Refresh puzzles
    public void refreshPuzzles() {
        Log.d("REFRESH", "Đang làm mới danh sách puzzle");
        puzzleRepository.refreshPuzzles();
    }
}

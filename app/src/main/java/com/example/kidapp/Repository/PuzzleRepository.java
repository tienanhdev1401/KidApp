package com.example.kidapp.Repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kidapp.models.Puzzle;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PuzzleRepository {
    private final FirebaseFirestore db;
    private final MutableLiveData<List<Puzzle>> puzzlesLiveData;

    public PuzzleRepository(Application application) {
        db = FirebaseFirestore.getInstance();
        puzzlesLiveData = new MutableLiveData<>();
        loadPuzzles();
    }

    public LiveData<List<Puzzle>> getAllPuzzles() {
        if (puzzlesLiveData.getValue() == null) {
            loadPuzzles();
        }
        return puzzlesLiveData;
    }
    
    public void refreshPuzzles() {
        loadPuzzles();
    }
    
    private void loadPuzzles() {
        db.collection("puzzle")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Puzzle> puzzleList = queryDocumentSnapshots.toObjects(Puzzle.class);
                        puzzlesLiveData.setValue(puzzleList);
                        Log.d("PUZZLE_REPO", "Loaded " + puzzleList.size() + " puzzles");
                    } else {
                        puzzlesLiveData.setValue(null); // If no puzzles found
                        Log.d("PUZZLE_REPO", "No puzzles found");
                    }
                })
                .addOnFailureListener(e -> {
                    puzzlesLiveData.setValue(null);
                    Log.e("PUZZLE_REPO", "Error loading puzzles", e);
                });
    }
}

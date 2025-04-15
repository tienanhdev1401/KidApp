package com.example.kidapp.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewTreeObserver;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kidapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GameXepHinhActivity extends AppCompatActivity {

    private static final int COLUMNS = 3;
    private static final int ROWS = 3;
    private static final int EMPTY_TILE = ROWS * COLUMNS - 1; // The last tile (8) is empty

    private GridLayout puzzleGrid;
    private TextView timerTextView;
    private ImageView previewImageView;
    private Button newGameButton, hintButton;

    private Button[][] buttons;
    private int[][] tiles; // Current state of the puzzle
    private int[][] solvedTiles; // Correct state of the puzzle

    private Bitmap originalImage;
    private Bitmap[] tileBitmaps;

    private boolean isGameStarted = false;
    private int seconds = 0;
    private Handler timerHandler;
    private Runnable timerRunnable;

    private int emptyTileRow;
    private int emptyTileCol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_xep_hinh);

        // Initialize views
        puzzleGrid = findViewById(R.id.puzzleGrid);
        timerTextView = findViewById(R.id.timerTextView);
        previewImageView = findViewById(R.id.previewImageView);
        newGameButton = findViewById(R.id.newGameButton);
        hintButton = findViewById(R.id.hintButton);

        // Setup timer
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                seconds++;
                int minutes = seconds / 60;
                int secs = seconds % 60;
                timerTextView.setText(String.format(Locale.getDefault(), "Thời gian: %02d:%02d", minutes, secs));
                timerHandler.postDelayed(this, 1000);
            }
        };

        // Setup buttons
        newGameButton.setOnClickListener(v -> startNewGame());
        hintButton.setOnClickListener(v -> showHint());

        // Initialize game - wait for layout to be drawn first
        puzzleGrid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                puzzleGrid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                initializePuzzle();
                startNewGame();
            }
        });
        ImageView bntBack = findViewById(R.id.btn_Back);
        bntBack.setOnClickListener(v-> finish());

    }

    private void initializePuzzle() {
        // Load and setup the image
        originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.puzzle_image);
        previewImageView.setImageBitmap(originalImage);

        // Initialize arrays
        buttons = new Button[ROWS][COLUMNS];
        tiles = new int[ROWS][COLUMNS];
        solvedTiles = new int[ROWS][COLUMNS];

        // Create the solved state
        int count = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                solvedTiles[i][j] = count++;
            }
        }

        // Create tile bitmaps
        createTileBitmaps();

        // Create buttons for the grid
        createButtons();
    }

    private void createTileBitmaps() {
        int width = originalImage.getWidth() / COLUMNS;
        int height = originalImage.getHeight() / ROWS;

        tileBitmaps = new Bitmap[ROWS * COLUMNS];

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                int index = i * COLUMNS + j;

                if (index < EMPTY_TILE) {
                    // Calculate the region to crop from the original image
                    int x = j * width;
                    int y = i * height;

                    // Create a tile from the original image
                    tileBitmaps[index] = Bitmap.createBitmap(originalImage, x, y, width, height);
                } else {
                    // Create an empty (transparent) bitmap for the last tile
                    tileBitmaps[index] = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                }
            }
        }
    }

    private void createButtons() {
        puzzleGrid.removeAllViews();

        int gridWidth = puzzleGrid.getWidth();
        int gridHeight = puzzleGrid.getHeight();

        int buttonWidth = gridWidth / COLUMNS;
        int buttonHeight = gridHeight / ROWS;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                Button button = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonWidth;
                params.height = buttonHeight;
                params.rowSpec = GridLayout.spec(i);
                params.columnSpec = GridLayout.spec(j);
                button.setLayoutParams(params);

                final int row = i;
                final int col = j;

                button.setOnClickListener(v -> onTileClick(row, col));

                buttons[i][j] = button;
                puzzleGrid.addView(button);
            }
        }
    }

    private void startNewGame() {
        // Reset timer
        timerHandler.removeCallbacks(timerRunnable);
        seconds = 0;
        timerTextView.setText("Thời gian: 00:00");

        // Shuffle tiles
        List<Integer> tilesList = new ArrayList<>();
        for (int i = 0; i < ROWS * COLUMNS; i++) {
            tilesList.add(i);
        }

        // Make sure the puzzle is solvable by making an even number of inversions
        do {
            Collections.shuffle(tilesList);
        } while (!isSolvable(tilesList));

        // Set up the initial state
        int index = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                tiles[i][j] = tilesList.get(index++);

                if (tiles[i][j] == EMPTY_TILE) {
                    emptyTileRow = i;
                    emptyTileCol = j;
                }
            }
        }

        // Update UI
        updateButtonImages();

        // Start timer
        isGameStarted = true;
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    // Fisher-Yates shuffle
    private boolean isSolvable(List<Integer> tiles) {
        int inversions = 0;
        int emptyPosition = 0;

        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i) == EMPTY_TILE) {
                emptyPosition = i;
                continue;
            }

            for (int j = i + 1; j < tiles.size(); j++) {
                if (tiles.get(j) != EMPTY_TILE && tiles.get(i) > tiles.get(j)) {
                    inversions++;
                }
            }
        }

        // For a 3x3 puzzle:
        // If the empty tile is in an even row counting from the bottom,
        // then the puzzle is solvable if inversions is odd
        // If the empty tile is in an odd row counting from the bottom,
        // then the puzzle is solvable if inversions is even
        int emptyRow = emptyPosition / COLUMNS;
        int emptyRowFromBottom = ROWS - 1 - emptyRow;

        return (emptyRowFromBottom % 2 == 0 && inversions % 2 == 1) ||
                (emptyRowFromBottom % 2 == 1 && inversions % 2 == 0);
    }

    private void updateButtonImages() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                int tileIndex = tiles[i][j];

                if (tileIndex == EMPTY_TILE) {
                    buttons[i][j].setBackground(null);
                    buttons[i][j].setText("");
                } else {
                    buttons[i][j].setBackground(new BitmapDrawable(getResources(), tileBitmaps[tileIndex]));
                    buttons[i][j].setText("");
                }
            }
        }
    }

    private void onTileClick(int row, int col) {
        if (!isGameStarted) return;

        // Check if the clicked tile is adjacent to the empty tile
        if ((Math.abs(row - emptyTileRow) == 1 && col == emptyTileCol) ||
                (Math.abs(col - emptyTileCol) == 1 && row == emptyTileRow)) {

            // Swap the tiles
            tiles[emptyTileRow][emptyTileCol] = tiles[row][col];
            tiles[row][col] = EMPTY_TILE;

            // Update empty tile position
            emptyTileRow = row;
            emptyTileCol = col;

            // Update UI
            updateButtonImages();

            // Check if the puzzle is solved
            if (isPuzzleSolved()) {
                gameWon();
            }
        }
    }

    private boolean isPuzzleSolved() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (tiles[i][j] != solvedTiles[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void gameWon() {
        // Stop the timer
        timerHandler.removeCallbacks(timerRunnable);
        isGameStarted = false;

        // Show congratulation dialog
        new AlertDialog.Builder(this)
                .setTitle("Chúc mừng!")
                .setMessage("Bạn đã hoàn thành trò chơi trong " + formatTime(seconds) + "!")
                .setPositiveButton("Chơi lại", (dialog, which) -> startNewGame())
                .setNegativeButton("Thoát", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void showHint() {
        if (!isGameStarted) return;

        // Show the original image for 3 seconds
        puzzleGrid.setVisibility(View.INVISIBLE);
        previewImageView.setLayoutParams(puzzleGrid.getLayoutParams());
        previewImageView.setScaleType(ImageView.ScaleType.FIT_XY);

        Toast.makeText(this, "Ghi nhớ hình ảnh!", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> {
            puzzleGrid.setVisibility(View.VISIBLE);
            previewImageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.preview_width),
                    getResources().getDimensionPixelSize(R.dimen.preview_height)));
            previewImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop the timer when the activity is paused
        if (isGameStarted) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the timer when the activity is resumed
        if (isGameStarted) {
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }
}
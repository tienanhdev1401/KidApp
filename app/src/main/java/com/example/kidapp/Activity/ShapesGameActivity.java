package com.example.kidapp.Activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.kidapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ShapesGameActivity extends AppCompatActivity {

    private FrameLayout dropZoneContainer;
    private ImageView triangleShape, hexagonShape, circleShape, rectangleShape;
    private FrameLayout squareDropZone, triangleDropZone, hexagonDropZone, circleDropZone;
    private ImageView reloadButton, backButton;

    private Map<Integer, Integer> shapeToDropZoneMap;
    private List<FrameLayout> dropZones;
    private List<ImageView> shapes;
    private Random random;
    
    // Sound effects
    private MediaPlayer correctSound;
    private MediaPlayer incorrectSound;
    private MediaPlayer successSound;
    private MediaPlayer clickSound;
    
    // Track completed drop zones
    private int completedDropZones = 0;
    private boolean isGameCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shapes_game);

        initViews();
        initSounds();
        setupDragListeners();
        setupDropZoneListeners();
        setupButtons();

        // Initial animations
        animateShapes();
    }

    private void initViews() {
        // Initialize containers
        dropZoneContainer = findViewById(R.id.dropZoneContainer);

        // Initialize buttons
        reloadButton = findViewById(R.id.reloadButton);
        backButton = findViewById(R.id.backButton);

        // Initialize shapes (draggable)
        triangleShape = findViewById(R.id.triangleShape);
        hexagonShape = findViewById(R.id.hexagonShape);
        circleShape = findViewById(R.id.circleShape);
        rectangleShape = findViewById(R.id.rectangleShape);

        // Initialize drop zones
        squareDropZone = findViewById(R.id.squareDropZone);
        triangleDropZone = findViewById(R.id.triangleDropZone);
        hexagonDropZone = findViewById(R.id.hexagonDropZone);
        circleDropZone = findViewById(R.id.circleDropZone);

        // Create collections for randomization
        dropZones = new ArrayList<>();
        dropZones.add(squareDropZone);
        dropZones.add(triangleDropZone);
        dropZones.add(hexagonDropZone);
        dropZones.add(circleDropZone);

        shapes = new ArrayList<>();
        shapes.add(triangleShape);
        shapes.add(hexagonShape);
        shapes.add(circleShape);
        shapes.add(rectangleShape);

        // Map shapes to their corresponding drop zones
        shapeToDropZoneMap = new HashMap<>();
        shapeToDropZoneMap.put(R.id.triangleShape, R.id.triangleDropZone);
        shapeToDropZoneMap.put(R.id.hexagonShape, R.id.hexagonDropZone);
        shapeToDropZoneMap.put(R.id.circleShape, R.id.circleDropZone);
        shapeToDropZoneMap.put(R.id.rectangleShape, R.id.squareDropZone);

        random = new Random();
    }
    
    private void initSounds() {
        // Initialize sound effects
        correctSound = MediaPlayer.create(this, R.raw.correct_sound);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect_sound);
        successSound = MediaPlayer.create(this, R.raw.celebration_sound);
        clickSound = MediaPlayer.create(this, R.raw.correct_sound);
    }

    private void setupButtons() {
        reloadButton.setOnClickListener(v -> {
            playSound(clickSound);
            animateButton(reloadButton);
            resetGame();
        });

        backButton.setOnClickListener(v -> {
            playSound(clickSound);
            animateButton(backButton);
            finish();
        });
    }
    
    private void animateButton(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.start();
    }

    private void setupDragListeners() {
        // Set touch listeners for all shape views to enable dragging
        View.OnTouchListener touchListener = (view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                animatePickUp(view);
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDragAndDrop(data, shadowBuilder, view, 0);
                return true;
            }
            return false;
        };

        triangleShape.setOnTouchListener(touchListener);
        hexagonShape.setOnTouchListener(touchListener);
        circleShape.setOnTouchListener(touchListener);
        rectangleShape.setOnTouchListener(touchListener);
    }
    
    private void animatePickUp(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(200);
        animatorSet.start();
    }
    
    private void animatePutDown(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.2f, 1f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(200);
        animatorSet.start();
    }

    private void setupDropZoneListeners() {
        // Set drag listeners for all drop zones
        View.OnDragListener dragListener = new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        // Handle the drop event
                        View draggedView = (View) event.getLocalState();
                        animatePutDown(draggedView);

                        // Check if this is the correct drop zone for this shape
                        if (shapeToDropZoneMap.get(draggedView.getId()) == v.getId()) {
                            // Create a copy of the dragged shape and add it to the drop zone
                            ImageView newShapeView = new ImageView(ShapesGameActivity.this);
                            newShapeView.setImageDrawable(((ImageView) draggedView).getDrawable());
                            newShapeView.setLayoutParams(new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT));
                            newShapeView.setPadding(10, 10, 10, 10);

                            // Check if this drop zone was already filled
                            boolean wasAlreadyFilled = ((FrameLayout) v).getChildCount() > 0;
                            
                            // Clear any previous views in the drop zone and add the new one
                            ((FrameLayout) v).removeAllViews();
                            ((FrameLayout) v).addView(newShapeView);
                            
                            // Animate the placed shape
                            animateCorrectPlacement(newShapeView);

                            // Play sound and update completion tracking
                            if (!wasAlreadyFilled) {
                                completedDropZones++;
                                playSound(correctSound);
                            }
                            
                            // Check if all shapes are placed
                            checkGameComplete();
                        } else {
                            // Animate and provide feedback for incorrect placement
                            animateIncorrectPlacement(v);
                            playSound(incorrectSound);
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        // Visual feedback when entering a valid drop zone
                        animateDropZoneHighlight(v, true);
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        // Reset visual feedback when exiting
                        animateDropZoneHighlight(v, false);
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        // Reset all visual feedback when drag ends
                        v.setAlpha(1.0f);
                        return true;
                }
                return true;
            }
        };

        // Apply the drag listener to all drop zones
        squareDropZone.setOnDragListener(dragListener);
        triangleDropZone.setOnDragListener(dragListener);
        hexagonDropZone.setOnDragListener(dragListener);
        circleDropZone.setOnDragListener(dragListener);
    }
    
    private void animateDropZoneHighlight(View v, boolean highlight) {
        float toAlpha = highlight ? 0.7f : 1.0f;
        float toScale = highlight ? 1.1f : 1.0f;
        
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(v, "alpha", v.getAlpha(), toAlpha);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", v.getScaleX(), toScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", v.getScaleY(), toScale);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnim, scaleX, scaleY);
        animatorSet.setDuration(200);
        animatorSet.start();
    }
    
    private void animateCorrectPlacement(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1.1f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1.1f, 1.0f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 20f, -20f, 10f, -10f, 0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, rotation);
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new BounceInterpolator());
        animatorSet.start();
    }
    
    private void animateIncorrectPlacement(View view) {
        ObjectAnimator translateX = ObjectAnimator.ofFloat(view, "translationX", 0, -10, 10, -10, 10, -5, 5, 0);
        translateX.setDuration(500);
        translateX.start();
    }
    
    private void playSound(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            // Rewind the media player
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    }

    private void randomizeDropZones() {
        // Reset game state
        completedDropZones = 0;
        isGameCompleted = false;

        // Clear all drop zones
        for (FrameLayout dropZone : dropZones) {
            dropZone.removeAllViews();
            dropZone.setScaleX(1f);
            dropZone.setScaleY(1f);
            dropZone.setAlpha(1f);
        }

        // Get container dimensions
        int containerWidth = dropZoneContainer.getWidth();
        int containerHeight = dropZoneContainer.getHeight();

        // Make drop zones larger - use 2.5 instead of 3 to make them bigger
        int dropZoneSize = (int)(Math.min(containerWidth, containerHeight) / 1.5);
        int padding = 40;
        
        // Create a zigzag pattern arrangement
        List<Point> zigzagPositions = new ArrayList<>();
        
        // First shape (top left)
        zigzagPositions.add(new Point(
                padding,
                (int)(containerHeight/7.3)));
                
        // Second shape (top right)
        zigzagPositions.add(new Point(
                containerWidth - dropZoneSize - padding,
                (int)(containerHeight / 2.2 - dropZoneSize / 2)));
                
        // Third shape (middle left)
        zigzagPositions.add(new Point(
                (int)(padding * 1),
                (int)(containerHeight / 1.8)));
                
        // Fourth shape (bottom right)
        zigzagPositions.add(new Point(
                containerWidth - dropZoneSize - padding,
                containerHeight - dropZoneSize - padding));

        // Shuffle the drop zones so shapes appear in random positions
        Collections.shuffle(dropZones, random);

        // Apply positions to each drop zone
        for (int i = 0; i < dropZones.size(); i++) {
            if (i >= zigzagPositions.size()) break;

            FrameLayout dropZone = dropZones.get(i);
            Point position = zigzagPositions.get(i);

            // Set new layout parameters
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    dropZoneSize, dropZoneSize);
            params.leftMargin = position.x;
            params.topMargin = position.y;
            dropZone.setLayoutParams(params);

            // Make drop zone visible and apply entry animation
            dropZone.setVisibility(View.VISIBLE);
            dropZone.setAlpha(0f);
            dropZone.setScaleX(0.5f);
            dropZone.setScaleY(0.5f);

            // Stagger the animations for a nice effect
            new Handler().postDelayed(() -> {
                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(dropZone, "alpha", 0f, 1f);
                ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(dropZone, "scaleX", 0.5f, 1.1f, 1f);
                ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(dropZone, "scaleY", 0.5f, 1.1f, 1f);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(alphaAnim, scaleXAnim, scaleYAnim);
                animatorSet.setDuration(500);
                animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorSet.start();
            }, i * 150); // Stagger the animations
        }
    }

    private void animateShapes() {
        for (int i = 0; i < shapes.size(); i++) {
            ImageView shape = shapes.get(i);
            
            // Initial state
            shape.setAlpha(0f);
            shape.setScaleX(0.5f);
            shape.setScaleY(0.5f);
            
            // Stagger the animations
            new Handler().postDelayed(() -> {
                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(shape, "alpha", 0f, 1f);
                ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(shape, "scaleX", 0.5f, 1.1f, 1f);
                ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(shape, "scaleY", 0.5f, 1.1f, 1f);
                
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(alphaAnim, scaleXAnim, scaleYAnim);
                animatorSet.setDuration(400);
                animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorSet.start();
                
                // Set up a continuous subtle "breathe" animation
                startBreathingAnimation(shape);
            }, i * 200); // Stagger the animations
        }
    }

    private void startBreathingAnimation(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f, 1f);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f, 1f);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(2000);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Initial random arrangement of drop zones
            randomizeDropZones();
        }
    }

    private void checkGameComplete() {
        // Check if all drop zones have been filled
        if (completedDropZones >= dropZones.size() && !isGameCompleted) {
            isGameCompleted = true;
            playSound(successSound);
            
            // Animate celebration
            celebrateCompletion();
            
            // Show success message
            new Handler().postDelayed(() -> {
                Toast.makeText(this, "Great job! All shapes matched perfectly!", Toast.LENGTH_LONG).show();
            }, 1000);
        }
    }
    
    private void celebrateCompletion() {
        // Animate all drop zones as celebration
        for (int i = 0; i < dropZones.size(); i++) {
            FrameLayout dropZone = dropZones.get(i);
            
            // Get the shape inside the drop zone
            if (dropZone.getChildCount() > 0) {
                View shapeView = dropZone.getChildAt(0);
                
                // Stagger the celebrations
                new Handler().postDelayed(() -> {
                    // Jump and spin animation
                    ObjectAnimator translateY = ObjectAnimator.ofFloat(shapeView, "translationY", 0, -30, 0);
                    ObjectAnimator rotation = ObjectAnimator.ofFloat(shapeView, "rotation", 0, 360);
                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(shapeView, "scaleX", 1f, 1.3f, 1f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(shapeView, "scaleY", 1f, 1.3f, 1f);
                    
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(translateY, rotation, scaleX, scaleY);
                    animatorSet.setDuration(800);
                    animatorSet.setInterpolator(new BounceInterpolator());
                    animatorSet.start();
                }, i * 200);
            }
        }
    }
    
    private void resetGame() {
        // Reset game state and re-randomize the drop zones
        completedDropZones = 0;
        isGameCompleted = false;
        
        // Clear all drop zones
        for (FrameLayout dropZone : dropZones) {
            dropZone.removeAllViews();
        }
        
        // Re-randomize with animation
        randomizeDropZones();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Release media player resources
        if (correctSound != null) {
            correctSound.release();
            correctSound = null;
        }
        
        if (incorrectSound != null) {
            incorrectSound.release();
            incorrectSound = null;
        }
        
        if (successSound != null) {
            successSound.release();
            successSound = null;
        }
        
        if (clickSound != null) {
            clickSound.release();
            clickSound = null;
        }
    }
}
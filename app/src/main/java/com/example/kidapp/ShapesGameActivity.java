package com.example.kidapp;

import android.content.ClipData;
import android.graphics.Point;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shapes_game);

        initViews();
        setupDragListeners();
        setupDropZoneListeners();
        setupButtons();

        // Initial random arrangement of drop zones
        randomizeDropZones();
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

    private void setupButtons() {
        reloadButton.setOnClickListener(v -> randomizeDropZones());

        backButton.setOnClickListener(v -> finish());
    }

    private void setupDragListeners() {
        // Set touch listeners for all shape views to enable dragging
        View.OnTouchListener touchListener = (view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
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

    private void setupDropZoneListeners() {
        // Set drag listeners for all drop zones
        View.OnDragListener dragListener = new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        // Handle the drop event
                        View draggedView = (View) event.getLocalState();

                        // Check if this is the correct drop zone for this shape
                        if (shapeToDropZoneMap.get(draggedView.getId()) == v.getId()) {
                            // Create a copy of the dragged shape and add it to the drop zone
                            ImageView newShapeView = new ImageView(ShapesGameActivity.this);
                            newShapeView.setImageDrawable(((ImageView) draggedView).getDrawable());
                            newShapeView.setLayoutParams(new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT));
                            newShapeView.setPadding(10, 10, 10, 10); // Add padding to make shape fit nicely in drop zone

                            // Clear any previous views in the drop zone and add the new one
                            ((FrameLayout) v).removeAllViews();
                            ((FrameLayout) v).addView(newShapeView);

                            // Give feedback to the user
                            Toast.makeText(ShapesGameActivity.this, "Correct!", Toast.LENGTH_SHORT).show();

                            // Check if all shapes are placed
                            checkGameComplete();
                        } else {
                            // Provide feedback for incorrect placement
                            Toast.makeText(ShapesGameActivity.this, "Try another spot", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        // Visual feedback when entering a valid drop zone
                        v.setAlpha(0.7f);
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        // Reset visual feedback when exiting
                        v.setAlpha(1.0f);
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

    private void randomizeDropZones() {
        // Clear all drop zones
        for (FrameLayout dropZone : dropZones) {
            dropZone.removeAllViews();
        }

        // Get container dimensions
        int containerWidth = dropZoneContainer.getWidth();
        int containerHeight = dropZoneContainer.getHeight();

        // Define padding and gap to avoid overlap
        int padding = 10; // padding from edges
        int gap = 30; // khoảng cách giữa các hình

        // Drop zone kích thước nhỏ hơn để tạo khoảng cách giữa các hình
        int dropZoneSize = Math.min(containerWidth, containerHeight) / 2;

        // Danh sách các vị trí khả dụng
        List<Point> availablePositions = new ArrayList<>();

        // Tạo lưới vị trí hợp lệ trong phạm vi container
        for (int x = padding; x + dropZoneSize <= containerWidth - padding; x += dropZoneSize + gap) {
            for (int y = padding; y + dropZoneSize <= containerHeight - padding; y += dropZoneSize + gap) {
                availablePositions.add(new Point(x, y));
            }
        }

        // Trộn ngẫu nhiên danh sách vị trí
        Collections.shuffle(availablePositions, random);

        // Áp dụng vị trí cho từng drop zone
        for (int i = 0; i < dropZones.size(); i++) {
            if (i >= availablePositions.size()) break; // Đảm bảo không bị lỗi nếu thiếu vị trí

            FrameLayout dropZone = dropZones.get(i);
            Point position = availablePositions.get(i);

            // Set new layout parameters
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    dropZoneSize, dropZoneSize);
            params.leftMargin = position.x;
            params.topMargin = position.y;
            dropZone.setLayoutParams(params);

            // Hiển thị drop zone
            dropZone.setVisibility(View.VISIBLE);
        }
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
        boolean allPlaced = true;

        // Check if all drop zones have at least one child view
        for (FrameLayout dropZone : dropZones) {
            if (dropZone.getChildCount() == 0) {
                allPlaced = false;
                break;
            }
        }

        if (allPlaced) {
            Toast.makeText(this, "Great job! All shapes placed correctly!", Toast.LENGTH_LONG).show();
            // You could trigger a success animation or move to next level here
        }
    }
}
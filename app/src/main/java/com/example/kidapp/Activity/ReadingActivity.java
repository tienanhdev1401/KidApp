package com.example.kidapp.Activity;


import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kidapp.R;

public class ReadingActivity extends AppCompatActivity {
    private ImageView ivBookPage;
    private TextView[] indicators;
    private int[] imageResources = {
            R.drawable.reading,
            R.drawable.reading2,
            R.drawable.reading3,
            R.drawable.reading,
            R.drawable.reading2,
            R.drawable.reading3,
            R.drawable.reading,
            R.drawable.reading2
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reading);
        ivBookPage = findViewById(R.id.iv_book_page);
        setupIndicators();
        setupBackButton();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupIndicators() {
        indicators = new TextView[]{
                findViewById(R.id.indicator_1),
                findViewById(R.id.indicator_2),
                findViewById(R.id.indicator_3),
                findViewById(R.id.indicator_4),
                findViewById(R.id.indicator_5),
                findViewById(R.id.indicator_6),
                findViewById(R.id.indicator_7),
                findViewById(R.id.indicator_8)
        };

        for (int i = 0; i < indicators.length; i++) {
            final int position = i;
            indicators[i].setOnClickListener(v -> updatePage(position));
        }

        // Set initial state
        updatePage(0);
    }

    private void updatePage(int newPosition) {
        // Update image
        ivBookPage.setImageResource(imageResources[newPosition]);

        // Update indicators
        for (int i = 0; i < indicators.length; i++) {
            if (i == newPosition) {
                indicators[i].setBackgroundResource(R.drawable.circle_indicator_selected);
                indicators[i].setTextColor(Color.WHITE);
            } else {
                indicators[i].setBackgroundResource(R.drawable.circle_indicator_unselected);
                indicators[i].setTextColor(Color.BLACK);
            }
        }
    }

    private void setupBackButton() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
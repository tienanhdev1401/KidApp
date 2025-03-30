package com.example.kidapp;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.setAlpha;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class MusicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_music);
        setupImageSlider();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void setupImageSlider() {
        // Khởi tạo danh sách ảnh
        List<Integer> sliderImages = Arrays.asList(
                R.drawable.music_background,
                R.drawable.music_background2,
                R.drawable.music_background3
        );

        // Ánh xạ ViewPager2 và Indicator
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        CircleIndicator3 indicator = findViewById(R.id.indicator);

        // Thiết lập Adapter
        ImageSliderAdapter adapter = new ImageSliderAdapter(this, sliderImages);
        viewPager.setAdapter(adapter);

        // Liên kết Indicator với ViewPager
        indicator.setViewPager(viewPager);

        // Thêm hiệu ứng chuyển trang
        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setAlpha(0.5f);
                page.setScaleY(0.9f);

                if (position < -1 || position > 1) {
                    page.setAlpha(0f);
                } else {
                    page.setAlpha(1 - Math.abs(position));
                    page.setScaleY(Math.max(0.9f, 1 - Math.abs(position)));
                }

            }


        });
    }
}
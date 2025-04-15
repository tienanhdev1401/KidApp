package com.example.kidapp.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kidapp.Adapter.AnimalPagerAdapter;
import com.example.kidapp.R;
import com.example.kidapp.models.Animal;

import java.util.ArrayList;
import java.util.List;

public class AnimalsActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private List<Animal> animalsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animals);

        // Khởi tạo danh sách con vật
        animalsList = new ArrayList<>();
        animalsList.add(new Animal("Elephant", R.drawable.elephant, R.drawable.elephant_icon));
        animalsList.add(new Animal("Lion", R.drawable.lion, R.drawable.lion_icon));
        animalsList.add(new Animal("Monkey", R.drawable.monkey, R.drawable.monkey_icon));
        animalsList.add(new Animal("Giraffe", R.drawable.giraffe, R.drawable.giraffe_icon));
        animalsList.add(new Animal("Sheep", R.drawable.sheep, R.drawable.sheep_icon));
        animalsList.add(new Animal("squirell", R.drawable.squirrel, R.drawable.squirell_icon));
        animalsList.add(new Animal("Panda", R.drawable.panda, R.drawable.panda_icon));
        animalsList.add(new Animal("Kangaroo", R.drawable.kangaroo, R.drawable.kangaroo_icon));

        // Thiết lập ViewPager
        viewPager = findViewById(R.id.view_pager);
        AnimalPagerAdapter adapter = new AnimalPagerAdapter(this, animalsList);
        viewPager.setAdapter(adapter);

        // Cập nhật indicators khi trang thay đổi
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }
        });

        // Thiết lập trang đầu tiên
        updateIndicators(0);


        // Nút quay lại
        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updateIndicators(int selectedPosition) {
        List<ImageView> indicators = new ArrayList<>();
        indicators.add(findViewById(R.id.indicator_1));
        indicators.add(findViewById(R.id.indicator_2));
        indicators.add(findViewById(R.id.indicator_3));
        indicators.add(findViewById(R.id.indicator_4));
        indicators.add(findViewById(R.id.indicator_5));
        indicators.add(findViewById(R.id.indicator_6));
        indicators.add(findViewById(R.id.indicator_7));
        indicators.add(findViewById(R.id.indicator_8));

        for (int i = 0; i < indicators.size(); i++) {
            ImageView imageView = indicators.get(i);
            final int position = i; // Lưu vị trí riêng cho mỗi listener

            // Thêm sự kiện click cho indicator
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Chuyển đến trang tương ứng khi click
                    viewPager.setCurrentItem(position, true);
                }
            });

            // Cập nhật hiệu ứng scale
            if (i == selectedPosition) {
                imageView.animate()
                        .scaleX(1.5f)
                        .scaleY(1.5f)
                        .setDuration(200)
                        .start();
            } else {
                imageView.animate()
                        .scaleX(0.5f)
                        .scaleY(0.5f)
                        .setDuration(200)
                        .start();
            }
        }
    }
}
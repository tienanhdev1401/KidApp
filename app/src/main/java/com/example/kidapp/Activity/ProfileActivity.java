package com.example.kidapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kidapp.Adapter.SkillAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.UserViewModel;
import com.example.kidapp.models.SkillItem;
import com.example.kidapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.kidapp.models.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private RecyclerView vocabularyRecyclerView;
    private FirebaseAuth mAuth;
    private RecyclerView readingRecyclerView;
    private SkillAdapter vocabularyAdapter;
    private SkillAdapter readingAdapter;
    private TextView[] dayViews = new TextView[7];
    private TextView profileTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getUserByEmail(currentUser.getEmail()).observe(this, user -> {
            if (user != null) {
                Log.d("USER_PROFILE", "User found: " + user.getUsername()
                        + " | Email: " + user.getEmail());
                // Cập nhật UI với thông tin người dùng
                profileTitle.setText(user.getUsername()); // Hoặc các view khác
            } else {
                Log.d("USER_PROFILE", "No user found with email: " + currentUser.getEmail());
            }
        });
        // Nút quay lại
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Tham chiếu đến TextView title
        profileTitle = findViewById(R.id.profile_title);

        // Khởi tạo RecyclerView cho Vocabulary Skills
        vocabularyRecyclerView = findViewById(R.id.vocabulary_recycler);
        vocabularyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<SkillItem> vocabularyList = new ArrayList<>();
        vocabularyList.add(new SkillItem("Word 1", R.drawable.word1_image));
        vocabularyList.add(new SkillItem("Word 2", R.drawable.word2_image));
        vocabularyList.add(new SkillItem("Word 3", R.drawable.word3_image));

        vocabularyAdapter = new SkillAdapter(vocabularyList);
        vocabularyRecyclerView.setAdapter(vocabularyAdapter);

        // Khởi tạo RecyclerView cho Reading Skills
        readingRecyclerView = findViewById(R.id.reading_recycler);
        readingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<SkillItem> readingList = new ArrayList<>();
        readingList.add(new SkillItem("Passage 1", R.drawable.passage1_image));
        readingList.add(new SkillItem("Passage 2", R.drawable.passage2_image));
        readingList.add(new SkillItem("Passage 3", R.drawable.passage3_image));

        readingAdapter = new SkillAdapter(readingList);
        readingRecyclerView.setAdapter(readingAdapter);

        // Cập nhật date bar
        updateDateBar();
    }

    private void updateDateBar() {
        // Lấy tham chiếu đến các TextView trong date_bar
        dayViews[0] = findViewById(R.id.day_sun);
        dayViews[1] = findViewById(R.id.day_mon);
        dayViews[2] = findViewById(R.id.day_tue);
        dayViews[3] = findViewById(R.id.day_wed);
        dayViews[4] = findViewById(R.id.day_thu);
        dayViews[5] = findViewById(R.id.day_fri);
        dayViews[6] = findViewById(R.id.day_sat);

        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1=Chủ nhật, 2=Thứ 2,...
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Đặt ngày cho tuần hiện tại
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        for (int i = 0; i < 7; i++) {
            // Lấy thứ (S, M, T,...)
            String dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
                    .toUpperCase().charAt(0) + "";

            // Lấy ngày trong tháng
            int dayNumber = calendar.get(Calendar.DAY_OF_MONTH);

            // Đặt text cho TextView
            dayViews[i].setText(dayName + "\n" + dayNumber);

            // Highlight ngày hiện tại
            if (i == (currentDayOfWeek - 1)) {
                dayViews[i].setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                dayViews[i].setTextColor(getResources().getColor(android.R.color.white));
            } else {
                dayViews[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
                dayViews[i].setTextColor(getResources().getColor(android.R.color.black));
            }

            // Tăng ngày lên 1
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
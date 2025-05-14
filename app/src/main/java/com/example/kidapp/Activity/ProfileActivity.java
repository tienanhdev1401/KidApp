package com.example.kidapp.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kidapp.Adapter.AchievementAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.UserViewModel;
import com.example.kidapp.models.Achievement;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private TextView profileTitle;
    private RecyclerView achievementRecyclerView;
    private AchievementAdapter achievementAdapter;
    private List<Achievement> allAchievements = new ArrayList<>();
    private TextView[] dayViews = new TextView[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileTitle = findViewById(R.id.profile_title);
        achievementRecyclerView = findViewById(R.id.achievement_recycler);
        achievementRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        dayViews[0] = findViewById(R.id.day_sun);
        dayViews[1] = findViewById(R.id.day_mon);
        dayViews[2] = findViewById(R.id.day_tue);
        dayViews[3] = findViewById(R.id.day_wed);
        dayViews[4] = findViewById(R.id.day_thu);
        dayViews[5] = findViewById(R.id.day_fri);
        dayViews[6] = findViewById(R.id.day_sat);
        updateDateBar();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getUserByEmail(currentUser.getEmail()).observe(this, user -> {
            if (user != null) {
                profileTitle.setText(user.getUsername());
                List<String> userAchievements = user.getAchievements() != null ? user.getAchievements() : new ArrayList<>();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("achievement").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    allAchievements.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Achievement ach = doc.toObject(Achievement.class);
                        allAchievements.add(ach);
                    }
                    achievementAdapter = new AchievementAdapter(allAchievements, userAchievements);
                    achievementRecyclerView.setAdapter(achievementAdapter);

                    // Hiển thị danh sách tên các thành tựu đã đạt
                    if (userAchievements.isEmpty()) {
                        TextView tvAchievedList = findViewById(R.id.tv_achieved_list);
                        tvAchievedList.setText("Bạn chưa đạt thành tựu nào.");
                    } else {
                        List<String> achievedNames = new ArrayList<>();
                        for (Achievement ach : allAchievements) {
                            if (userAchievements.contains(ach.getId())) {
                                achievedNames.add(ach.getName());
                            }
                        }
                    }
                });
            } else {
                Log.d("USER_PROFILE", "No user found with email: " + currentUser.getEmail());
            }
        });
    }

    private void updateDateBar() {
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
}
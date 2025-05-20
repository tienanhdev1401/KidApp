package com.example.kidapp.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.view.View;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import android.app.AlertDialog;
import android.content.Context;
import com.squareup.picasso.Picasso;
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
    private List<Achievement> allAchievements = new ArrayList<>();
    private TextView[] dayViews = new TextView[7];
    private LinearLayout llBeginnerAchievements;
    private LinearLayout llMiddleAchievements;
    private LinearLayout llMasterAchievements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_2);

        profileTitle = findViewById(R.id.profile_title);


        // Find new LinearLayouts
        llBeginnerAchievements = findViewById(R.id.ll_beginner_achievements);
        llMiddleAchievements = findViewById(R.id.ll_middle_achievements);
        llMasterAchievements = findViewById(R.id.ll_master_achievements);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageView settingButton = findViewById(R.id.setting_button);
        settingButton.setOnClickListener(v -> {
            // Chuyển sang trang chi tiết sản phẩm
            startActivity(new android.content.Intent(ProfileActivity.this, ProfileDetailActivity.class));
        });

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

                // Load avatar using Glide
                ImageView avatarImageView = findViewById(R.id.avatar);
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    Glide.with(ProfileActivity.this)
                            .load(user.getAvatarUrl())
                            .apply(RequestOptions.circleCropTransform())
                            .into(avatarImageView);
                } else {
                    // Set a default avatar if the URL is null or empty
                    avatarImageView.setImageResource(R.drawable.avt);
                }

                // Update level based on score
                TextView levelTextView = findViewById(R.id.level);
                int score = user.getScoreranking(); // Assuming User object has getScore() method
                String level;
                if (score >= 0 && score <= 10) {
                    level = "Beginner";
                } else if (score >= 11 && score <= 30) {
                    level = "Middle";
                } else if (score > 30) {
                    level = "Master";
                } else {
                    level = "Unknown"; // Handle potential negative scores or other cases
                }
                levelTextView.setText(level);

                // Update score
                TextView scoreTextView = findViewById(R.id.score);
                scoreTextView.setText(String.valueOf(score));

                List<String> userAchievements = user.getAchievements() != null ? user.getAchievements() : new ArrayList<>();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("achievement").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    allAchievements.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Achievement ach = doc.toObject(Achievement.class);
                        allAchievements.add(ach);
                    }
                    // Display achievements by category
                    displayAchievementsByCategory(allAchievements, userAchievements);
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
//            String dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
//                    .toUpperCase().charAt(0) + "";

            // Lấy ngày trong tháng
            int dayNumber = calendar.get(Calendar.DAY_OF_MONTH);

            // Đặt text cho TextView
            dayViews[i].setText(String.valueOf(dayNumber));

            // Lấy LinearLayout cha
            LinearLayout parentLayout = (LinearLayout) dayViews[i].getParent();
            // Lấy TextView chữ cái thứ (con đầu tiên của LinearLayout)
            TextView dayLetterView = (TextView) parentLayout.getChildAt(0);

            // Highlight ngày hiện tại bằng cách đổi background của LinearLayout cha
            if (i == (currentDayOfWeek - 1)) {
                parentLayout.setBackgroundResource(R.drawable.selected_day_background);
                dayViews[i].setTextColor(getResources().getColor(android.R.color.white));
                dayLetterView.setTextColor(getResources().getColor(android.R.color.white)); // Đổi màu chữ cái thứ thành trắng
            } else {
                parentLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                dayViews[i].setTextColor(getResources().getColor(android.R.color.black));
                dayLetterView.setTextColor(getResources().getColor(R.color.black)); // Đổi màu chữ cái thứ thành màu xám như trong XML
            }

            // Tăng ngày lên 1
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void displayAchievementsByCategory(List<Achievement> allAchievements, List<String> userAchievements) {
        // Xóa các view cũ trong LinearLayouts trước khi thêm mới
        llBeginnerAchievements.removeAllViews();
        llMiddleAchievements.removeAllViews();
        llMasterAchievements.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Achievement achievement : allAchievements) {
            View achievementView = inflater.inflate(R.layout.item_achievement, null);
            ImageView icon = achievementView.findViewById(R.id.ivAchievementIcon);

            // Kiểm tra xem người dùng đã đạt được thành tựu này chưa
            if (userAchievements.contains(achievement.getId())) {
                // Đã đạt được, tải ảnh avatar bằng Glide và hiển thị tên
                Glide.with(this)
                     .load(achievement.getImageUrl())
                     .into(icon);
                achievementView.setAlpha(1.0f); // Đảm bảo hiển thị rõ
            } else {
                 // Chưa đạt được, tải ảnh avatar bằng Glide và làm mờ, hiển thị tên là "????"
                 Glide.with(this)
                      .load(achievement.getImageUrl())
                      .into(icon); // Tải ảnh và áp dụng grayscale
                 achievementView.setAlpha(0.3f); // Làm mờ icon
             }

             // Add click listener to show dialog
             achievementView.setOnClickListener(v -> {
                 showAchievementDialog(this, achievement);
             });

            // Thêm View vào LinearLayout phù hợp dựa vào tên
            String achievementName = achievement.getName().toLowerCase();
            if (achievementName.contains("beginner")) {
                llBeginnerAchievements.addView(achievementView);
            } else if (achievementName.contains("middle")) {
                llMiddleAchievements.addView(achievementView);
            } else if (achievementName.contains("master")) {
                llMasterAchievements.addView(achievementView);
            }
        }
    }

    private void showAchievementDialog(Context context, Achievement achievement) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_achievement, null);
        ImageView ivIcon = dialogView.findViewById(R.id.ivDialogAchievementIcon);
        TextView tvName = dialogView.findViewById(R.id.tvDialogAchievementName);
        TextView tvDesc = dialogView.findViewById(R.id.tvDialogAchievementDesc);

        // Load icon using Picasso (as provided in the user's code)
        if (achievement.getImageUrl() != null && !achievement.getImageUrl().isEmpty()) {
            Picasso.get().load(achievement.getImageUrl())
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(ivIcon);
        } else {
            ivIcon.setImageResource(R.drawable.no_image);
        }
        tvName.setText(achievement.getName());
        tvDesc.setText(achievement.getDescription());

        new AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("Đóng", null)
            .show();
    }
}
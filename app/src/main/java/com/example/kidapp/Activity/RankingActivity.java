package com.example.kidapp.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.kidapp.Adapter.RankingAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.UserViewModel;
import com.example.kidapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
    private RankingAdapter rankingAdapter;
    private RecyclerView recyclerViewRankings;

    // UI elements for current user card
    private TextView tvYourRank;
    private ImageView imgUserTier;
    private TextView tvUserRank;
    private TextView tvUsername;
    private TextView tvUserTier;
    private TextView tvUserStars;
    private TextView tvUserWinRate;
    private ImageView imgCurrentUserTierBadge;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ranking);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        tvYourRank = findViewById(R.id.tvYourRank);
        imgUserTier = findViewById(R.id.imgUserTier);
        tvUserRank = findViewById(R.id.tvUserRank);
        tvUsername = findViewById(R.id.tvUsername);
        tvUserTier = findViewById(R.id.tvUserTier);
        tvUserStars = findViewById(R.id.tvUserStars);
        tvUserWinRate = findViewById(R.id.tvUserWinRate);
        imgCurrentUserTierBadge = findViewById(R.id.imgCurrentUserTierBadge);

        recyclerViewRankings = findViewById(R.id.recyclerRankings);
        recyclerViewRankings.setLayoutManager(new LinearLayoutManager(this));

        rankingAdapter = new RankingAdapter();
        // Set the current user's email to the adapter
        rankingAdapter.setCurrentUserEmail(currentUser.getEmail());
        recyclerViewRankings.setAdapter(rankingAdapter);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.getAllUsers().observe(this, userList -> {
            if (userList != null) {
                // Sort users by scoreRanking in descending order
                Collections.sort(userList, (u1, u2) -> Integer.compare(u2.getScoreranking(), u1.getScoreranking()));

                // Update the adapter with the sorted list
                rankingAdapter.setUsers(userList);

                // Find and display current user's rank and details
                updateCurrentUserCard(userList, currentUser.getEmail());

            } else {
                // Handle case where user list is null or empty
                Log.d("RankingActivity", "User list is null or empty");
            }
        });
    }

    private void updateCurrentUserCard(List<User> userList, String currentUserEmail) {
        User currentUserData = null;
        int rank = -1;

        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            if (user.getEmail() != null && user.getEmail().equals(currentUserEmail)) {
                currentUserData = user;
                rank = i + 1; // Rank is 1-based
                break;
            }
        }

        if (currentUserData != null) {
            // Update the current user's card views
            tvYourRank.setText("Thứ hạng của bạn"); // Keep the static text or update if needed
            tvUserRank.setText("#" + rank);
            tvUsername.setText(currentUserData.getUsername());
            // You might need to implement logic to determine and set the tier based on score
            // For now, set a placeholder or keep the one from XML if it's static
            // tvUserTier.setText("Kim Cương"); // Placeholder

            // Determine and set the user's tier based on scoreRanking
            int score = currentUserData.getScoreranking();
            String tier = "";
            if (score >= 0 && score <= 10) {
                tier = "Đồng";
            } else if (score >= 11 && score <= 20) {
                tier = "Bạc";
            } else if (score >= 21 && score <= 30) {
                tier = "Vàng";
            } else if (score >= 31 && score <= 40) {
                tier = "Lục Bảo";
            } else if (score >= 41 && score <= 50) {
                tier = "Kim Cương";
            } else if (score > 50) {
                tier = "Trên Kim Cương"; // Hoặc một cấp bậc cao hơn tùy ý
            }
             tvUserTier.setText(tier);

             // Set the text color based on tier
            int tierColor;
            switch (tier) {
                case "Đồng":
                    tierColor = ContextCompat.getColor(this, R.color.tier_bronze);
                    break;
                case "Bạc":
                    tierColor = ContextCompat.getColor(this, R.color.tier_silver); // Sử dụng màu gray cho Sắt
                    break;
                case "Vàng":
                    tierColor = ContextCompat.getColor(this, R.color.tier_gold);
                    break;
                case "Lục Bảo":
                    tierColor = ContextCompat.getColor(this, R.color.tier_emerald);
                    break;
                case "Kim Cương":
                    tierColor = ContextCompat.getColor(this, R.color.tier_diamond);
                    break;
                case "Trên Kim Cương":
                    tierColor = ContextCompat.getColor(this, R.color.tier_master);
                    break;
                default:
                    tierColor = ContextCompat.getColor(this, R.color.text_primary); // Màu mặc định
                    break;
            }
            tvUserTier.setTextColor(tierColor);

            // Set the tier badge image based on tier
            int tierBadgeDrawable = R.drawable.ic_star; // Default or placeholder badge
            switch (tier) {
                case "Đồng":
                    tierBadgeDrawable = R.drawable.bronze; // Thay thế bằng drawable thực tế
                    break;
                case "Bạc":
                    tierBadgeDrawable = R.drawable.silver; // Thay thế bằng drawable thực tế
                    break;
                case "Vàng":
                    tierBadgeDrawable = R.drawable.gold; // Thay thế bằng drawable thực tế
                    break;
                case "Lục Bảo":
                    tierBadgeDrawable = R.drawable.emerald; // Thay thế bằng drawable thực tế
                    break;
                case "Kim Cương":
                    tierBadgeDrawable = R.drawable.diamond; // Thay thế bằng drawable thực tế
                    break;
                case "Trên Kim Cương":
                    tierBadgeDrawable = R.drawable.master; // Thay thế bằng drawable thực tế
                    break;
                default:
                    tierBadgeDrawable = R.drawable.ic_star; // Ảnh mặc định nếu không khớp tier nào
                    break;
            }
            imgCurrentUserTierBadge.setImageResource(tierBadgeDrawable);

            tvUserStars.setText(String.valueOf(currentUserData.getScoreranking()));

            // Calculate and Set Win Rate for current user
            String winRateText = "";
            if (currentUserData.getTotalMatches() > 0) {
                double winRate = (double) currentUserData.getScoreranking() / currentUserData.getTotalMatches() * 100;
                DecimalFormat df = new DecimalFormat("#.##");
                winRateText = "Tỉ lệ thắng: " + df.format(winRate) + "%";
            } else {
                 winRateText = "Tỉ lệ thắng: N/A"; // Or 0% depending on requirements
            }
            tvUserWinRate.setText(winRateText);

            // Load User Avatar using Glide
            if (currentUserData.getAvatarUrl() != null && !currentUserData.getAvatarUrl().isEmpty()) {
                 Glide.with(this)
                      .load(currentUserData.getAvatarUrl())
                      .placeholder(R.drawable.animal_avatar) // Ảnh mặc định khi đang tải hoặc lỗi
                      .error(R.drawable.animal_avatar) // Ảnh mặc định khi tải lỗi
                      .into(imgUserTier);
            } else {
                // Load default animal avatar if avatarUrl is null or empty
                 Glide.with(this)
                      .load(R.drawable.animal_avatar) // Load ảnh drawable
                      .into(imgUserTier);
            }

        } else {
            // Handle case where current user data is not found in the list
            Log.d("RankingActivity", "Current user data not found in the list");
            // Maybe hide the card or show a message
        }
    }
}
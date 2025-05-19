package com.example.kidapp.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.R;
import com.example.kidapp.models.User;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
// Import Glide or Picasso here if used for image loading
// import com.bumptech.glide.Glide;
// import com.squareup.picasso.Picasso;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private List<User> userList = new ArrayList<>();
    private String currentUserEmail;

    public void setUsers(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_ranking, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        User user = userList.get(position);

        // Set Rank (1-based)
        holder.tvRank.setText(String.valueOf(position + 1));

        // Load Avatar using Glide
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
             Glide.with(holder.itemView.getContext())
                  .load(user.getAvatarUrl())
                  .placeholder(R.drawable.animal_avatar) // Ảnh mặc định khi đang tải hoặc lỗi
                  .error(R.drawable.animal_avatar) // Ảnh mặc định khi tải lỗi
                  .into(holder.imgPlayerAvatar);
        } else {
            // Load default animal avatar if avatarUrl is null or empty
             Glide.with(holder.itemView.getContext())
                  .load(R.drawable.animal_avatar) // Load ảnh drawable
                  .into(holder.imgPlayerAvatar);
        }

        // Set Username
        holder.tvPlayerUsername.setText(user.getUsername());

        // Calculate and Set Win Rate
        String winRateText = "";
        if (user.getTotalMatches() > 0) {
            double winRate = (double) user.getScoreranking() / user.getTotalMatches() * 100;
            DecimalFormat df = new DecimalFormat("#.##");
            winRateText = "Tỉ lệ thắng: " + df.format(winRate) + "%";
        } else {
             winRateText = "Tỉ lệ thắng: N/A"; // Or 0% depending on requirements
        }
        holder.tvPlayerStats.setText(winRateText);

        // Set Stars (Ranking Score)
        holder.tvPlayerStars.setText(String.valueOf(user.getScoreranking()));

        // Determine and set the player's tier based on scoreRanking
        int score = user.getScoreranking();
        String tier = "";
        int tierColor;
        int tierBadgeDrawable = R.drawable.ic_star; // Default or placeholder badge

        if (score >= 0 && score <= 10) {
            tier = "Đồng";
            tierColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.tier_bronze);
            tierBadgeDrawable = R.drawable.bronze; // Thay thế bằng drawable thực tế
        } else if (score >= 11 && score <= 20) {
            tier = "Bạc";
            tierColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.tier_silver);
            tierBadgeDrawable = R.drawable.silver; // Thay thế bằng drawable thực tế
        } else if (score >= 21 && score <= 30) {
            tier = "Vàng";
            tierColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.tier_gold);
            tierBadgeDrawable = R.drawable.gold; // Thay thế bằng drawable thực tế
        } else if (score >= 31 && score <= 40) {
            tier = "Lục Bảo";
            tierColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.tier_emerald);
            tierBadgeDrawable = R.drawable.emerald; // Thay thế bằng drawable thực tế
        } else if (score >= 41 && score <= 50) {
            tier = "Kim Cương";
            tierColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.tier_diamond);
            tierBadgeDrawable = R.drawable.diamond; // Thay thế bằng drawable thực tế
        } else if (score > 50) {
            tier = "Trên Kim Cương"; // Hoặc một cấp bậc cao hơn tùy ý
            tierColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.tier_master);
            tierBadgeDrawable = R.drawable.master; // Thay thế bằng drawable thực tế
        } else {
            tierColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary); // Màu mặc định
        }

        holder.tvPlayerTier.setText(tier);
        holder.tvPlayerTier.setTextColor(tierColor);
        holder.imgTierBadge.setImageResource(tierBadgeDrawable);

        // Highlight current user's row
        if (currentUserEmail != null && currentUserEmail.equals(user.getEmail())) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.light_blue)); // Need to define light_blue color
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        CircleImageView imgPlayerAvatar;
        ImageView imgTierBadge;
        TextView tvPlayerUsername;
        TextView tvPlayerTier;
        TextView tvPlayerStats;
        TextView tvPlayerStars;
        CardView cardView; // Add CardView reference

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            imgPlayerAvatar = itemView.findViewById(R.id.imgPlayerAvatar);
            imgTierBadge = itemView.findViewById(R.id.imgTierBadge);
            tvPlayerUsername = itemView.findViewById(R.id.tvPlayerUsername);
            tvPlayerTier = itemView.findViewById(R.id.tvPlayerTier);
            tvPlayerStats = itemView.findViewById(R.id.tvPlayerStats);
            tvPlayerStars = itemView.findViewById(R.id.tvPlayerStars);
            cardView = (CardView) itemView; // Cast itemView to CardView
        }
    }
} 
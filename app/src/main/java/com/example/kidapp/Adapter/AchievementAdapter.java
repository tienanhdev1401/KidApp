package com.example.kidapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kidapp.R;
import com.example.kidapp.models.Achievement;
import com.squareup.picasso.Picasso;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {
    private List<Achievement> achievementList;
    private List<String> userAchievements;

    public AchievementAdapter(List<Achievement> achievementList, List<String> userAchievements) {
        this.achievementList = achievementList;
        this.userAchievements = userAchievements;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievementList.get(position);
        // Load icon
        if (achievement.getImageUrl() != null && !achievement.getImageUrl().isEmpty()) {
            Picasso.get().load(achievement.getImageUrl())
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(holder.ivIcon);
        } else {
            holder.ivIcon.setImageResource(R.drawable.no_image);
        }
        boolean unlocked = userAchievements.contains(achievement.getId());
        holder.itemView.setAlpha(unlocked ? 1f : 0.4f);

        // Nhấn giữ để hiện dialog nếu đã unlock
        if (unlocked) {
            holder.itemView.setOnLongClickListener(v -> {
                showAchievementDialog(holder.itemView.getContext(), achievement);
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
    }

    private void showAchievementDialog(Context context, Achievement achievement) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_achievement, null);
        ImageView ivIcon = dialogView.findViewById(R.id.ivDialogAchievementIcon);
        TextView tvName = dialogView.findViewById(R.id.tvDialogAchievementName);
        TextView tvDesc = dialogView.findViewById(R.id.tvDialogAchievementDesc);

        // Load icon
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

    @Override
    public int getItemCount() {
        return achievementList.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivAchievementIcon);
        }
    }
}
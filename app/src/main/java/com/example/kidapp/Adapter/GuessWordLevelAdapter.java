package com.example.kidapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kidapp.R;
import com.example.kidapp.models.WordGuessLevel;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class GuessWordLevelAdapter extends RecyclerView.Adapter<GuessWordLevelAdapter.LevelViewHolder> {



    public interface OnLevelClickListener {
        void onLevelClick(WordGuessLevel level);
    }

    private List<WordGuessLevel> levels = new ArrayList<>();
    private final OnLevelClickListener listener;
    private int levelReached = 0;

    public GuessWordLevelAdapter(OnLevelClickListener listener) {
        this.listener = listener;
    }

    public void setLevels(List<WordGuessLevel> levels) {
        this.levels = levels;
        notifyDataSetChanged();
    }

    public void setLevelReached(int levelReached) {
        this.levelReached = levelReached;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.level_item, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        WordGuessLevel level = levels.get(position);

        // Hiển thị số Level và tên chủ đề
        holder.tvLevelNumber.setText("Level " + level.getId()); // Sử dụng level.getId()
        holder.tvTopicName.setText(level.getTitle()); // Giả định WordGuessLevel có phương thức getTitle() cho tên chủ đề

        // Hiển thị ProgressBar khi bắt đầu tải ảnh
        holder.imageLoadingProgressBar.setVisibility(View.VISIBLE);

        // Tải ảnh đại diện cho level (giả định model WordGuessLevel có phương thức getImageUrl())
        if (level.getImageUrl() != null && !level.getImageUrl().isEmpty()) { // Giả định WordGuessLevel có getImageUrl()
            Picasso.get().load(level.getImageUrl()).placeholder(R.drawable.placeholder_image).into(holder.ivLevelImage, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    holder.imageLoadingProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    holder.imageLoadingProgressBar.setVisibility(View.GONE);
                    // Có thể hiển thị ảnh lỗi hoặc để trống
                    android.util.Log.e("GuessWordLevelAdapter", "Error loading image for level " + level.getId(), e);
                }
            });
        } else {
            holder.ivLevelImage.setImageResource(R.drawable.placeholder_image); // Sử dụng placeholder_image
            holder.imageLoadingProgressBar.setVisibility(View.GONE);
        }

        // Logic kiểm tra level đã mở khóa hay chưa
        boolean isLocked = level.getId() > levelReached + 1; // Logic mở khóa tương tự

        // Hiển thị trạng thái khóa/mở
        if (isLocked) {
            holder.ivLockOverlay.setVisibility(View.VISIBLE); // Hiển thị icon khóa
            holder.itemView.setAlpha(0.5f); // Làm mờ item nếu bị khóa
            holder.playButton.setEnabled(false); // Vô hiệu hóa nút Play khi bị khóa
        } else {
            holder.ivLockOverlay.setVisibility(View.GONE); // Ẩn icon khóa
            holder.itemView.setAlpha(1f);
            holder.playButton.setEnabled(true); // Bật nút Play khi mở khóa
        }

        // Xử lý click vào nút Chơi Ngay chỉ khi level không bị khóa
        holder.playButton.setOnClickListener(v -> {
            if (!isLocked) {
                listener.onLevelClick(level);
            } else {
                Toast.makeText(holder.itemView.getContext(), "Màn chơi này chưa mở khóa!", Toast.LENGTH_SHORT).show();
            }
        });

        // Xóa hoặc comment bỏ listener cho toàn bộ item view nếu không cần
        // holder.itemView.setOnClickListener(null);
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLevelImage;
        TextView tvLevelNumber;
        ImageView ivLockOverlay;
        ProgressBar imageLoadingProgressBar;
        TextView tvTopicName;
        Button playButton; // Khai báo Button

        LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLevelImage = itemView.findViewById(R.id.ivLevelImage);
            tvLevelNumber = itemView.findViewById(R.id.tvLevelNumber);
            ivLockOverlay = itemView.findViewById(R.id.ivLockOverlay);
            imageLoadingProgressBar = itemView.findViewById(R.id.imageLoadingProgressBar);
            tvTopicName = itemView.findViewById(R.id.tvTopicName);
            playButton = itemView.findViewById(R.id.playButton);
        }
    }
} 
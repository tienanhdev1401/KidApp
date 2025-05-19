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
import com.example.kidapp.models.FlipCardLevel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FlipCardLevelAdapter extends RecyclerView.Adapter<FlipCardLevelAdapter.LevelViewHolder> {

    public interface OnLevelClickListener {
        void onLevelClick(FlipCardLevel level);
    }

    private List<FlipCardLevel> levels = new ArrayList<>();
    private final OnLevelClickListener listener;
    private int levelReached = 0;

    public FlipCardLevelAdapter(OnLevelClickListener listener) {
        this.listener = listener;
    }

    public void setLevels(List<FlipCardLevel> levels) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.level_item_flip_card, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        FlipCardLevel level = levels.get(position);

        // Hiển thị số Level và tên chủ đề
        holder.tvLevelNumber.setText("Level " + level.getId());
        holder.tvTopicName.setText(level.getTopic());

        // Hiển thị ProgressBar khi bắt đầu tải ảnh
        holder.imageLoadingProgressBar.setVisibility(View.VISIBLE);

        // Tải ảnh đại diện cho level (giả định model FlipCardLevel có phương thức getImageUrl())
        if (level.getImageUrl() != null && !level.getImageUrl().isEmpty()) {
            Picasso.get().load(level.getImageUrl()).placeholder(R.drawable.placeholder_image).into(holder.ivLevelImage, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    holder.imageLoadingProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    holder.imageLoadingProgressBar.setVisibility(View.GONE);
                    // Có thể hiển thị ảnh lỗi hoặc để trống
                    android.util.Log.e("FlipCardLevelAdapter", "Error loading image for level " + level.getId(), e);
                }
            });
        } else {
            holder.ivLevelImage.setImageResource(R.drawable.placeholder_image);
            holder.imageLoadingProgressBar.setVisibility(View.GONE);
        }

        // Logic kiểm tra level đã mở khóa hay chưa
        boolean isLocked = level.getId() > levelReached + 1;

        // Hiển thị trạng thái khóa/mở
        if (isLocked) {
            holder.ivLockOverlay.setVisibility(View.VISIBLE); // Hiển thị icon khóa
            holder.itemView.setAlpha(0.5f); // Làm mờ item nếu bị khóa
        } else {
            holder.ivLockOverlay.setVisibility(View.GONE); // Ẩn icon khóa
            holder.itemView.setAlpha(1f);
        }

        // Xử lý click chỉ khi level không bị khóa
        // holder.itemView.setOnClickListener(v -> {
        //     if (!isLocked) {
        //         listener.onLevelClick(level);
        //     } else {
        //         // Có thể thêm hiệu ứng hoặc thông báo khi click vào level bị khóa
        //         Toast.makeText(holder.itemView.getContext(), "Màn chơi này chưa mở khóa!", Toast.LENGTH_SHORT).show();
        //     }
        // });

        holder.playButton.setOnClickListener(v -> {
            if (!isLocked) {
                listener.onLevelClick(level);
            } else {
                Toast.makeText(holder.itemView.getContext(), "Màn chơi này chưa mở khóa!", Toast.LENGTH_SHORT).show();
            }
        });
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
        Button playButton;

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
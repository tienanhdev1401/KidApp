package com.example.kidapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kidapp.R;
import com.example.kidapp.models.WordGuessLevel;
import java.util.ArrayList;
import java.util.List;

public class GuessWordLevelAdapter extends RecyclerView.Adapter<GuessWordLevelAdapter.LevelViewHolder> {



    public interface OnLevelClickListener {
        void onLevelClick(WordGuessLevel level);
    }

    private List<WordGuessLevel> levels = new ArrayList<>();
    private final OnLevelClickListener listener;

    public GuessWordLevelAdapter(OnLevelClickListener listener) {
        this.listener = listener;
    }

    public void setLevels(List<WordGuessLevel> levels) {
        this.levels = levels;
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
        holder.tvLevelNumber.setText(String.valueOf(level.getId()));

        boolean isLocked=false;

        // Hiển thị số sao
        int stars = 3; // Đảm bảo WordGuessLevel có hàm getStars()
        holder.star1.setImageResource(stars >= 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        holder.star2.setImageResource(stars >= 2 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        holder.star3.setImageResource(stars == 3 ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);

        // Hiển thị khóa/mở
        if (isLocked) { // Đảm bảo WordGuessLevel có hàm isLocked()
            holder.ivLock.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.ivLock.setVisibility(View.GONE);
            holder.itemView.setAlpha(1f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!isLocked) {
                listener.onLevelClick(level);
            }
        });
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        TextView tvLevelNumber;
        ImageView star1, star2, star3, ivLock;
        LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLevelNumber = itemView.findViewById(R.id.tvLevelNumber);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            ivLock = itemView.findViewById(R.id.ivLock);
        }
    }
} 
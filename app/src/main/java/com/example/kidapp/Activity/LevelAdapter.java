package com.example.kidapp.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kidapp.R;
import com.example.kidapp.models.WordGuessLevel;
import java.util.ArrayList;
import java.util.List;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {

    public interface OnLevelClickListener {
        void onLevelClick(WordGuessLevel level);
    }

    private List<WordGuessLevel> levels = new ArrayList<>();
    private final OnLevelClickListener listener;

    public LevelAdapter(OnLevelClickListener listener) {
        this.listener = listener;
    }

    public void setLevels(List<WordGuessLevel> levels) {
        this.levels = levels;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        WordGuessLevel level = levels.get(position);
        holder.tvLevelName.setText(level.getName());
        holder.itemView.setOnClickListener(v -> listener.onLevelClick(level));
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        TextView tvLevelName;
        LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLevelName = itemView.findViewById(R.id.tvLevelName);
        }
    }
} 
package com.example.kidapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.R;
import com.example.kidapp.models.FlipCardLevel;

import java.util.ArrayList;
import java.util.List;

public class FlipCardLevelAdapter extends RecyclerView.Adapter<FlipCardLevelAdapter.LevelViewHolder> {

    public interface OnLevelClickListener {
        void onLevelClick(FlipCardLevel level);
    }

    private List<FlipCardLevel> levelList=new ArrayList<>();
    private final OnLevelClickListener listener;

    public FlipCardLevelAdapter(OnLevelClickListener listener) {
        this.listener = listener;
    }

    public void setLevelList(List<FlipCardLevel> levelList) {
        this.levelList = levelList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flipcard_level, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        FlipCardLevel level = levelList.get(position);
        holder.tvLevel.setText(String.valueOf(level.getId()));
        holder.tvTopic.setText(level.getTopic());
        holder.itemView.setOnClickListener(v -> listener.onLevelClick(level));
    }

    @Override
    public int getItemCount() {
        return levelList != null ? levelList.size() : 0;
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        TextView tvLevel, tvTopic;
        ImageView ivLevelStar;

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvTopic = itemView.findViewById(R.id.tvTopic);
        }
    }
} 
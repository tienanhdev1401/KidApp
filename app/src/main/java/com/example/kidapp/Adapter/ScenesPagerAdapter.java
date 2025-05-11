package com.example.kidapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.models.StoryModel;

import java.util.ArrayList;

public class ScenesPagerAdapter extends RecyclerView.Adapter<ScenesPagerAdapter.SceneViewHolder> {

    private Context context;
    private ArrayList<StoryModel.SceneModel> scenes;

    public ScenesPagerAdapter(Context context, ArrayList<StoryModel.SceneModel> scenes) {
        this.context = context;
        this.scenes = scenes;
    }

    @NonNull
    @Override
    public SceneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story_scene, parent, false);
        return new SceneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SceneViewHolder holder, int position) {
        StoryModel.SceneModel scene = scenes.get(position);
        
        // Hiển thị nội dung cảnh
        holder.sceneContent.setText(scene.getVietnameseContent());
        
        // Hiển thị ảnh minh họa
        Glide.with(context)
                .load(scene.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background) // Hình ảnh mặc định khi đang tải
                .error(R.drawable.ic_launcher_background) // Hình ảnh hiển thị khi lỗi
                .into(holder.sceneImage);
    }

    @Override
    public int getItemCount() {
        return scenes != null ? scenes.size() : 0;
    }

    public static class SceneViewHolder extends RecyclerView.ViewHolder {
        ImageView sceneImage;
        TextView sceneContent;

        public SceneViewHolder(@NonNull View itemView) {
            super(itemView);
            sceneImage = itemView.findViewById(R.id.sceneImage);
            sceneContent = itemView.findViewById(R.id.sceneContent);
        }
    }
} 
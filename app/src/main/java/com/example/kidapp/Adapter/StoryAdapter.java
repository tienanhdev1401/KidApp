package com.example.kidapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.MusicViewModel;
import com.example.kidapp.ViewModel.StoryViewModel;
import com.example.kidapp.models.Music;
import com.example.kidapp.models.Story;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {
    private final Context context;
    private List<Story> storyList;
    private StoryAdapter.OnFavoriteClickListener favoriteClickListener;
    private StoryAdapter.OnItemClickListener itemClickListener;

    private StoryViewModel storyViewModel;

    private final LifecycleOwner lifecycleOwner;

    public StoryAdapter(Context context, List<Story> storyList,LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.storyList = storyList != null ? storyList : new ArrayList<>();
        this.storyViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(StoryViewModel.class);
        this.lifecycleOwner = lifecycleOwner;
    }
    public interface OnFavoriteClickListener {
        void onFavoriteClick(int position, boolean isFavorite);
    }

    public void setOnFavoriteClickListener(StoryAdapter.OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Story product);
    }

    public void setOnItemClickListener(StoryAdapter.OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public StoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryAdapter.ViewHolder holder, int position) {
        Story story = storyList.get(position);

        if(!story.getStoryImgUrl().isEmpty()) {
            String firstImage = story.getStoryImgUrl();
            Glide.with(context)
                    .load(firstImage)
                    .into(holder.storyIcon);
        }

        holder.tvStoryName.setText(story.getStoryTitle());

        if(story.getStoryVideoUrl() != null && !story.getStoryVideoUrl().isEmpty()) {
            String duration = null;
            try {
                duration = getVideoDuration(story.getStoryVideoUrl());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            holder.tvInfoName.setText(duration);
        } else {
            holder.tvInfoName.setText("00:00");
        }


        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position, storyList.get(position));
            }
        });

    }

    private String getVideoDuration(String videoUrl) throws IOException {
        android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoUrl, new java.util.HashMap<>()); // Nếu video là URL
            String time = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillisec = Long.parseLong(time);

            long durationInSeconds = timeInMillisec / 1000;
            long hours = durationInSeconds / 3600;
            long minutes = (durationInSeconds % 3600) / 60;
            long seconds = durationInSeconds % 60;

            if (hours > 0) {
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            } else {
                return String.format("%02d:%02d", minutes, seconds);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "00:00";
        } finally {
            retriever.release();
        }
    }



    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView storyIcon, storyFavorite;
        TextView tvStoryName, tvInfoName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storyIcon = itemView.findViewById(R.id.storyThumbnailImg);
            storyFavorite = itemView.findViewById(R.id.favoriteBtn);
            tvStoryName = itemView.findViewById(R.id.storyTitleTv);
            tvInfoName = itemView.findViewById(R.id.storyInfoTv);

        }
    }
}

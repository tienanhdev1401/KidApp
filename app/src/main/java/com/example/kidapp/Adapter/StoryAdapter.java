package com.example.kidapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.FavoriteViewModel;
import com.example.kidapp.ViewModel.StoryViewModel;
import com.example.kidapp.models.Story;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {
    private final Context context;
    private List<Story> storyList;
    private StoryAdapter.OnFavoriteClickListener favoriteClickListener;
    private StoryAdapter.OnItemClickListener itemClickListener;

    private StoryViewModel storyViewModel;
    private FavoriteViewModel favoriteViewModel;

    private final LifecycleOwner lifecycleOwner;
    private String userEmail;

    public StoryAdapter(Context context, List<Story> storyList, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.storyList = storyList != null ? storyList : new ArrayList<>();
        this.lifecycleOwner = lifecycleOwner;
        
        if (context instanceof ViewModelStoreOwner) {
            this.storyViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(StoryViewModel.class);
            this.favoriteViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(FavoriteViewModel.class);
        }
        
        // Get current user email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        }
    }
    
    // Constructor for favorite functionality without LifecycleOwner
    public StoryAdapter(Context context, List<Story> storyList) {
        this.context = context;
        this.storyList = storyList != null ? storyList : new ArrayList<>();
        this.lifecycleOwner = null;
        
        if (context instanceof ViewModelStoreOwner && context instanceof LifecycleOwner) {
            this.favoriteViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(FavoriteViewModel.class);
        }
        
        // Get current user email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        }
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
    
    // Method to update data in adapter
    public void updateData(List<Story> newStoryList) {
        this.storyList = newStoryList;
        notifyDataSetChanged();
    }
    
    // Method to refresh favorite status for all items
    public void refreshFavoriteStatus() {
        if (favoriteViewModel != null && lifecycleOwner != null && userEmail != null) {
            notifyDataSetChanged();
        }
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

        if(story.getStoryImgUrl() != null && !story.getStoryImgUrl().isEmpty()) {
            String firstImage = story.getStoryImgUrl();
            Glide.with(context)
                    .load(firstImage)
                    .centerCrop()
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
            holder.tvInfoName.setText("5 min • Story");
        }

        holder.itemView.setOnClickListener(v -> {

                // Nếu có itemClickListener được thiết lập thì sử dụng nó
                itemClickListener.onItemClick(position, storyList.get(position));

        });
        
        // Xử lý favorite button
        if (favoriteViewModel != null && lifecycleOwner != null && userEmail != null) {
            MaterialButton favoriteButton = holder.storyFavorite;
            checkAndUpdateFavoriteStatus(favoriteButton, story);

            favoriteButton.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    Story current = storyList.get(currentPosition);
                    
                    // Đảo trạng thái yêu thích và cập nhật UI ngay lập tức
                    favoriteViewModel.isStoryFavorite(userEmail, current.getStoryId())
                        .observe(lifecycleOwner, new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean isFavorite) {
                                if (isFavorite != null) {
                                    // Chỉ cần quan sát một lần
                                    favoriteViewModel.isStoryFavorite(userEmail, current.getStoryId())
                                        .removeObserver(this);
                                    
                                    // Đảo trạng thái
                                    boolean newState = !isFavorite;
                                    
                                    // Cập nhật UI trước
                                    updateFavoriteButtonState(favoriteButton, newState);
                                    
                                    // Sau đó cập nhật trong database
                                    favoriteViewModel.toggleStoryFavorite(userEmail, current.getStoryId());
                                    
                                    // Thông báo cho listener
                                    if (favoriteClickListener != null) {
                                        favoriteClickListener.onFavoriteClick(currentPosition, newState);
                                    }
                                    
                                    // Hiển thị thông báo
                                    String message = newState ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích";
                                    Snackbar.make(v, message, Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
                }
            });
        } else {
            // Nếu không có thông tin favorite, hiển thị trạng thái mặc định
            holder.storyFavorite.setIconResource(R.drawable.non_love);
        }
    }
    
    private void checkAndUpdateFavoriteStatus(MaterialButton favoriteButton, Story story) {
        if (favoriteViewModel == null || lifecycleOwner == null || userEmail == null || story.getStoryId() == null) {
            favoriteButton.setIconResource(R.drawable.non_love);
            return;
        }

        favoriteViewModel.isStoryFavorite(userEmail, story.getStoryId()).observe(lifecycleOwner, isFavorite -> {
            updateFavoriteButtonState(favoriteButton, isFavorite != null && isFavorite);
        });
    }
    
    private void updateFavoriteButtonState(MaterialButton favoriteButton, boolean isFavorite) {
        if (isFavorite) {
            favoriteButton.setIconResource(R.drawable.love);
            favoriteButton.setIconTint(context.getResources().getColorStateList(R.color.favorite));
        } else {
            favoriteButton.setIconResource(R.drawable.non_love);
            favoriteButton.setIconTint(context.getResources().getColorStateList(R.color.non_favorite));
        }
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
        ImageView storyIcon;
        MaterialButton storyFavorite;
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

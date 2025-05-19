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
import com.example.kidapp.Activity.StoryDetailActivity;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.FavoriteViewModel;
import com.example.kidapp.models.Story;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class FeaturedStoryAdapter extends RecyclerView.Adapter<FeaturedStoryAdapter.ViewHolder> {
    private Context context;
    private List<Story> storyList;
    private FavoriteViewModel favoriteViewModel;
    private String userEmail;
    private StoryAdapter.OnFavoriteClickListener favoriteClickListener;
    private StoryAdapter.OnItemClickListener itemClickListener;
    private final LifecycleOwner lifecycleOwner;

    public FeaturedStoryAdapter(Context context, List<Story> storyList, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.storyList = storyList;
        this.lifecycleOwner = lifecycleOwner;

        // Get current user email
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }
        
        // Initialize ViewModel
        favoriteViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(FavoriteViewModel.class);
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_featured_story, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Story story = storyList.get(position);
        
        // Set story title
        holder.storyTitle.setText(story.getStoryTitle());
        
        // Set duration (placeholder for now)
        holder.storyDuration.setText("5 min");
        
        // Load story thumbnail
        if (story.getStoryImgUrl() != null && !story.getStoryImgUrl().isEmpty()) {
            Glide.with(context)
                    .load(story.getStoryImgUrl())
                    .placeholder(R.drawable.story_background)
                    .error(R.drawable.story_tortoise_hare)
                    .centerCrop()
                    .into(holder.storyThumbnail);
        }
        
        // Xử lý favorite button - kiểm tra trạng thái ban đầu
        if (favoriteViewModel != null && lifecycleOwner != null && userEmail != null) {
            checkAndUpdateFavoriteStatus(holder.favoriteButton, story);
            
            holder.favoriteButton.setOnClickListener(v -> {
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
                                        updateFavoriteButtonState(holder.favoriteButton, newState);
                                        
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
            holder.favoriteButton.setImageResource(R.drawable.non_love);
        }
        
        // Set thumbnail click listener for opening story detail

        holder.itemView.setOnClickListener(v -> {

            // Nếu có itemClickListener được thiết lập thì sử dụng nó
            itemClickListener.onItemClick(position, storyList.get(position));

        });

        // Set click listener on the whole item
        holder.itemView.setOnClickListener(v -> {

            // Nếu có itemClickListener được thiết lập thì sử dụng nó
            itemClickListener.onItemClick(position, storyList.get(position));

        });
        holder.storyThumbnail.setOnClickListener(v -> {

            // Nếu có itemClickListener được thiết lập thì sử dụng nó
            itemClickListener.onItemClick(position, storyList.get(position));

        });
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }
    
    private void checkAndUpdateFavoriteStatus(ImageView favoriteButton, Story story) {
        if (favoriteViewModel == null || lifecycleOwner == null || userEmail == null || story.getStoryId() == null) {
            favoriteButton.setImageResource(R.drawable.non_love);
            return;
        }

        favoriteViewModel.isStoryFavorite(userEmail, story.getStoryId()).observe(lifecycleOwner, isFavorite -> {
            updateFavoriteButtonState(favoriteButton, isFavorite != null && isFavorite);
        });
    }

    private void updateFavoriteButtonState(ImageView favoriteButton, boolean isFavorite) {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.love);
            favoriteButton.setColorFilter(context.getResources().getColor(R.color.favorite));
        } else {
            favoriteButton.setImageResource(R.drawable.non_love);
            favoriteButton.setColorFilter(context.getResources().getColor(R.color.non_favorite));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView storyThumbnail, favoriteButton;
        TextView storyTitle, storyDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storyThumbnail = itemView.findViewById(R.id.featuredStoryThumbnail);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            storyTitle = itemView.findViewById(R.id.featuredStoryTitle);
            storyDuration = itemView.findViewById(R.id.featuredStoryDuration);
        }
    }
}
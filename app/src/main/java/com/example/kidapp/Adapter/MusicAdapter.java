package com.example.kidapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
import com.example.kidapp.ViewModel.MusicViewModel;
import com.example.kidapp.models.Music;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private final Context context;
    private List<Music> musicList;
    private OnFavoriteClickListener favoriteClickListener;
    private OnItemClickListener itemClickListener;

    private MusicViewModel musicViewModel;

    private final LifecycleOwner lifecycleOwner;

    private String userEmail;
    private FavoriteViewModel favoriteViewModel;

    public MusicAdapter(Context context, List<Music> musicList, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.musicList = musicList != null ? musicList : new ArrayList<>();
        this.lifecycleOwner = lifecycleOwner;
        
        if (context instanceof ViewModelStoreOwner) {
            this.musicViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(MusicViewModel.class);
            this.favoriteViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(FavoriteViewModel.class);
        }
        
        // Get current user email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        }
    }
    
    // Constructor for favorite functionality without LifecycleOwner
    public MusicAdapter(Context context, List<Music> musicList) {
        this.context = context;
        this.musicList = musicList != null ? musicList : new ArrayList<>();
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

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Music product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }
    
    // Method to update data in adapter
    public void updateData(List<Music> newMusicList) {
        this.musicList = newMusicList;
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
    public MusicAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.ViewHolder holder, int position) {
        Music music = musicList.get(position);

        if (music.getMusicIconUrl() != null && !music.getMusicIconUrl().isEmpty()) {
            String firstImage = music.getMusicIconUrl();

            // Sử dụng Glide
            Glide.with(context)
                    .load(firstImage) // Có thể là URI hoặc đường dẫn file
                    .into(holder.musicIcon);
        }

        holder.tvSongName.setText(music.getMusicName());
        holder.tvArtistName.setText(music.getAuthor());

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position, musicList.get(position));
            }
        });

        // Chỉ xử lý favorite nếu có đủ thông tin
        if (favoriteViewModel != null && lifecycleOwner != null && userEmail != null) {
            MaterialButton favoriteButton = holder.musicFavorite;
            checkAndUpdateFavoriteStatus(favoriteButton, music);

            favoriteButton.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    Music current = musicList.get(currentPosition);
                    
                    // Đảo trạng thái yêu thích và cập nhật UI ngay lập tức
                    favoriteViewModel.isMusicFavorite(userEmail, current.getMusicId())
                        .observe(lifecycleOwner, new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean isFavorite) {
                                if (isFavorite != null) {
                                    // Chỉ cần quan sát một lần
                                    favoriteViewModel.isMusicFavorite(userEmail, current.getMusicId())
                                        .removeObserver(this);
                                    
                                    // Đảo trạng thái
                                    boolean newState = !isFavorite;
                                    
                                    // Cập nhật UI trước
                                    updateFavoriteButtonState(favoriteButton, newState);
                                    
                                    // Sau đó cập nhật trong database
                                    favoriteViewModel.toggleMusicFavorite(userEmail, current.getMusicId());
                                    
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
            holder.musicFavorite.setIconResource(R.drawable.non_love);
        }
    }

    private void checkAndUpdateFavoriteStatus(MaterialButton favoriteButton, Music music) {
        if (favoriteViewModel == null || lifecycleOwner == null || userEmail == null || music.getMusicId() == null) {
            favoriteButton.setIconResource(R.drawable.non_love);
            return;
        }

        // Sử dụng observeForever và sau đó removeObserver để tránh lưu trữ nhiều observer
        Observer<Boolean> favoriteObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isFavorite) {
                updateFavoriteButtonState(favoriteButton, isFavorite != null && isFavorite);
                // Sau khi cập nhật UI, loại bỏ observer để tránh rò rỉ bộ nhớ
                favoriteViewModel.isMusicFavorite(userEmail, music.getMusicId()).removeObserver(this);
            }
        };
        
        favoriteViewModel.isMusicFavorite(userEmail, music.getMusicId()).observeForever(favoriteObserver);
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

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView musicIcon;
        MaterialButton musicFavorite;
        TextView tvSongName, tvArtistName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            musicIcon = itemView.findViewById(R.id.musicIcon);
            musicFavorite = itemView.findViewById(R.id.favoriteBtn);
            tvSongName = itemView.findViewById(R.id.tv_songName);
            tvArtistName = itemView.findViewById(R.id.tv_artistName);
        }
    }
}

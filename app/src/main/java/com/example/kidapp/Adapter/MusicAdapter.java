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
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.MusicViewModel;
import com.example.kidapp.models.Music;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private final Context context;
    private List<Music> musicList;
    private OnFavoriteClickListener favoriteClickListener;
    private OnItemClickListener itemClickListener;

    private MusicViewModel musicViewModel;

    private final LifecycleOwner lifecycleOwner;

    public MusicAdapter(Context context, List<Music> musicList,LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.musicList = musicList != null ? musicList : new ArrayList<>();
        this.musicViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(MusicViewModel.class);
        this.lifecycleOwner = lifecycleOwner;

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

    @NonNull
    @Override
    public MusicAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.ViewHolder holder, int position) {
        Music music = musicList.get(position);

        Log.d("Music icon: ", music.getMusicIconUrl());
        if (!music.getMusicIconUrl().isEmpty()) {
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
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView musicIcon, musicFavorite;
        TextView tvSongName, tvArtistName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            musicIcon = itemView.findViewById(R.id.musicIcon);
            musicFavorite = itemView.findViewById(R.id.favoriteButton);
            tvSongName = itemView.findViewById(R.id.tv_songName);
            tvArtistName = itemView.findViewById(R.id.tv_artistName);

        }
    }
}

package com.example.kidapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.kidapp.Activity.GameXepHinhActivity;
import com.example.kidapp.R;
import com.example.kidapp.models.Puzzle;

import java.util.List;

public class PuzzleAdapter extends RecyclerView.Adapter<PuzzleAdapter.PuzzleViewHolder> {

    private Context context;
    private List<Puzzle> puzzleList;
    private int lastPosition = -1;

    public PuzzleAdapter(Context context, List<Puzzle> puzzleList) {
        this.context = context;
        this.puzzleList = puzzleList;
    }

    @NonNull
    @Override
    public PuzzleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_puzzle, parent, false);
        return new PuzzleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PuzzleViewHolder holder, int position) {
        Puzzle puzzle = puzzleList.get(position);
        
        // Thêm hiệu ứng cho item
        setAnimation(holder.itemView, position);
        
        // Hiển thị tên puzzle
        holder.puzzleNameTextView.setText(puzzle.getName() != null ? puzzle.getName() : "Puzzle " + (position + 1));
        
        // Hiển thị hình ảnh puzzle
        if (puzzle.getURLimage() != null && !puzzle.getURLimage().isEmpty()) {
            holder.imageLoadingProgressBar.setVisibility(View.VISIBLE);
            
            Glide.with(holder.itemView.getContext())
                .load(puzzle.getURLimage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.imageLoadingProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.imageLoadingProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.puzzleImageView);
        } else {
            // Nếu không có URL, hiển thị hình mặc định
            holder.puzzleImageView.setImageResource(R.drawable.puzzle_image);
            holder.imageLoadingProgressBar.setVisibility(View.GONE);
        }
        
        // Xử lý sự kiện khi nhấn nút chơi
        holder.playButton.setOnClickListener(v -> {
            // Thêm hiệu ứng khi nhấn nút
            holder.playButton.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                holder.playButton.animate().scaleX(1f).scaleY(1f).setDuration(100);
                
                Intent intent = new Intent(context, GameXepHinhActivity.class);
                intent.putExtra("PUZZLE_ID", puzzle.getId());
                intent.putExtra("PUZZLE_URL", puzzle.getURLimage());
                intent.putExtra("PUZZLE_NAME", puzzle.getName());
                context.startActivity(intent);
            }).start();
        });
        
        // Xử lý sự kiện khi nhấn vào item
        holder.itemView.setOnClickListener(v -> {
            holder.playButton.performClick();
        });
    }

    @Override
    public int getItemCount() {
        return puzzleList != null ? puzzleList.size() : 0;
    }
    
    public void updatePuzzleList(List<Puzzle> newPuzzleList) {
        this.puzzleList = newPuzzleList;
        lastPosition = -1; // Reset animation
        notifyDataSetChanged();
    }
    
    // Thêm hiệu ứng cho item
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            viewToAnimate.setAlpha(0f);
            viewToAnimate.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(300)
                .setStartDelay(position * 100)
                .start();
            lastPosition = position;
        }
    }

    public static class PuzzleViewHolder extends RecyclerView.ViewHolder {
        ImageView puzzleImageView;
        TextView puzzleNameTextView;
        Button playButton;
        ProgressBar imageLoadingProgressBar;
        CardView cardView;

        public PuzzleViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            puzzleImageView = itemView.findViewById(R.id.puzzleImageView);
            puzzleNameTextView = itemView.findViewById(R.id.puzzleNameTextView);
            playButton = itemView.findViewById(R.id.playButton);
            imageLoadingProgressBar = itemView.findViewById(R.id.imageLoadingProgressBar);
        }
    }
} 
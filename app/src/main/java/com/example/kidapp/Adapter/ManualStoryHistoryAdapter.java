package com.example.kidapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.models.ManualStory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManualStoryHistoryAdapter extends RecyclerView.Adapter<ManualStoryHistoryAdapter.StoryViewHolder> {
    private List<ManualStory> stories;
    private final Context context;
    private final OnStoryClickListener listener;
    private final OnDeleteClickListener deleteListener;

    public ManualStoryHistoryAdapter(Context context, OnStoryClickListener listener, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.stories = new ArrayList<>();
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story_ai, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        ManualStory story = stories.get(position);
        holder.bind(story);
    }

    @Override
    public int getItemCount() {
        return stories != null ? stories.size() : 0;
    }

    public void updateStories(List<ManualStory> stories) {
        this.stories = stories;
        notifyDataSetChanged();
    }

    // Phương thức để xóa một truyện khỏi danh sách
    public void removeStory(String storyId) {
        if (stories != null && !stories.isEmpty()) {
            for (int i = 0; i < stories.size(); i++) {
                if (stories.get(i).getId().equals(storyId)) {
                    stories.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }
    }

    // Interface cho sự kiện click vào item
    public interface OnStoryClickListener {
        void onStoryClick(ManualStory story);
    }

    // Interface cho sự kiện click vào nút xóa
    public interface OnDeleteClickListener {
        void onDeleteClick(ManualStory story);
    }

    class StoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView storyImage;
        private final TextView storyTitle;
        private final TextView storyDate;
        private final TextView storyScenes;
        private final ImageButton btnDelete;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            storyImage = itemView.findViewById(R.id.storyImage);
            storyTitle = itemView.findViewById(R.id.storyTitle);
            storyDate = itemView.findViewById(R.id.storyDate);
            storyScenes = itemView.findViewById(R.id.storyScenes);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onStoryClick(stories.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    deleteListener.onDeleteClick(stories.get(position));
                }
            });
        }

        public void bind(ManualStory story) {
            // Hiển thị tiêu đề
            storyTitle.setText(story.getTitle());
            
            // Hiển thị số trang trong truyện
            int pageCount = story.getPages() != null ? story.getPages().size() : 0;
            storyScenes.setText(pageCount + " trang");
            
            // Hiển thị ngày tạo
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateString = dateFormat.format(new Date(story.getCreatedTimestamp()));
            storyDate.setText(dateString);
            
            // Hiển thị ảnh bìa
            if (story.getCoverImageUrl() != null && !story.getCoverImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(story.getCoverImageUrl())
                        .placeholder(R.drawable.storyaihistory_book)
                        .error(R.drawable.storyaihistory_book)
                        .centerCrop()
                        .into(storyImage);
            } else {
                // Hiển thị ảnh mặc định nếu không có ảnh bìa
                storyImage.setImageResource(R.drawable.storyaihistory_book);
            }
        }
    }
} 
package com.example.kidapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.models.ManualStory;
import com.example.kidapp.models.StoryElement;

import java.util.List;

public class ManualStoryReaderAdapter extends RecyclerView.Adapter<ManualStoryReaderAdapter.PageViewHolder> {
    
    private Context context;
    private List<ManualStory.Page> pages;
    
    public ManualStoryReaderAdapter(Context context, List<ManualStory.Page> pages) {
        this.context = context;
        this.pages = pages;
    }
    
    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manual_story_reader_page, parent, false);
        return new PageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        ManualStory.Page page = pages.get(position);
        
        // Hiển thị nội dung trang
        holder.tvContent.setText(page.getContent());
        
        // Hiển thị số trang
        holder.tvPageNumber.setText(String.format("Trang %d / %d", position + 1, pages.size()));
        
        // Xóa nhân vật và vật phẩm cũ trước khi thêm mới
        holder.charactersContainer.removeAllViews();
        holder.itemsContainer.removeAllViews();
        
        // Hiển thị ảnh bối cảnh
        if (page.getImageUrl() != null && !page.getImageUrl().isEmpty()) {
            holder.pageContainer.setVisibility(View.VISIBLE);
            Glide.with(context)
                .load(page.getImageUrl())
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_placeholder)
                .into(holder.ivImage);
            
            // Thêm nhân vật vào container nếu có
            if (page.getCharacters() != null && !page.getCharacters().isEmpty()) {
                // Giới hạn số lượng nhân vật hiển thị (tối đa 3)
                int numCharsToShow = Math.min(page.getCharacters().size(), 3);
                
                for (int i = 0; i < numCharsToShow; i++) {
                    StoryElement character = page.getCharacters().get(i);
                    if (character.getImageUrl() != null && !character.getImageUrl().isEmpty()) {
                        // Tạo ImageView cho mỗi nhân vật
                        ImageView characterImage = new ImageView(context);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                240); // Chiều cao cố định cho nhân vật
                        params.setMargins(16, 0, 16, 0); // Margin giữa các nhân vật
                        characterImage.setLayoutParams(params);
                        characterImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        
                        // Load ảnh nhân vật
                        Glide.with(context)
                            .load(character.getImageUrl())
                            .into(characterImage);
                        
                        // Thêm vào container
                        holder.charactersContainer.addView(characterImage);
                    }
                }
            }
            
            // Thêm vật phẩm vào container nếu có
            if (page.getItems() != null && !page.getItems().isEmpty()) {
                // Giới hạn số lượng vật phẩm hiển thị (tối đa 2)
                int numItemsToShow = Math.min(page.getItems().size(), 2);
                
                for (int i = 0; i < numItemsToShow; i++) {
                    StoryElement item = page.getItems().get(i);
                    if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                        // Tạo ImageView cho mỗi vật phẩm
                        ImageView itemImage = new ImageView(context);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                120, 120); // Kích thước cố định cho vật phẩm
                        params.setMargins(8, 0, 8, 0); // Margin giữa các vật phẩm
                        itemImage.setLayoutParams(params);
                        itemImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        
                        // Load ảnh vật phẩm
                        Glide.with(context)
                            .load(item.getImageUrl())
                            .into(itemImage);
                        
                        // Thêm vào container
                        holder.itemsContainer.addView(itemImage);
                    }
                }
            }
        } else {
            holder.pageContainer.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return pages.size();
    }
    
    static class PageViewHolder extends RecyclerView.ViewHolder {
        View pageContainer;
        ImageView ivImage;
        LinearLayout charactersContainer;
        LinearLayout itemsContainer;
        TextView tvContent;
        TextView tvPageNumber;
        
        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            pageContainer = itemView.findViewById(R.id.pageContainer);
            ivImage = itemView.findViewById(R.id.ivImage);
            charactersContainer = itemView.findViewById(R.id.charactersContainer);
            itemsContainer = itemView.findViewById(R.id.itemsContainer);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvPageNumber = itemView.findViewById(R.id.tvPageNumber);
        }
    }
} 
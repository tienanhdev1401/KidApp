package com.example.kidapp.Adapter;

import android.content.Context;
import android.net.Uri;
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

public class ManualStoryPageAdapter extends RecyclerView.Adapter<ManualStoryPageAdapter.PageViewHolder> {

    private Context context;
    private List<ManualStory.Page> pages;
    private OnPageClickListener listener;

    public ManualStoryPageAdapter(Context context, List<ManualStory.Page> pages, OnPageClickListener listener) {
        this.context = context;
        this.pages = pages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manual_story_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        ManualStory.Page page = pages.get(position);
        
        // Thiết lập số trang
        holder.tvPageNumber.setText("Trang " + (position + 1));
        
        // Thiết lập nội dung trang
        holder.tvPageContent.setText(page.getContent());
        
        // Xóa các view cũ trong container nhân vật và vật phẩm
        holder.pageCharactersContainer.removeAllViews();
        holder.pageItemsContainer.removeAllViews();
        
        // Thiết lập ảnh bối cảnh
        if (page.getImageUrl() != null && !page.getImageUrl().isEmpty()) {
            holder.pageImageContainer.setVisibility(View.VISIBLE);
            Glide.with(context)
                .load(page.getImageUrl())
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_placeholder)
                .into(holder.ivPageImage);
            
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
                                160); // Chiều cao cố định cho nhân vật
                        params.setMargins(8, 0, 8, 0); // Margin giữa các nhân vật
                        characterImage.setLayoutParams(params);
                        characterImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        
                        // Load ảnh nhân vật
                        Glide.with(context)
                            .load(character.getImageUrl())
                            .into(characterImage);
                        
                        // Thêm vào container
                        holder.pageCharactersContainer.addView(characterImage);
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
                                80, 80); // Kích thước cố định cho vật phẩm
                        params.setMargins(4, 0, 4, 0); // Margin giữa các vật phẩm
                        itemImage.setLayoutParams(params);
                        itemImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        
                        // Load ảnh vật phẩm
                        Glide.with(context)
                            .load(item.getImageUrl())
                            .into(itemImage);
                        
                        // Thêm vào container
                        holder.pageItemsContainer.addView(itemImage);
                    }
                }
            }
        } else {
            holder.pageImageContainer.setVisibility(View.GONE);
        }
        
        // Hiển thị thông tin về các phần tử đã chọn cho trang
        StringBuilder elementsText = new StringBuilder();
        
        // Thêm thông tin về bối cảnh
        if (page.getSetting() != null) {
            elementsText.append("Bối cảnh: ").append(page.getSetting().getName());
        }
        
        // Thêm thông tin về nhân vật
        if (page.getCharacters() != null && !page.getCharacters().isEmpty()) {
            if (elementsText.length() > 0) {
                elementsText.append("\n");
            }
            elementsText.append("Nhân vật: ");
            for (int i = 0; i < page.getCharacters().size(); i++) {
                elementsText.append(page.getCharacters().get(i).getName());
                if (i < page.getCharacters().size() - 1) {
                    elementsText.append(", ");
                }
            }
        }
        
        // Thêm thông tin về vật phẩm
        if (page.getItems() != null && !page.getItems().isEmpty()) {
            if (elementsText.length() > 0) {
                elementsText.append("\n");
            }
            elementsText.append("Vật phẩm: ");
            for (int i = 0; i < page.getItems().size(); i++) {
                elementsText.append(page.getItems().get(i).getName());
                if (i < page.getItems().size() - 1) {
                    elementsText.append(", ");
                }
            }
        }
        
        // Hiển thị hoặc ẩn TextView các phần tử trang
        if (elementsText.length() > 0) {
            holder.tvPageElements.setVisibility(View.VISIBLE);
            holder.tvPageElements.setText(elementsText.toString());
        } else {
            holder.tvPageElements.setVisibility(View.GONE);
        }
        
        // Thiết lập sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPageClick(position);
            }
        });
        
        holder.ivPageImage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    public void updatePages(List<ManualStory.Page> newPages) {
        this.pages = newPages;
        notifyDataSetChanged();
    }
    
    public void updatePageImageLocally(int position, Uri imageUri) {
        if (position >= 0 && position < pages.size()) {
            PageViewHolder holder = (PageViewHolder) getRecyclerView().findViewHolderForAdapterPosition(position);
            if (holder != null) {
                Glide.with(context)
                    .load(imageUri)
                    .into(holder.ivPageImage);
            }
        }
    }
    
    private RecyclerView getRecyclerView() {
        RecyclerView rv = null;
        for (PageViewHolder holder : PageViewHolder.instances) {
            if (holder.itemView.getParent() instanceof RecyclerView) {
                rv = (RecyclerView) holder.itemView.getParent();
                break;
            }
        }
        return rv;
    }

    public static class PageViewHolder extends RecyclerView.ViewHolder {
        public static List<PageViewHolder> instances = new java.util.ArrayList<>();
        
        TextView tvPageNumber;
        TextView tvPageContent;
        ImageView ivPageImage;
        TextView tvPageElements;
        View pageImageContainer;
        LinearLayout pageCharactersContainer;
        LinearLayout pageItemsContainer;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPageNumber = itemView.findViewById(R.id.tvPageNumber);
            tvPageContent = itemView.findViewById(R.id.tvPageContent);
            ivPageImage = itemView.findViewById(R.id.ivPageImage);
            tvPageElements = itemView.findViewById(R.id.tvPageElements);
            pageImageContainer = itemView.findViewById(R.id.pageImageContainer);
            pageCharactersContainer = itemView.findViewById(R.id.pageCharactersContainer);
            pageItemsContainer = itemView.findViewById(R.id.pageItemsContainer);
            instances.add(this);
        }
    }

    public interface OnPageClickListener {
        void onPageClick(int position);
        void onImageClick(int position);
    }
} 
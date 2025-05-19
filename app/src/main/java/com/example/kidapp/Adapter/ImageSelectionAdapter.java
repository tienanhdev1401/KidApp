package com.example.kidapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.R;
import com.example.kidapp.models.ImageItem;

import java.util.List;

public class ImageSelectionAdapter extends RecyclerView.Adapter<ImageSelectionAdapter.ViewHolder> {

    private final List<ImageItem> imageItems;
    private int selectedPosition = -1;
    private final OnItemSelectedListener listener;
    
    public interface OnItemSelectedListener {
        void onItemSelected(ImageItem item, int position);
    }

    public ImageSelectionAdapter(List<ImageItem> imageItems, OnItemSelectedListener listener) {
        this.imageItems = imageItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selectable_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageItem item = imageItems.get(position);
        holder.textView.setText(item.getName());
        holder.imageView.setImageResource(item.getImageResource());
        
        // Đánh dấu item được chọn
        holder.selectedOverlay.setVisibility(selectedPosition == position ? View.VISIBLE : View.GONE);
        
        holder.cardView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = position;
            
            // Cập nhật UI cho item trước đó và item mới
            if (previousSelected != -1) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);
            
            // Thông báo lựa chọn
            if (listener != null) {
                listener.onItemSelected(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageItems.size();
    }

    public String getSelectedItemValue() {
        if (selectedPosition != -1 && selectedPosition < imageItems.size()) {
            return imageItems.get(selectedPosition).getValue();
        }
        return "";
    }
    
    public void clearSelection() {
        int previousSelected = selectedPosition;
        selectedPosition = -1;
        if (previousSelected != -1) {
            notifyItemChanged(previousSelected);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        View selectedOverlay;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
            selectedOverlay = itemView.findViewById(R.id.selectedOverlay);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
} 
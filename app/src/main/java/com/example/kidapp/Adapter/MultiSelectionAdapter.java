package com.example.musicai.features.aistory.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicai.R;
import com.example.musicai.features.aistory.model.ImageItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiSelectionAdapter extends RecyclerView.Adapter<MultiSelectionAdapter.ViewHolder> {

    private final List<ImageItem> imageItems;
    private final Set<Integer> selectedPositions = new HashSet<>();
    private final OnItemSelectionChangedListener listener;
    private final int maxSelections; // Số lượng tối đa có thể chọn (0 = không giới hạn)
    
    public interface OnItemSelectionChangedListener {
        void onItemSelectionChanged(List<ImageItem> selectedItems);
    }

    public MultiSelectionAdapter(List<ImageItem> imageItems, OnItemSelectionChangedListener listener, int maxSelections) {
        this.imageItems = imageItems;
        this.listener = listener;
        this.maxSelections = maxSelections;
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
        
        // Đánh dấu item đã được chọn
        boolean isSelected = selectedPositions.contains(position);
        holder.selectedOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        
        holder.cardView.setOnClickListener(v -> {
            // Nếu đã chọn, hủy chọn
            if (isSelected) {
                selectedPositions.remove(position);
                notifyItemChanged(position);
            } 
            // Ngược lại, thêm vào danh sách đã chọn nếu chưa đạt giới hạn
            else if (maxSelections == 0 || selectedPositions.size() < maxSelections) {
                selectedPositions.add(position);
                notifyItemChanged(position);
            }
            
            // Thông báo thay đổi lựa chọn
            if (listener != null) {
                listener.onItemSelectionChanged(getSelectedItems());
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageItems.size();
    }

    public List<ImageItem> getSelectedItems() {
        List<ImageItem> items = new ArrayList<>();
        for (Integer position : selectedPositions) {
            if (position < imageItems.size()) {
                items.add(imageItems.get(position));
            }
        }
        return items;
    }
    
    public List<String> getSelectedItemValues() {
        List<String> values = new ArrayList<>();
        for (Integer position : selectedPositions) {
            if (position < imageItems.size()) {
                values.add(imageItems.get(position).getValue());
            }
        }
        return values;
    }
    
    public void clearSelections() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }
    
    // Thêm một item vào danh sách đã chọn (nếu có thể)
    public boolean selectItem(int position) {
        if (position >= 0 && position < imageItems.size() &&
            (maxSelections == 0 || selectedPositions.size() < maxSelections)) {
            selectedPositions.add(position);
            notifyItemChanged(position);
            return true;
        }
        return false;
    }
    
    // Thêm một item vào danh sách đã chọn dựa trên giá trị
    public boolean selectItemByValue(String value) {
        for (int i = 0; i < imageItems.size(); i++) {
            if (imageItems.get(i).getValue().equals(value)) {
                return selectItem(i);
            }
        }
        return false;
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
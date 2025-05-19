package com.example.kidapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kidapp.R;
import com.example.kidapp.models.StoryElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoryElementAdapter extends RecyclerView.Adapter<StoryElementAdapter.ViewHolder> {
    private List<StoryElement> elements = new ArrayList<>();
    private final OnItemSelectedListener listener;
    private final Set<Integer> selectedPositions = new HashSet<>();
    private final int MAX_SELECTIONS;
    private final StoryElement.ElementType elementType;

    public interface OnItemSelectedListener {
        void onItemSelected(StoryElement element, boolean isSelected);
    }

    public StoryElementAdapter(OnItemSelectedListener listener, StoryElement.ElementType elementType, int maxSelections) {
        this.listener = listener;
        this.elementType = elementType;
        this.MAX_SELECTIONS = maxSelections;
    }

    public void updateData(List<StoryElement> newElements) {
        elements.clear();
        elements.addAll(newElements);
        notifyDataSetChanged();
    }

    public List<StoryElement> getSelectedElements() {
        List<StoryElement> selectedElements = new ArrayList<>();
        for (Integer position : selectedPositions) {
            if (position >= 0 && position < elements.size()) {
                selectedElements.add(elements.get(position));
            }
        }
        return selectedElements;
    }

    public void clearSelections() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_story_element, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoryElement element = elements.get(position);
        boolean isSelected = selectedPositions.contains(position);
        holder.bind(element, isSelected);
        
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            
            boolean isCurrentlySelected = selectedPositions.contains(adapterPosition);
            
            if (isCurrentlySelected) {
                // Bỏ chọn phần tử
                selectedPositions.remove(adapterPosition);
                notifyItemChanged(adapterPosition);
                listener.onItemSelected(element, false);
            } else {
                // Kiểm tra nếu đã đạt giới hạn chọn
                if (elementType == StoryElement.ElementType.SETTING) {
                    // Đối với bối cảnh, chỉ cho phép chọn 1
                    if (!selectedPositions.isEmpty()) {
                        int oldPosition = selectedPositions.iterator().next();
                        selectedPositions.clear();
                        notifyItemChanged(oldPosition);
                    }
                    selectedPositions.add(adapterPosition);
                    notifyItemChanged(adapterPosition);
                    listener.onItemSelected(element, true);
                } else if (selectedPositions.size() < MAX_SELECTIONS) {
                    // Chọn thêm phần tử nếu chưa đạt giới hạn
                    selectedPositions.add(adapterPosition);
                    notifyItemChanged(adapterPosition);
                    listener.onItemSelected(element, true);
                } else {
                    // Đã đạt giới hạn chọn
                    Toast.makeText(v.getContext(), 
                            "Bạn chỉ có thể chọn tối đa " + MAX_SELECTIONS + " " + 
                            (elementType == StoryElement.ElementType.CHARACTER ? "nhân vật" : "vật phẩm"), 
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    public void toggleSelection(StoryElement element) {
        // Tìm vị trí của phần tử trong danh sách
        int position = -1;
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).getId().equals(element.getId())) {
                position = i;
                break;
            }
        }
        
        if (position >= 0) {
            // Nếu phần tử đã được chọn, bỏ chọn nó
            if (selectedPositions.contains(position)) {
                selectedPositions.remove(position);
                notifyItemChanged(position);
                listener.onItemSelected(element, false);
            } 
            // Nếu phần tử chưa được chọn và chưa đạt giới hạn, chọn nó
            else if (selectedPositions.size() < MAX_SELECTIONS) {
                selectedPositions.add(position);
                notifyItemChanged(position);
                listener.onItemSelected(element, true);
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameText;
        private final View selectionIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.elementImage);
            nameText = itemView.findViewById(R.id.elementName);
            selectionIndicator = itemView.findViewById(R.id.selectionIndicator);
        }

        public void bind(StoryElement element, boolean isSelected) {
            nameText.setText(element.getName());
            Glide.with(itemView.getContext())
                    .load(element.getImageUrl())
                    .into(imageView);
            selectionIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }
    }
} 
package com.example.musicai.features.aistory.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicai.databinding.ItemStoryElementBinding;
import com.example.musicai.features.aistory.model.StoryElement;

import java.util.ArrayList;
import java.util.List;

public class StoryElementAdapter extends RecyclerView.Adapter<StoryElementAdapter.ViewHolder> {
    private List<StoryElement> elements = new ArrayList<>();
    private int selectedPosition = -1;
    private final OnItemSelectedListener listener;

    public interface OnItemSelectedListener {
        void onItemSelected(StoryElement element);
    }

    public StoryElementAdapter(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    public void updateData(List<StoryElement> newElements) {
        this.elements = newElements;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStoryElementBinding binding = ItemStoryElementBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoryElement element = elements.get(position);
        holder.bind(element, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemStoryElementBinding binding;

        public ViewHolder(ItemStoryElementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    int previousSelected = selectedPosition;
                    selectedPosition = position;
                    notifyItemChanged(previousSelected);
                    notifyItemChanged(selectedPosition);
                    listener.onItemSelected(elements.get(position));
                }
            });
        }

        public void bind(StoryElement element, boolean isSelected) {
            binding.elementName.setText(element.getName());
            Glide.with(binding.elementImage)
                    .load(element.getImageUrl())
                    .into(binding.elementImage);
            binding.getRoot().setSelected(isSelected);
        }
    }
} 
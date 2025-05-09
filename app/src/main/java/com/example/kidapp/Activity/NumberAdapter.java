package com.example.kidapp.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kidapp.Model.Number;
import com.example.kidapp.R;

import java.util.List;

public class NumberAdapter extends RecyclerView.Adapter<NumberAdapter.NumberViewHolder> {
    private List<Number> numbers;
    private OnNumberClickListener listener;

    public interface OnNumberClickListener {
        void onNumberClick(Number number);
    }

    public NumberAdapter(List<Number> numbers, OnNumberClickListener listener) {
        this.numbers = numbers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_number, parent, false);
        return new NumberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NumberViewHolder holder, int position) {
        Number number = numbers.get(position);
        Glide.with(holder.itemView.getContext()).load(number.getImageUrl()).into(holder.ivNumber);
        holder.itemView.setOnClickListener(v -> listener.onNumberClick(number));
    }

    @Override
    public int getItemCount() {
        return numbers.size();
    }

    public static class NumberViewHolder extends RecyclerView.ViewHolder {
        ImageView ivNumber;

        public NumberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivNumber = itemView.findViewById(R.id.ivNumber);
        }
    }
} 
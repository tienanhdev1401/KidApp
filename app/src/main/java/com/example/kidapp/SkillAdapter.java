package com.example.kidapp;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SkillAdapter extends RecyclerView.Adapter<SkillAdapter.SkillViewHolder> {

    private List<SkillItem> skillList;

    public SkillAdapter(List<SkillItem> skillList) {
        this.skillList = skillList;
    }

    @NonNull
    @Override
    public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_skill, parent, false);
        return new SkillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
        SkillItem skill = skillList.get(position);
        holder.imageView.setImageResource(skill.getImageResId());
        holder.textView.setText(skill.getName());
    }

    @Override
    public int getItemCount() {
        return skillList.size();
    }

    static class SkillViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public SkillViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            textView = itemView.findViewById(R.id.item_name);
        }
    }
}

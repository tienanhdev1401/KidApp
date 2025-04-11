package com.example.kidapp;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AnimalPagerAdapter extends RecyclerView.Adapter<AnimalPagerAdapter.AnimalViewHolder> {

    private Context context;
    private List<Animal> animals;

    public AnimalPagerAdapter(Context context, List<Animal> animals) {
        this.context = context;
        this.animals = animals;
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.animal_page_item, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        Animal animal = animals.get(position);
        holder.bind(animal);
    }

    @Override
    public int getItemCount() {
        return animals.size();
    }

    @Override
    public void onViewRecycled(@NonNull AnimalViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(context).clear(holder.animalImage);
    }

    class AnimalViewHolder extends RecyclerView.ViewHolder {
        private ImageView animalImage;
        private TextView animalName;
        private ImageButton soundButton;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            animalImage = itemView.findViewById(R.id.animal_image);
            animalName = itemView.findViewById(R.id.animal_name);
            soundButton = itemView.findViewById(R.id.sound_button);
        }

        public void bind(final Animal animal) {

            Glide.with(context)
                    .asGif()
                    .load(animal.getImageResId())
                    .override(800, 600) // Tuỳ chỉnh kích thước
                    .fitCenter()
                    .into(animalImage);
            animalName.setText(animal.getName().toUpperCase());

//            soundButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    MediaPlayer mediaPlayer = MediaPlayer.create(context, animal.getSoundResId());
//                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                        @Override
//                        public void onCompletion(MediaPlayer mp) {
//                            mp.release();
//                        }
//                    });
//                    mediaPlayer.start();
//                }
//            });
        }
    }
}
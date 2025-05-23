package com.example.kidapp.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.Activity.MusicActivity;
import com.example.kidapp.Activity.MusicDetailActivity;
import com.example.kidapp.Adapter.MusicAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.FavoriteViewModel;
import com.example.kidapp.models.Music;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class FavoriteMusicFragment extends Fragment {
    private FavoriteViewModel favoriteViewModel;
    private RecyclerView recyclerView;
    private MusicAdapter adapter;
    private View emptyView;
    private String userEmail;
    private List<Music> currentMusicList;

    public FavoriteMusicFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite_music, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get current user email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        } else {
            // Handle case when user is not logged in
            return;
        }

        recyclerView = view.findViewById(R.id.musicRecyclerView);
        emptyView = view.findViewById(R.id.emptyStateContainer);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        currentMusicList = new ArrayList<>();
        adapter = new MusicAdapter(getContext(), currentMusicList, getViewLifecycleOwner());
        
        // Thiết lập listener cho sự kiện click vào nút yêu thích
        adapter.setOnFavoriteClickListener((position, isFavorite) -> {
            if (!isFavorite && position < currentMusicList.size()) {
                // Nếu người dùng bỏ yêu thích, xóa item khỏi danh sách
                Music removedMusic = currentMusicList.remove(position);
                adapter.updateData(currentMusicList);
                
                // Kiểm tra nếu danh sách trống, hiển thị empty state
                if (currentMusicList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
        
        // Thiết lập listener cho sự kiện click vào item để mở trang chi tiết
        adapter.setOnItemClickListener((position, music) -> {
            Intent intent = new Intent(getActivity(), MusicDetailActivity.class);
            intent.putParcelableArrayListExtra("playlist", new ArrayList<>(currentMusicList));
            intent.putExtra("musicPosition", position);
            intent.putExtra("music", music);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        favoriteViewModel = new ViewModelProvider(requireActivity()).get(FavoriteViewModel.class);
        
        loadFavoriteMusic();
    }

    private void loadFavoriteMusic() {
        favoriteViewModel.getFavoriteMusic(userEmail).observe(getViewLifecycleOwner(), musicList -> {
            if (musicList != null && !musicList.isEmpty()) {
                currentMusicList = new ArrayList<>(musicList);
                adapter.updateData(currentMusicList);
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            } else {
                currentMusicList.clear();
                adapter.updateData(currentMusicList);
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }
}
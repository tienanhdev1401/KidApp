package com.example.kidapp.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kidapp.Adapter.StoryAdapter;
import com.example.kidapp.R;
import com.example.kidapp.ViewModel.FavoriteViewModel;
import com.example.kidapp.models.Story;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class FavoriteStoryFragment extends Fragment {
    private FavoriteViewModel favoriteViewModel;
    private RecyclerView recyclerView;
    private StoryAdapter adapter;
    private View emptyView;
    private String userEmail;
    private List<Story> currentStoryList;

    public FavoriteStoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite_story, container, false);
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

        recyclerView = view.findViewById(R.id.storyRecyclerView);
        emptyView = view.findViewById(R.id.emptyStateContainer);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        currentStoryList = new ArrayList<>();
        adapter = new StoryAdapter(getContext(), currentStoryList, getViewLifecycleOwner());
        
        // Thiết lập listener cho sự kiện click vào nút yêu thích
        adapter.setOnFavoriteClickListener((position, isFavorite) -> {
            if (!isFavorite && position < currentStoryList.size()) {
                // Nếu người dùng bỏ yêu thích, xóa item khỏi danh sách
                Story removedStory = currentStoryList.remove(position);
                adapter.updateData(currentStoryList);
                
                // Kiểm tra nếu danh sách trống, hiển thị empty state
                if (currentStoryList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
        
        recyclerView.setAdapter(adapter);

        favoriteViewModel = new ViewModelProvider(requireActivity()).get(FavoriteViewModel.class);
        
        loadFavoriteStories();
    }

    private void loadFavoriteStories() {
        favoriteViewModel.getFavoriteStory(userEmail).observe(getViewLifecycleOwner(), storyList -> {
            if (storyList != null && !storyList.isEmpty()) {
                currentStoryList = new ArrayList<>(storyList);
                adapter.updateData(currentStoryList);
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            } else {
                currentStoryList.clear();
                adapter.updateData(currentStoryList);
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }
}
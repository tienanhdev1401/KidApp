package com.example.kidapp.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.kidapp.Fragment.FavoriteMusicFragment;
import com.example.kidapp.Fragment.FavoriteStoryFragment;

public class FavoriteViewPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 2;
    private static final int MUSIC_PAGE = 0;
    private static final int STORY_PAGE = 1;

    public FavoriteViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case MUSIC_PAGE:
                return new FavoriteMusicFragment();
            case STORY_PAGE:
                return new FavoriteStoryFragment();
            default:
                return new FavoriteMusicFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
} 
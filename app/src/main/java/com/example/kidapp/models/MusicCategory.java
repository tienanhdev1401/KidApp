package com.example.kidapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MusicCategory implements Parcelable {
    private String categoryId;
    private String categoryName;
    private String categoryImageUrl;


    public MusicCategory() {}

    public MusicCategory(String categoryName, String categoryImageUrl) {

        this.categoryName = categoryName;
        this.categoryImageUrl = categoryImageUrl;
    }

    protected MusicCategory(Parcel in) {
        categoryId = in.readString();
        categoryName = in.readString();
        categoryImageUrl = in.readString();
    }

    public static final Creator<MusicCategory> CREATOR = new Creator<MusicCategory>() {
        @Override
        public MusicCategory createFromParcel(Parcel in) {
            return new MusicCategory(in);
        }

        @Override
        public MusicCategory[] newArray(int size) {
            return new MusicCategory[size];
        }
    };

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryImageUrl() {
        return categoryImageUrl;
    }

    public void setCategoryImageUrl(String categoryImageUrl) {
        this.categoryImageUrl = categoryImageUrl;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(categoryId);
        dest.writeString(categoryName);
        dest.writeString(categoryImageUrl);

    }
} 
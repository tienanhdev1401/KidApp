package com.example.kidapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class StoryCategory implements Parcelable {
    private String categoryId;
    private String categoryName;
    private String categoryImageUrl;

    public StoryCategory() {}

    public StoryCategory(String categoryName, String categoryImageUrl) {
        this.categoryName = categoryName;
        this.categoryImageUrl = categoryImageUrl;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryImageUrl() {
        return categoryImageUrl;
    }

    public void setCategoryImageUrl(String categoryImageUrl) {
        this.categoryImageUrl = categoryImageUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    protected StoryCategory(Parcel in) {
        categoryId = in.readString();
        categoryName = in.readString();
        categoryImageUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(categoryId);
        dest.writeString(categoryName);
        dest.writeString(categoryImageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StoryCategory> CREATOR = new Creator<StoryCategory>() {
        @Override
        public StoryCategory createFromParcel(Parcel in) {
            return new StoryCategory(in);
        }

        @Override
        public StoryCategory[] newArray(int size) {
            return new StoryCategory[size];
        }
    };
}

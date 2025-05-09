package com.example.kidapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Music implements Parcelable {

    private String musicId;
    private String musicName;
    private String musicThumbnailUrl;
    private String musicUrl;
    private String author;
    private String musicIconUrl;
    private String type;
    private String categoryId; // Reference to category by ID


    public Music() {}

    public Music( String musicName, String musicThumbnailUrl, String musicUrl, String author, String musicIconUrl, String type, String categoryId) {
        this.musicName = musicName;
        this.musicThumbnailUrl = musicThumbnailUrl;
        this.musicUrl = musicUrl;
        this.author = author;
        this.musicIconUrl = musicIconUrl;
        this.type = type;
        this.categoryId = categoryId;
    }

    protected Music(Parcel in) {
        musicId = in.readString();
        musicName = in.readString();
        musicThumbnailUrl = in.readString();
        musicUrl = in.readString();
        author = in.readString();
        musicIconUrl = in.readString();
        type = in.readString();
        categoryId = in.readString();
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicAvtUrl() {
        return musicThumbnailUrl;
    }

    public void setMusicAvtUrl(String musicAvtUrl) {
        this.musicThumbnailUrl = musicAvtUrl;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMusicIconUrl() {
        return musicIconUrl;
    }

    public void setMusicIconUrl(String musicIconUrl) {
        this.musicIconUrl = musicIconUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(musicId);
        dest.writeString(musicName);
        dest.writeString(musicThumbnailUrl);
        dest.writeString(musicUrl);
        dest.writeString(author);
        dest.writeString(musicIconUrl);
        dest.writeString(type);
        dest.writeString(categoryId);
    }

    @Override
    public String toString() {
        return "Music{" +
                "musicId='" + musicId + '\'' +
                ", musicName='" + musicName + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}

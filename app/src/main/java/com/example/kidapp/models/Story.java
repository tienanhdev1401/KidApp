package com.example.kidapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.w3c.dom.Text;

public class Story implements Parcelable {
    private String storyId;
    private String storyCategoryId;
    private String storyTitle;
    private String storyContent;
    private String storyMoral;
    private String storyImgUrl;
    private String storyVideoUrl;
    private String storyQuestion;
    private String storyAnswer;
    private String optionA;
    private String optionB;
    private String optionC;

    public Story() {
    }

    public Story(String storyCategoryId, String storyTitle, String storyContent, String storyMoral, String storyImgUrl, String storyVideoUrl, String storyQuestion, String storyAnswer, String optionA, String optionB, String optionC) {
        this.storyCategoryId = storyCategoryId;
        this.storyTitle = storyTitle;
        this.storyContent = storyContent;
        this.storyMoral = storyMoral;
        this.storyImgUrl = storyImgUrl;
        this.storyVideoUrl = storyVideoUrl;
        this.storyQuestion = storyQuestion;
        this.storyAnswer = storyAnswer;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;

    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getStoryCategoryId() {
        return storyCategoryId;
    }

    public void setStoryCategoryId(String storyCategoryId) {
        this.storyCategoryId = storyCategoryId;
    }

    public String getStoryTitle() {
        return storyTitle;
    }

    public void setStoryTitle(String storyTitle) {
        this.storyTitle = storyTitle;
    }

    public String getStoryMoral() {
        return storyMoral;
    }

    public void setStoryMoral(String storyMoral) {
        this.storyMoral = storyMoral;
    }

    public String getStoryContent() {
        return storyContent;
    }

    public void setStoryContent(String storyContent) {
        this.storyContent = storyContent;
    }

    public String getStoryImgUrl() {
        return storyImgUrl;
    }

    public void setStoryImgUrl(String storyImgUrl) {
        this.storyImgUrl = storyImgUrl;
    }

    public String getStoryVideoUrl() {
        return storyVideoUrl;
    }

    public void setStoryVideoUrl(String storyVideoUrl) {
        this.storyVideoUrl = storyVideoUrl;
    }

    public String getStoryQuestion() {
        return storyQuestion;
    }

    public void setStoryQuestion(String storyQuestion) {
        this.storyQuestion = storyQuestion;
    }

    public String getStoryAnswer() {
        return storyAnswer;
    }

    public void setStoryAnswer(String storyAnswer) {
        this.storyAnswer = storyAnswer;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    protected Story(Parcel in) {
        storyId = in.readString();
        storyCategoryId = in.readString();
        storyTitle = in.readString();
        storyContent = in.readString();
        storyMoral = in.readString();
        storyImgUrl = in.readString();
        storyVideoUrl = in.readString();
        storyQuestion = in.readString();
        storyAnswer = in.readString();
        optionA = in.readString();
        optionB = in.readString();
        optionC = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(storyId);
        dest.writeString(storyCategoryId);
        dest.writeString(storyTitle);
        dest.writeString(storyContent);
        dest.writeString(storyMoral);
        dest.writeString(storyImgUrl);
        dest.writeString(storyVideoUrl);
        dest.writeString(storyQuestion);
        dest.writeString(storyAnswer);
        dest.writeString(optionA);
        dest.writeString(optionB);
        dest.writeString(optionC);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Story> CREATOR = new Creator<Story>() {
        @Override
        public Story createFromParcel(Parcel in) {
            return new Story(in);
        }

        @Override
        public Story[] newArray(int size) {
            return new Story[size];
        }
    };
}

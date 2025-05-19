package com.example.kidapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ManualStory implements Parcelable {
    private String id;
    private String title;
    private String coverImageUrl;
    private List<Page> pages;
    private long createdTimestamp;
    private long lastModifiedTimestamp;
    
    // New fields for story elements
    private String settingId;
    private String settingName;
    private String settingImageUrl;
    private List<String> characterIds;
    private List<String> characterNames;
    private List<String> characterImageUrls;
    private List<String> itemIds;
    private List<String> itemNames;
    private List<String> itemImageUrls;

    public ManualStory() {
        this.pages = new ArrayList<>();
        this.characterIds = new ArrayList<>();
        this.characterNames = new ArrayList<>();
        this.characterImageUrls = new ArrayList<>();
        this.itemIds = new ArrayList<>();
        this.itemNames = new ArrayList<>();
        this.itemImageUrls = new ArrayList<>();
        this.createdTimestamp = System.currentTimeMillis();
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    protected ManualStory(Parcel in) {
        id = in.readString();
        title = in.readString();
        coverImageUrl = in.readString();
        pages = new ArrayList<>();
        in.readList(pages, Page.class.getClassLoader());
        createdTimestamp = in.readLong();
        lastModifiedTimestamp = in.readLong();
        
        // Read new fields
        settingId = in.readString();
        settingName = in.readString();
        settingImageUrl = in.readString();
        characterIds = new ArrayList<>();
        in.readStringList(characterIds);
        characterNames = new ArrayList<>();
        in.readStringList(characterNames);
        characterImageUrls = new ArrayList<>();
        in.readStringList(characterImageUrls);
        itemIds = new ArrayList<>();
        in.readStringList(itemIds);
        itemNames = new ArrayList<>();
        in.readStringList(itemNames);
        itemImageUrls = new ArrayList<>();
        in.readStringList(itemImageUrls);
    }

    public static final Creator<ManualStory> CREATOR = new Creator<ManualStory>() {
        @Override
        public ManualStory createFromParcel(Parcel in) {
            return new ManualStory(in);
        }

        @Override
        public ManualStory[] newArray(int size) {
            return new ManualStory[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public void addPage(Page page) {
        this.pages.add(page);
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public void removePage(int position) {
        if (position >= 0 && position < pages.size()) {
            this.pages.remove(position);
            this.lastModifiedTimestamp = System.currentTimeMillis();
        }
    }

    public void movePage(int fromPosition, int toPosition) {
        if (fromPosition >= 0 && fromPosition < pages.size() && 
            toPosition >= 0 && toPosition < pages.size()) {
            Page page = pages.remove(fromPosition);
            pages.add(toPosition, page);
            this.lastModifiedTimestamp = System.currentTimeMillis();
        }
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(long lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }
    
    // Getters and setters for new fields
    
    public String getSettingId() {
        return settingId;
    }

    public void setSettingId(String settingId) {
        this.settingId = settingId;
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public String getSettingName() {
        return settingName;
    }

    public void setSettingName(String settingName) {
        this.settingName = settingName;
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public String getSettingImageUrl() {
        return settingImageUrl;
    }

    public void setSettingImageUrl(String settingImageUrl) {
        this.settingImageUrl = settingImageUrl;
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public List<String> getCharacterIds() {
        return characterIds;
    }

    public void setCharacterIds(List<String> characterIds) {
        this.characterIds = characterIds;
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public List<String> getCharacterNames() {
        return characterNames;
    }

    public void setCharacterNames(List<String> characterNames) {
        this.characterNames = characterNames;
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public List<String> getCharacterImageUrls() {
        return characterImageUrls;
    }

    public void setCharacterImageUrls(List<String> characterImageUrls) {
        this.characterImageUrls = characterImageUrls;
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds;
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public List<String> getItemNames() {
        return itemNames;
    }

    public void setItemNames(List<String> itemNames) {
        this.itemNames = itemNames;
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    public List<String> getItemImageUrls() {
        return itemImageUrls;
    }

    public void setItemImageUrls(List<String> itemImageUrls) {
        this.itemImageUrls = itemImageUrls;
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }
    
    // Convenience methods for adding elements
    
    public void setSetting(StoryElement setting) {
        if (setting != null) {
            this.settingId = setting.getId();
            this.settingName = setting.getName();
            this.settingImageUrl = setting.getImageUrl();
            this.lastModifiedTimestamp = System.currentTimeMillis();
        }
    }
    
    public void addCharacter(StoryElement character) {
        if (character != null) {
            this.characterIds.add(character.getId());
            this.characterNames.add(character.getName());
            this.characterImageUrls.add(character.getImageUrl());
            this.lastModifiedTimestamp = System.currentTimeMillis();
        }
    }
    
    public void clearCharacters() {
        this.characterIds.clear();
        this.characterNames.clear();
        this.characterImageUrls.clear();
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }
    
    public void addItem(StoryElement item) {
        if (item != null) {
            this.itemIds.add(item.getId());
            this.itemNames.add(item.getName());
            this.itemImageUrls.add(item.getImageUrl());
            this.lastModifiedTimestamp = System.currentTimeMillis();
        }
    }
    
    public void clearItems() {
        this.itemIds.clear();
        this.itemNames.clear();
        this.itemImageUrls.clear();
        this.lastModifiedTimestamp = System.currentTimeMillis();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(coverImageUrl);
        dest.writeList(pages);
        dest.writeLong(createdTimestamp);
        dest.writeLong(lastModifiedTimestamp);
        
        // Write new fields
        dest.writeString(settingId);
        dest.writeString(settingName);
        dest.writeString(settingImageUrl);
        dest.writeStringList(characterIds);
        dest.writeStringList(characterNames);
        dest.writeStringList(characterImageUrls);
        dest.writeStringList(itemIds);
        dest.writeStringList(itemNames);
        dest.writeStringList(itemImageUrls);
    }

    public static class Page implements Parcelable {
        private String imageUrl;
        private String content;
        private String audioUrl;
        
        // Phần tử truyện cho từng trang
        private StoryElement setting;
        private List<StoryElement> characters;
        private List<StoryElement> items;

        public Page() {
            this.characters = new ArrayList<>();
            this.items = new ArrayList<>();
        }

        public Page(String imageUrl, String content) {
            this.imageUrl = imageUrl;
            this.content = content;
            this.characters = new ArrayList<>();
            this.items = new ArrayList<>();
        }

        protected Page(Parcel in) {
            imageUrl = in.readString();
            content = in.readString();
            audioUrl = in.readString();
            setting = in.readParcelable(StoryElement.class.getClassLoader());
            characters = new ArrayList<>();
            in.readList(characters, StoryElement.class.getClassLoader());
            items = new ArrayList<>();
            in.readList(items, StoryElement.class.getClassLoader());
        }

        public static final Creator<Page> CREATOR = new Creator<Page>() {
            @Override
            public Page createFromParcel(Parcel in) {
                return new Page(in);
            }

            @Override
            public Page[] newArray(int size) {
                return new Page[size];
            }
        };

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
        
        public String getAudioUrl() {
            return audioUrl;
        }
        
        public void setAudioUrl(String audioUrl) {
            this.audioUrl = audioUrl;
        }
        
        public StoryElement getSetting() {
            return setting;
        }
        
        public void setSetting(StoryElement setting) {
            this.setting = setting;
        }
        
        public List<StoryElement> getCharacters() {
            return characters;
        }
        
        public void setCharacters(List<StoryElement> characters) {
            this.characters = characters != null ? characters : new ArrayList<>();
        }
        
        public void addCharacter(StoryElement character) {
            if (this.characters == null) {
                this.characters = new ArrayList<>();
            }
            this.characters.add(character);
        }
        
        public List<StoryElement> getItems() {
            return items;
        }
        
        public void setItems(List<StoryElement> items) {
            this.items = items != null ? items : new ArrayList<>();
        }
        
        public void addItem(StoryElement item) {
            if (this.items == null) {
                this.items = new ArrayList<>();
            }
            this.items.add(item);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(imageUrl);
            dest.writeString(content);
            dest.writeString(audioUrl);
            dest.writeParcelable(setting, flags);
            dest.writeList(characters);
            dest.writeList(items);
        }
    }
} 
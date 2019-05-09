package com.erank.radiokoletsionv2.fragments.podcasts;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.Objects;

public class Podcast implements Parcelable {

    public static final Creator<Podcast> CREATOR = new Creator<Podcast>() {
        @Override
        public Podcast createFromParcel(Parcel source) {
            return new Podcast(source);
        }

        @Override
        public Podcast[] newArray(int size) {
            return new Podcast[size];
        }
    };
    private String audioUrl;
    //props:
    private String description;
    private String id;
    private Date date;

    private long likesAmount;
    private boolean isPlaying;

    public Podcast() {
    }

    public Podcast(String description, String audioUrl, Date date, String id) {
        this.description = description;
        this.audioUrl = audioUrl;
        this.date = date;
        this.id = id;
    }
    protected Podcast(Parcel in) {
        this.description = in.readString();
        this.audioUrl = in.readString();
        this.date = (Date) in.readSerializable();
        this.id = in.readString();
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getDescription() {
        return description;
    }


    public void setLikesAmount(long likesAmount) {
        this.likesAmount = likesAmount;
    }

    public Date getDate() {
        return date;
    }

    public long getLikesAmount() {
        return likesAmount;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Podcast)) return false;
        Podcast podcast = (Podcast) o;
        return Objects.equals(id, podcast.id);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.description);
        dest.writeString(this.audioUrl);
        dest.writeSerializable(this.date);
        dest.writeString(this.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, audioUrl, date, id);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean playing) {
        this.isPlaying = playing;
    }
}
package com.erank.koletsionpods.db.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.erank.koletsionpods.utils.enums.PodcastState;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class Podcast implements Parcelable {

    public static final Parcelable.Creator<Podcast> CREATOR = new Parcelable.Creator<Podcast>() {
        @Override
        public Podcast createFromParcel(Parcel source) {
            return new Podcast(source);
        }

        @Override
        public Podcast[] newArray(int size) {
            return new Podcast[size];
        }
    };
    @Exclude
    public PodcastState state;
    @Exclude
    private Set<String> likedUserIds;
    @Exclude
    private List<Comment> comments;
    private String id;
    private String description;
    private String audioUrl;
    private Date date;

    public Podcast() {
        likedUserIds = new HashSet<>();
        comments = new ArrayList<>();
        state = PodcastState.DEFAULT;
    }

    public Podcast(JSONObject podcast, String baseHttp) throws JSONException {
        this();
//                id of the podcast
        id = podcast.getString("vodId");

        String vodName = podcast.getString("vodName");

        description = vodName.substring(0, vodName.length() - 4)
                .replace("_", " ");

        long timeStamp = podcast.getLong("creationDate");
        date = new Date(timeStamp);

        String filepath = podcast.getString("filePath");
        audioUrl = baseHttp + filepath;
    }

    protected Podcast(Parcel in) {
        int tmpState = in.readInt();
        this.state = tmpState == -1 ? null : PodcastState.values()[tmpState];
        List<String> likes = new ArrayList<>();
        in.readStringList(likes);
        this.likedUserIds = new HashSet<>(likes);
        this.comments = in.createTypedArrayList(Comment.CREATOR);
        this.id = in.readString();
        this.description = in.readString();
        this.audioUrl = in.readString();
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
    }

    @Exclude
    public List<Comment> getCommentsList() {
        return comments;
    }

    public Map<String, Comment> getComments() {
        HashMap<String, Comment> map = new HashMap<>();
        for (Comment comment : comments) map.put(comment.getId(), comment);
        return map;
    }

    @Exclude
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void setComments(Map<String, Comment> comments) {
        setComments(new ArrayList<>(comments.values()));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("likes")
    public Map<String, Boolean> getLikedUserIds() {
        Map<String, Boolean> map = new HashMap<>();
        for (String userId : likedUserIds) map.put(userId, true);
        return map;
    }

    @Exclude
    public void setLikedUserIds(Set<String> likedUserIds) {
        this.likedUserIds = likedUserIds;
    }

    @PropertyName("likes")
    public void setlikedUserIds(Map<String, Boolean> likedUserIds) {
        setLikedUserIds(new HashSet<>(likedUserIds.keySet()));
    }

    @Exclude
    public Set<String> getLikedUIDs() {
        return likedUserIds;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Exclude
    public long getLikesAmount() {
        return likedUserIds.size();
    }

    @Exclude
    public void addLike(String uid) {
        likedUserIds.add(uid);
    }

    @Exclude
    public void removeLike(String uid) {
        likedUserIds.remove(uid);
    }

    @Exclude
    public void addComment(Comment comment) {
        comments.add(comment);
    }

    @Exclude
    public void removeComment(Comment comment) {
        comments.remove(comment);
    }

    @Exclude
    public void updateComment(Comment comment, int pos) {
        comments.set(pos, comment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Podcast podcast = (Podcast) o;
        return Objects.equals(likedUserIds, podcast.likedUserIds) &&
                Objects.equals(comments, podcast.comments) &&
                Objects.equals(id, podcast.id) &&
                Objects.equals(description, podcast.description) &&
                Objects.equals(audioUrl, podcast.audioUrl) &&
                Objects.equals(date, podcast.date) &&
                state == podcast.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(likedUserIds, comments, id, description, audioUrl, date, state);
    }

    @Exclude
    public boolean isReady() {
        return state == PodcastState.PREPARED
                || state == PodcastState.PLAYING
                || state == PodcastState.PAUSED;
    }

    @Exclude
    public boolean isPlaying() {
        return state == PodcastState.PLAYING;
    }

    @Exclude
    public boolean isPlaySafe() {
        return state == PodcastState.PREPARED || state == PodcastState.PAUSED;
    }

    @Exclude
    public boolean isLoading() {
        return state == PodcastState.LOADING;
    }

    @Exclude
    public boolean isPrepared() {
        return state == PodcastState.PREPARED;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.state == null ? -1 : this.state.ordinal());
        dest.writeStringList(new ArrayList<>(likedUserIds));
        dest.writeTypedList(this.comments);
        dest.writeString(this.id);
        dest.writeString(this.description);
        dest.writeString(this.audioUrl);
        dest.writeLong(this.date != null ? this.date.getTime() : -1);
    }

    public static class NameComparator implements Comparator<Podcast> {
        @Override
        public int compare(Podcast podcast1, Podcast podcast2) {
            int descCmp = podcast1.description.compareTo(podcast2.description);
            if (descCmp != 0) return descCmp;
            return podcast1.getId().compareTo(podcast2.getId());
        }
    }

    public static class LikesComperator implements Comparator<Podcast> {
        @Override
        public int compare(Podcast o1, Podcast o2) {
            return o1.likedUserIds.size() - o2.likedUserIds.size();
        }
    }
}
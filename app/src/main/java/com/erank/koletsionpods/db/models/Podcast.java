package com.erank.koletsionpods.db.models;

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


public class Podcast {

    @Exclude
    public PodcastState state;
    private String id;
    @PropertyName("vodName")
    public String description;
    @PropertyName("filePath")
    public String audioUrl;
    private String defaultStream;
    @Exclude
    private Date date;
    public long duration;
    @Exclude
    private Set<String> likedUserIds;
    @Exclude
    private List<Comment> comments;
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

    public String getDefaultStream() {
        return defaultStream;
    }

    public void setDefaultStream(String defaultStream) {
        this.defaultStream = defaultStream;
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

    public void setComments(Map<String, Comment> comments) {
        this.comments = new ArrayList<>(comments.values());
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

    @PropertyName("likes")
    public void setLikedUserIds(Map<String, Boolean> likedUserIds) {
        this.likedUserIds = new HashSet<>(likedUserIds.keySet());
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

    public Long getCreationDate(){return date.getTime();}

    public void setCreationDate(Long timeStamp) {
        this.date = new Date(timeStamp);
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

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("id", id);
        map.put("description", description);
        map.put("audioUrl", audioUrl);
        map.put("date", date);

        return map;
    }

    public static class NameComparator implements Comparator<Podcast> {
        @Override
        public int compare(Podcast podcast1, Podcast podcast2) {
            int descCmp = podcast1.description.compareTo(podcast2.description);
            if (descCmp != 0) return descCmp;
            return podcast1.getId().compareTo(podcast2.getId());
        }
    }

    public static class LikesComparator implements Comparator<Podcast> {
        @Override
        public int compare(Podcast o1, Podcast o2) {
            return o1.likedUserIds.size() - o2.likedUserIds.size();
        }
    }
}
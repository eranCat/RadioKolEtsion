package com.erank.koletsionpods.utils.db.models;

import com.google.firebase.database.Exclude;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Comment {

    private String id;
    private String uid;
    private String content;
    private String userName;
    private Date postDate;
    private boolean isEditable;

    public Comment() {
    }

    private Comment(String id, String content, String uid, String userName) {
        this.id = id;
        this.uid = uid;
        this.content = content;
        this.userName = userName;
        this.postDate = new Date();
        this.isEditable = true;
    }

    public Comment(String id, String content, User user) {
        this(id, content, user.getId(), user.getName());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    @Exclude
    public boolean isEditable() {
        return isEditable;
    }

    @Exclude
    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("uid", uid);
        result.put("content", content);
        result.put("postDate", postDate);
        result.put("userName", userName);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return isEditable == comment.isEditable &&
                Objects.equals(id, comment.id) &&
                Objects.equals(uid, comment.uid) &&
                Objects.equals(content, comment.content) &&
                Objects.equals(postDate, comment.postDate) &&
                Objects.equals(userName, comment.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uid, content, postDate, isEditable, userName);
    }

    @NotNull
    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", content='" + content + '\'' +
                ", postDate=" + postDate +
                ", isEditable=" + isEditable +
                ", userName='" + userName + '\'' +
                '}';
    }

    public static class DateComparator implements Comparator<Comment> {
        @Override
        public int compare(Comment o1, Comment o2) {
            return o2.postDate.compareTo(o1.postDate);
        }
    }
}

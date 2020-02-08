package com.erank.koletsionpods.db.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Comment implements Parcelable {
    private String id;
    private String uid;
    private String content;
    private Date postDate;
    private boolean isEditable;
    private String userEmail;

    public Comment(String id, String uid, String email, String content) {
        setId(id);
        setUid(uid);
        setUserEmail(email);
        setContent(content);
        setPostDate(new Date());
        isEditable = true;
    }

    public Comment() {
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

    @NotNull
    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", content='" + content + '\'' +
                ", postDate=" + postDate +
                ", isEditable=" + isEditable +
                ", userEmail='" + userEmail + '\'' +
                '}';
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
                Objects.equals(userEmail, comment.userEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uid, content, postDate, isEditable, userEmail);
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.uid);
        dest.writeString(this.content);
        dest.writeLong(this.postDate != null ? this.postDate.getTime() : -1);
        dest.writeByte(this.isEditable ? (byte) 1 : (byte) 0);
        dest.writeString(this.userEmail);
    }

    private Comment(Parcel in) {
        this.id = in.readString();
        this.uid = in.readString();
        this.content = in.readString();
        long tmpPostDate = in.readLong();
        this.postDate = tmpPostDate == -1 ? null : new Date(tmpPostDate);
        this.isEditable = in.readByte() != 0;
        this.userEmail = in.readString();
    }

    public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("uid", uid);
        result.put("content", content);
        result.put("postDate", postDate);
        result.put("email", userEmail);
        return result;
    }

    public static class DateComparator implements Comparator<Comment>{
        @Override
        public int compare(Comment o1, Comment o2) {
            return o2.postDate.compareTo(o1.postDate);
        }
    }
}

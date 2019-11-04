package com.recoded.taqadam.models;

import android.net.Uri;
import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;

import java.util.Date;

/**
 * Created by wisam on Dec 25 17.
 */

public class Comment extends Model implements Comparable<Comment> {

    @Expose
    private User user;
    @Expose
    private String body;
    @Expose
    private Long postId;
    @Expose
    private Date createdAt;

    public Comment(Long id) {
        this.id = id;
    }

    public Comment() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAuthor() {
        return user.getName();
    }

    public Uri getAuthorImage() {
        return user.getProfile().getAvatar();
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int compareTo(@NonNull Comment o) {
        long compareTime = o.getCreatedAt().getTime();

        return (int) (compareTime - this.getCreatedAt().getTime());
    }
}

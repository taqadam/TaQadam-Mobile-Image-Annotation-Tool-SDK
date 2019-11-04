package com.recoded.taqadam.models;

import android.net.Uri;
import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;

import java.util.Date;
import java.util.List;

/**
 * Created by wisam on Dec 25 17.
 */

public class Post extends Model implements Comparable<Post> {

    @Expose
    private User user;
    @Expose
    private List<Comment> comments;

    @Expose
    private Long commentsCount;
    @Expose
    private String title;
    @Expose
    private String body;
    @Expose
    private Date createdAt;

    public String getAuthor() {
        return user.getName();
    }

    public Uri getAuthorImage() {
        return user.getProfile().getAvatar();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Long commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int compareTo(@NonNull Post o) {
        long compareTime = o.getCreatedAt().getTime();

        return (int) (compareTime - this.getCreatedAt().getTime());
    }

    public String getTruncatedBody() {
        return body.length() > 150 ? body.substring(0, 150).concat("…") : body;
    }
}

package com.recoded.taqadam.models;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.recoded.taqadam.models.db.PostDbHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wisam on Dec 25 17.
 */

public class Comment implements Comparable<Comment> {
    private String uid;
    private String author;
    private Uri authorImg;
    private String body;
    private String id;
    private String postId;
    private long commentTime;

    public Comment(String id) {
        this.id = id;
    }

    public Comment() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Uri getAuthorImage() {
        return authorImg;
    }

    public void setAuthorImage(Uri authorImg) {
        this.authorImg = authorImg;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public long getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(long commentTime) {
        this.commentTime = commentTime;
    }

    public Comment fromMap(Map<String, Object> map) {
        uid = (String) map.get(PostDbHandler.USER_ID);
        author = (String) map.get(PostDbHandler.AUTHOR);
        authorImg = Uri.parse((String) map.get(PostDbHandler.AUTHOR_IMG));
        commentTime = (long) map.get(PostDbHandler.TIMESTAMP);
        body = (String) map.get(PostDbHandler.BODY);
        return this;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(PostDbHandler.USER_ID, uid);
        result.put(PostDbHandler.AUTHOR, author);
        result.put(PostDbHandler.AUTHOR_IMG, authorImg.toString());
        result.put(PostDbHandler.BODY, body);
        result.put(PostDbHandler.TIMESTAMP, commentTime);
        return result;
    }

    @Override
    public int compareTo(@NonNull Comment o) {
        long compareTime = o.getCommentTime();

        return (int) (compareTime - this.getCommentTime());
    }
}

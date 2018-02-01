package com.recoded.taqadam.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.recoded.taqadam.models.db.PostDbHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wisam on Dec 25 17.
 */

@IgnoreExtraProperties
public class Post {

    private String uid;
    private String author;
    private List<Comment> comments;
    private int noOfComments;
    private String title;
    private String body;
    private long postTime;
    private String id;

    public Post() {
    }

    ;

    public Post(String id) {
        this.id = id;
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

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public int getNoOfComments() {
        return noOfComments;
    }

    public void setNoOfComments(int noOfComments) {
        this.noOfComments = noOfComments;
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

    public long getPostTime() {
        return postTime;
    }

    public void setPostTime(long postTime) {
        this.postTime = postTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Post fromMap(Map<String, Object> map) {
        uid = (String) map.get(PostDbHandler.USER_ID);
        title = (String) map.get(PostDbHandler.TITLE);
        author = (String) map.get(PostDbHandler.AUTHOR);
        noOfComments = ((Long) map.get(PostDbHandler.COMMENTS)).intValue();
        postTime = (long) map.get(PostDbHandler.TIMESTAMP);
        body = (String) map.get(PostDbHandler.TRUNCATED_BODY);

        return this;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(PostDbHandler.USER_ID, uid);
        result.put(PostDbHandler.AUTHOR, author);
        result.put(PostDbHandler.TITLE, title);
        result.put(PostDbHandler.COMMENTS, noOfComments);

        String truncatedBody = body.length() > 150 ? body.substring(0, 150) : body;

        result.put(PostDbHandler.TRUNCATED_BODY, truncatedBody);
        result.put(PostDbHandler.TIMESTAMP, postTime);
        return result;
    }
}

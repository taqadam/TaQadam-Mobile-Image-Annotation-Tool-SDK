package com.recoded.taqadam.models;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.recoded.taqadam.models.auth.UserAuthHandler;
import com.recoded.taqadam.models.db.PostDbHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wisam on Dec 25 17.
 */

@IgnoreExtraProperties
public class Post implements Comparable<Post> {

    private String uid;
    private String author;
    private Uri authorImg;
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

    public Uri getAuthorImage() {
        return authorImg;
    }

    public void setAuthorImg(Uri imgUri) {
        authorImg = imgUri;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Comment> getComments() {
        if (comments == null) {
            comments = new ArrayList<>();
        }
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
        if (map.containsKey(PostDbHandler.AUTHOR_IMG))
            authorImg = Uri.parse((String) map.get(PostDbHandler.AUTHOR_IMG));
        else
            authorImg = UserAuthHandler.getInstance().getCurrentUser().getPicturePath();
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
        result.put(PostDbHandler.AUTHOR_IMG, authorImg.toString());
        result.put(PostDbHandler.TITLE, title);
        result.put(PostDbHandler.COMMENTS, noOfComments);

        String truncatedBody = getTruncatedBody();

        result.put(PostDbHandler.TRUNCATED_BODY, truncatedBody);
        result.put(PostDbHandler.TIMESTAMP, postTime);
        return result;
    }

    @Override
    public int compareTo(@NonNull Post o) {
        long compareTime = o.getPostTime();

        return (int) (compareTime - this.getPostTime());
    }

    public String getTruncatedBody() {
        return body.length() > 150 ? body.substring(0, 150).concat("…") : body;
    }
}

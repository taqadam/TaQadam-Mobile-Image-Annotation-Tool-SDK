package com.recoded.taqadam.models.db;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.recoded.taqadam.models.Comment;
import com.recoded.taqadam.models.Post;
import com.recoded.taqadam.models.auth.UserAuthHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by wisam on Dec 25 17.
 */

public class PostDbHandler {
    public static final String
            TITLE = "title",
            BODY = "body",
            TRUNCATED_BODY = "truncated_body",
            TIMESTAMP = "ts",
            USER_ID = "uid",
            AUTHOR = "author",
            COMMENTS = "comments";

    private static PostDbHandler handler;
    private HashMap<String, Post> postsList;
    private Task<Void> latestPostsTask;

    private String mUid;
    private DatabaseReference mThreadsDbRef, mDataDbRef;
    private ChildEventListener mCommentsListener, mThreadsListener;

    public static PostDbHandler getInstance() {
        if (handler == null) {
            handler = new PostDbHandler();
        }
        return handler;
    }

    private PostDbHandler() {
        mUid = UserAuthHandler.getInstance().getUid();
        this.mThreadsDbRef = FirebaseDatabase.getInstance().getReference()
                .child("Posts")
                .child("threads");
        this.mDataDbRef = FirebaseDatabase.getInstance().getReference()
                .child("Posts")
                .child("data");
        this.postsList = new HashMap<>();

        setupLatestThreadsListener();

        //Threads will be categorized by year-month, i.e. 2017-12
        //Read the latest posts (current month)
        mThreadsDbRef.child(getCurrentTimeCycle()).addChildEventListener(mThreadsListener);
        final TaskCompletionSource<Void> fetcher = new TaskCompletionSource<>();
        mThreadsDbRef.child(getCurrentTimeCycle()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //All threads will be added onChildAdded then this will be called so we just set the result
                /*HashMap<String, HashMap<String, Object>> posts
                        = (HashMap<String, HashMap<String, Object>>) dataSnapshot.getValue();

                if (posts != null) {
                    for (String key : posts.keySet()) {
                        Post post = new Post(key).fromMap(posts.get(key));
                        postsList.put(key, post);
                    }
                }*/

                //If the results are less than 5, read last months as well
                if (dataSnapshot.getChildrenCount() <= 5) {
                    String timeCycle = getPreviousTimeCycle(getCurrentTimeCycle());
                    getPosts(timeCycle).addOnCompleteListener(new OnCompleteListener<HashMap<String, Post>>() {
                        @Override
                        public void onComplete(@NonNull Task<HashMap<String, Post>> task) {
                            if (task.isSuccessful()) {
                                fetcher.setResult(null);
                            } else {
                                fetcher.setException(task.getException());
                            }
                        }
                    });
                } else {
                    fetcher.setResult(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                fetcher.setException(databaseError.toException());
            }
        });

        latestPostsTask = fetcher.getTask();
    }

    //This will be used to read more older posts only
    @NonNull
    public Task<HashMap<String, Post>> getPosts(String timeCycle) {
        final TaskCompletionSource<HashMap<String, Post>> fetcher = new TaskCompletionSource<>();

        if (timeCycle.equals(getCurrentTimeCycle())) {
            fetcher.setResult(null);
            return fetcher.getTask();
        }

        mThreadsDbRef.child(timeCycle).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, HashMap<String, Object>> posts
                        = (HashMap<String, HashMap<String, Object>>) dataSnapshot.getValue();

                //we will update current posts and return the new posts
                HashMap<String, Post> returnList = new HashMap<>();
                if (posts != null) {
                    for (String key : posts.keySet()) {
                        Post post = new Post(key).fromMap(posts.get(key));
                        postsList.put(key, post);
                        returnList.put(key, post);
                    }
                }
                fetcher.setResult(returnList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                fetcher.setException(databaseError.toException());
            }
        });
        return fetcher.getTask();
    }

    //This will be used to read the body of single post
    @NonNull
    public Task<Post> getPost(final String postId) {
        final TaskCompletionSource<Post> fetcher = new TaskCompletionSource<>();
        mDataDbRef.child(postId).child(BODY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postsList.get(postId).setBody((String) dataSnapshot.getValue());
                getComments(postId).addOnSuccessListener(new OnSuccessListener<List<Comment>>() {
                    @Override
                    public void onSuccess(List<Comment> comments) {
                        postsList.get(postId).setComments(comments);
                        fetcher.setResult(postsList.get(postId));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        fetcher.setException(e);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                fetcher.setException(databaseError.toException());
            }
        });

        return fetcher.getTask();
    }

    //This will be used to read comments on a single post
    @NonNull
    private Task<List<Comment>> getComments(final String postId) {
        final TaskCompletionSource<List<Comment>> fetcher = new TaskCompletionSource<>();
        mDataDbRef.child(postId).child(COMMENTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, HashMap<String, Object>> comments
                        = (HashMap<String, HashMap<String, Object>>) dataSnapshot.getValue();
                List<Comment> list = new ArrayList<>();
                if (comments != null) {
                    for (String id : comments.keySet()) {
                        Comment comment = new Comment(id).fromMap(comments.get(id));
                        comment.setPostId(postId);
                        list.add(comment);
                    }
                }

                fetcher.setResult(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                fetcher.setException(databaseError.toException());
            }
        });

        return fetcher.getTask();
    }

    public void deletePost(Post post) {
        if (post.getId() != null) {
            mThreadsDbRef.child(getTimeCycleFromTs(post.getPostTime())).child(post.getId()).setValue(null);
            mDataDbRef.child(post.getId())
                    .setValue(null);
        }
    }

    public void deleteComment(Comment comment) {
        if (comment.getPostId() != null && comment.getId() != null) {
            mDataDbRef.child(comment.getPostId())
                    .child(COMMENTS)
                    .child(comment.getId())
                    .setValue(null);
        }
    }

    public void updatePost(Post post) {
        if (post.getId() != null) {
            mThreadsDbRef.child(getTimeCycleFromTs(post.getPostTime())).child(post.getId()).setValue(post.toMap());
            mDataDbRef.child(post.getId())
                    .child(BODY)
                    .setValue(post.getBody());
        }
    }

    public void updateComment(Comment comment) {
        if (comment.getPostId() != null && comment.getId() != null) {
            mDataDbRef.child(comment.getPostId())
                    .child(COMMENTS)
                    .child(comment.getId())
                    .setValue(comment.toMap());
        }
    }

    public Post writePost(Post post) {
        if (post.getId() == null) {
            post.setId(mThreadsDbRef.child(getCurrentTimeCycle()).push().getKey());
            mThreadsDbRef.child(getCurrentTimeCycle()).child(post.getId()).setValue(post.toMap());
            mDataDbRef.child(post.getId())
                    .child(BODY)
                    .setValue(post.getBody());
        }

        return post;
    }

    public Comment writeComment(Comment comment) {
        if (comment.getPostId() != null && comment.getId() == null) {
            comment.setId(mDataDbRef.child(comment.getPostId()).child(COMMENTS).push().getKey());
            mDataDbRef.child(comment.getPostId())
                    .child(COMMENTS)
                    .child(comment.getId())
                    .setValue(comment.toMap());
        }
        return comment;
    }

    private void setupLatestThreadsListener() {
        this.mThreadsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post post = new Post(dataSnapshot.getKey())
                        .fromMap((HashMap<String, Object>) dataSnapshot.getValue());
                postsList.put(post.getId(), post);
                //We need to monitor the list as well or call a callback in here
                //Add all existing children then Single Value Event Listener will be called and we will set the results
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                postsList.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    public static String getCurrentTimeCycle() {
        Calendar cal = Calendar.getInstance();
        String dateFormat = "yyyy-MM";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        return sdf.format(cal.getTime());
    }

    public static String getTimeCycleFromTs(long ts) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(ts));
        String dateFormat = "yyyy-MM";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        return sdf.format(cal.getTime());
    }

    public static String getPreviousTimeCycle(String refTimeCycle) {
        Calendar cal = Calendar.getInstance();
        String dateFormat = "yyyy-MM";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        try {
            cal.setTime(sdf.parse(refTimeCycle));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cal.roll(Calendar.MONTH, false);

        return sdf.format(cal.getTime());
    }
}

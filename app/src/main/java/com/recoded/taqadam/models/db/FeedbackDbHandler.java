package com.recoded.taqadam.models.db;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.recoded.taqadam.BuildConfig;
import com.recoded.taqadam.models.auth.UserAuthHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wisam on Mar 3 18.
 */

public class FeedbackDbHandler {
    public static final String
            UID = "uid",
            FEEDBACK = "feedback",
            COMMENT = "comment",
            APP_VER = "app_ver",
            TIMESTAMP = "timestamp";

    private static FeedbackDbHandler handler;
    private String mUid;
    private DatabaseReference mFeedbackDbRef;

    private FeedbackDbHandler() {
        mUid = UserAuthHandler.getInstance().getUid();
        this.mFeedbackDbRef = FirebaseDatabase.getInstance().getReference()
                .child("App").child("Feedback");
    }

    public synchronized static FeedbackDbHandler getInstance() {
        if (handler == null) {
            handler = new FeedbackDbHandler();
        }
        return handler;
    }

    public void submitFeedback(String feedback, String comment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(UID, mUid);
        payload.put(FEEDBACK, feedback);
        payload.put(COMMENT, comment);
        payload.put(APP_VER, BuildConfig.VERSION_CODE);
        payload.put(TIMESTAMP, new Date().getTime());

        mFeedbackDbRef.push().setValue(payload);
    }
}

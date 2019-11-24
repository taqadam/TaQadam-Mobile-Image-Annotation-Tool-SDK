package com.recoded.taqadam.models.db;

import android.content.Context;
import android.util.Log;

import com.recoded.taqadam.models.Answer;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Answer.class}, version = 1, exportSchema = false)
public abstract class AnswersDatabase extends RoomDatabase {
    private static final String TAG = AnswersDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DB_NAME = "answers-db";
    private static AnswersDatabase sInstance;

    public static AnswersDatabase getInstance(Context ctx) {
        if(sInstance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "Creating new Database Instance");
                sInstance = Room
                        .databaseBuilder(ctx.getApplicationContext(), AnswersDatabase.class, DB_NAME)
                        .build();
            }
        }
        Log.d(TAG, "Getting database instance");
        return sInstance;
    }

    public abstract AnswersDao answersDao();
    public static void destroyInstance() {
        sInstance = null;
    }
}

package com.recoded.taqadam.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preference {
    public static String LAST_URL_OF_ANNOTATOR = "last_url_of_annotator_in_project_";
    public static String LAST_URL_OF_VALIDATOR  = "last_url_of_validator_in_project_";
    public static String LAST_TASK_ID_OF_ANNOTATOR = "last_task_id_of_annotator_in_project_";
    public static String LAST_TASK_ID_OF_VALIDATOR = "last_task_id_of_validator_in_project_";
    public static String LAST_ANSWER_FOR_VALIDATOR = "last_answer_for_validator_in_project_";

    Context context;
    SharedPreferences mPrefs;
    SharedPreferences.Editor editor;
    public Preference(Context context){
        this.context = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = mPrefs.edit();
    }

    public void putString(String key, String value){
        editor.putString(key, value);
        editor.apply();
        editor.commit();
    }

    public String getString(String key){
        return mPrefs.getString(key, null);
    }

    public void putBoolean(String key, boolean value){
        editor.putBoolean(key, value);
        editor.apply();
        editor.commit();
    }

    public boolean getBoolean(String key){
        return mPrefs.getBoolean(key, false);
    }

    public void putInt(String key, int i) {
        editor.putInt(key, i);
        editor.apply();
        editor.commit();
    }

    public void putLong(String key, long l) {
        editor.putLong(key, l);
        editor.apply();
        editor.commit();
    }

    public long getLong(String key) {
        return mPrefs.getLong(key, -1);
    }
}

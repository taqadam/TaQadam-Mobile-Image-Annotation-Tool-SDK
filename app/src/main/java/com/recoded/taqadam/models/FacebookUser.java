package com.recoded.taqadam.models;

import org.json.JSONObject;

/**
 * Created by wisam on Dec 13 17.
 */
public class FacebookUser {
    private String birthday;
    private String gender;
    private String name;
    private String lastName;
    private String id;
    private String firstName;
    private String pictureUrl;
    private int pictureWidth, pictureHeight;
    private String email;

    public FacebookUser(JSONObject res) {
        this.id = res.optString("id", null);
        this.email = res.optString("email", null);
        this.birthday = res.optString("birthday", null);
        this.gender = res.optString("gender", null);
        this.name = res.optString("name", null);
        this.firstName = res.optString("first_name", null);
        this.lastName = res.optString("last_name", null);
        JSONObject pictureData = res.optJSONObject("picture").optJSONObject("data");
        if (pictureData != null) {
            if (!pictureData.optBoolean("is_silhouette", true)) {
                this.pictureUrl = pictureData.optString("url", null);
                this.pictureHeight = pictureData.optInt("height", 0);
                this.pictureWidth = pictureData.optInt("width", 0);
            }
        }
    }

    public String getBirthday() {
        return birthday;
    }

    public String getGender() {
        return gender;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public int getPictureWidth() {
        return pictureWidth;
    }

    public int getPictureHeight() {
        return pictureHeight;
    }

    public String getEmail() {
        return email;
    }
}

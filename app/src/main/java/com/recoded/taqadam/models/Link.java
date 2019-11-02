package com.recoded.taqadam.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.recoded.taqadam.utils.Utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class Link implements Parcelable {
    public String id = Utils.getRandomString(5);
    public List<String> regionIds = new ArrayList<>();

    public Link() {

    }

    public Link(String id, List<String> ids) {
        this.id = id;
        regionIds.addAll(ids);
    }

    public JSONArray toJsonArray() {
        return new JSONArray(regionIds);
    }
    private Link(Parcel in) {
        id = in.readString();
        regionIds = in.createStringArrayList();
    }

    public static final Creator<Link> CREATOR = new Creator<Link>() {
        @Override
        public Link createFromParcel(Parcel in) {
            return new Link(in);
        }

        @Override
        public Link[] newArray(int size) {
            return new Link[size];
        }
    };

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeStringList(regionIds);
    }
}

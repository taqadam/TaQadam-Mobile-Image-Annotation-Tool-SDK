package com.recoded.taqadam.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wisam on Feb 27 18.
 */

public class ImageOld implements Parcelable {
    public Uri path;
    public String id;
    public boolean skipped = false;
    public int skipCount;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeParcelable(this.path, flags);
    }

    public ImageOld() {
    }

    protected ImageOld(Parcel in) {
        this.id = in.readString();
        this.path = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Parcelable.Creator<ImageOld> CREATOR = new Parcelable.Creator<ImageOld>() {
        @Override
        public ImageOld createFromParcel(Parcel source) {
            return new ImageOld(source);
        }

        @Override
        public ImageOld[] newArray(int size) {
            return new ImageOld[size];
        }
    };
}

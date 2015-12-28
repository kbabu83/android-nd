package com.infinity.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by KBabu on 21-Dec-15.
 */
public class Video implements Parcelable {
    private String id;
    private String name;
    private String link;
    private String type;

    public Video(String id, String name, String link, String type) {
        this.id = id;
        this.name = name;
        this.link = link;
        this.type = type;
    }

    protected Video(Parcel in) {
        id = in.readString();
        name = in.readString();
        link = in.readString();
        type = in.readString();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public String getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeString(getName());
        dest.writeString(getLink());
        dest.writeString(getType());
    }

}

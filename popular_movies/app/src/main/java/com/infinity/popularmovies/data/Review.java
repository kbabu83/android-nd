package com.infinity.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by KBabu on 16-Dec-15.
 */
public class Review implements Parcelable {
    private int id;
    private String author;
    private String content;
    private String url;

    public Review(int id, String author, String content, String url) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    private Review(Parcel in) {
        this.id = in.readInt();
        this.author = in.readString();
        this.content = in.readString();
        this.url = in.readString();
    }

    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel source) {
            return new Review(source);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeString(getAuthor());
        dest.writeString(getContent());
        dest.writeString(getUrl());

    }
}

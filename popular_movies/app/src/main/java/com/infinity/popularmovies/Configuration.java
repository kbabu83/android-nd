package com.infinity.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

/**
 * Created by KBabu on 23-Nov-15.
 */
public class Configuration implements Parcelable {
    private String imageBaseUrl = null;
    private String imageBaseUrlSecure = null;
    private List<String> posterImageSizes;

    public static final String PREFERRED_IMAGE_SIZE = "w300";

    //use this Constructor to get a default configuration before-hand
    public Configuration() {
        final String[] defaultPosterSizes = { "w92", "w154", "w185", "w342", "w500", "w780", "original"};
        setImageBaseUrl("http://image.tmdb.org/t/p/");
        setImageBaseUrlSecure("https://image.tmdb.org/t/p/");
        setPosterImageSizes(Arrays.asList(defaultPosterSizes));
    }

    public Configuration(@NonNull String baseUrl, @NonNull String baseUrlSecure, @NonNull String[] posterSizes) {
        setImageBaseUrl(baseUrl);
        setImageBaseUrlSecure(baseUrlSecure);
        setPosterImageSizes(Arrays.asList(posterSizes));
    }

    public Configuration(@NonNull String baseUrl, @NonNull String baseUrlSecure, @NonNull List<String> posterSizes) {
        setImageBaseUrl(baseUrl);
        setImageBaseUrlSecure(baseUrlSecure);
        setPosterImageSizes(posterSizes);
    }

    public Configuration(Parcel in) {
        imageBaseUrl = in.readString();
        imageBaseUrlSecure = in.readString();
        in.readStringList(posterImageSizes);

    }

    public static final Creator<Configuration> CREATOR = new Creator<Configuration>() {
        @Override
        public Configuration createFromParcel(Parcel in) {
            return new Configuration(in);
        }

        @Override
        public Configuration[] newArray(int size) {
            return new Configuration[size];
        }
    };

    public String getImageBaseUrl() {
        return imageBaseUrl;
    }

    public String getImageBaseUrlSecure() {
        return imageBaseUrlSecure;
    }

    public List<String> getPosterImageSizes() {
        return posterImageSizes;
    }

    public void setImageBaseUrl(@NonNull String imgBaseUrl) {
        this.imageBaseUrl = imgBaseUrl;
    }

    public void setImageBaseUrlSecure(@NonNull String imgBaseUrlSecure) {
        this.imageBaseUrlSecure = imgBaseUrlSecure;
    }

    public void setPosterImageSizes(@NonNull List<String> posterImageSizes) {
        this.posterImageSizes = posterImageSizes;
    }

    public boolean ready() {
        return ((imageBaseUrl != null && !imageBaseUrl.isEmpty()) &&
                (imageBaseUrlSecure != null && !imageBaseUrlSecure.isEmpty()) &&
                (posterImageSizes != null && posterImageSizes.size() > 0));

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getImageBaseUrl());
        dest.writeString(getImageBaseUrlSecure());
        dest.writeStringList(getPosterImageSizes());

    }

}


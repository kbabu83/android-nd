package com.infinity.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by KBabu on 23-Nov-15.
 */
public class Configuration implements Parcelable {
    private String imageBaseUrl = null;
    private String imageBaseUrlSecure = null;
    private List<String> posterImageSizes;

    private static final String HTTP_URL_PATTERN = "^http.*://.+$";
    private static final String HTTP_URL_BEGIN_PATTERN = "http.*://";

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
        this.imageBaseUrl = httpTrim(imgBaseUrl);
    }

    public void setImageBaseUrlSecure(@NonNull String imgBaseUrlSecure) {
        this.imageBaseUrlSecure = httpTrim(imgBaseUrlSecure);
    }

    public void setPosterImageSizes(@NonNull List<String> posterImageSizes) {
        this.posterImageSizes = posterImageSizes;
    }

    public boolean ready() {
        return ((imageBaseUrl != null && !imageBaseUrl.isEmpty()) &&
                (imageBaseUrlSecure != null && !imageBaseUrlSecure.isEmpty()) &&
                (posterImageSizes != null && posterImageSizes.size() > 0));

    }

    private String httpTrim(@NonNull String url) {
        if (url.matches(HTTP_URL_PATTERN)) {
            url = url.replaceFirst(HTTP_URL_BEGIN_PATTERN, "");
            if (url.endsWith("/"))
                url = url.substring(0, url.length() - 1);
        }

        return url;
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


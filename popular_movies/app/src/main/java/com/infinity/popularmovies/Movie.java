package com.infinity.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.infinity.popularmovies.data.Review;
import com.infinity.popularmovies.data.Video;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to hold details of movies
 * Created by KBabu on 24-Nov-15.
 */
public class Movie implements Parcelable {
    private int id;
    private String title;
    private String language;
    private String posterThumbnail;
    private String synopsis;
    private double rating;
    private int voteCount;
    private int duration;
    private Date releaseDate;
    private List<Video> trailers = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();

    public Movie(int id, String title, String language, String posterThumbnail, String synopsis,
                 double rating, int voteCount, int duration, Date releaseDate, List<Video> trailers,
                 List<Review> reviews) {
        this.id = id;
        this.title = title;
        this.language = language;
        this.posterThumbnail = posterThumbnail;
        this.synopsis = synopsis;
        this.rating = rating;
        this.voteCount = voteCount;
        this.duration = duration;
        this.releaseDate = releaseDate;
        if (trailers != null)
            this.trailers.addAll(trailers);

        if (reviews != null)
            this.reviews.addAll(reviews);
    }

    private Movie(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.language = in.readString();
        this.posterThumbnail = in.readString();
        this.synopsis = in.readString();
        this.rating = in.readDouble();
        this.voteCount = in.readInt();
        this.duration = in.readInt();
        try {
            this.releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(in.readString());
        } catch (ParseException e) {
            Log.e("Movie", "Date parse failed: " + e.getMessage());
        }
        in.readTypedList(this.trailers, Video.CREATOR);
        in.readTypedList(this.reviews, Review.CREATOR);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeString(getTitle());
        dest.writeString(getLanguage());
        dest.writeString(getPosterThumbnail());
        dest.writeString(getSynopsis());
        dest.writeDouble(getRating());
        dest.writeInt(getVoteCount());
        dest.writeInt(getDuration());
        dest.writeString(new SimpleDateFormat("yyyy-MM-dd").format(getReleaseDate()));
        dest.writeTypedList(getTrailers());
        dest.writeTypedList(getReviews());

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPosterThumbnail() {
        return posterThumbnail;
    }

    public void setPosterThumbnail(String posterThumbnail) {
        this.posterThumbnail = posterThumbnail;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public List<Video> getTrailers() {
        return trailers;
    }

    public void setTrailers(List<Video> trailers) {
        this.trailers = trailers;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}


package com.infinity.popularmovies.netapi;

/**
 * Created by KBabu on 18-Dec-15.
 */
public class MovieResponse {
    private int id;
    private String title;
    private String original_language;
    private int runtime;
    private String overview;
    private String release_date;
    private String poster_path;
    private int vote_count;
    private double vote_average;

    public MovieResponse(int id, String title, String original_language, int runtime,
                         String overview, String release_date, String poster_path, int vote_count,
                         double vote_average) {
        this.id = id;
        this.title = title;
        this.original_language = original_language;
        this.runtime = runtime;
        this.overview = overview;
        this.release_date = release_date;
        this.poster_path = poster_path;
        this.vote_count = vote_count;
        this.vote_average = vote_average;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return original_language;
    }

    public int getRuntime() {
        return runtime;
    }

    public String getOverview() {
        return overview;
    }

    public String getReleaseDate() {
        return release_date;
    }

    public String getPosterPath() {
        return poster_path;
    }

    public int getVoteCount() {
        return vote_count;
    }

    public double getVoteAverage() {
        return vote_average;
    }
}


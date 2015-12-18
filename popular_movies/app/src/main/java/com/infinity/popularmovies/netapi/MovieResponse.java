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
    private String vote_count;
    private String vote_average;

    public MovieResponse(int id, String title, String original_language, int runtime,
                         String overview, String release_date, String poster_path, String vote_count,
                         String vote_average) {
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

    public String getOriginal_language() {
        return original_language;
    }

    public int getRuntime() {
        return runtime;
    }

    public String getOverview() {
        return overview;
    }

    public String getRelease_date() {
        return release_date;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getVote_count() {
        return vote_count;
    }

    public String getVote_average() {
        return vote_average;
    }
}


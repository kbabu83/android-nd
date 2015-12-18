package com.infinity.popularmovies.netapi;

import com.infinity.popularmovies.Movie;

import java.util.List;

/**
 * Created by KBabu on 18-Dec-15.
 */
public class TmdbResponse {
    private int page;
    private List<Movie> results;
    private int total_results;
    private int total_pages;

    public TmdbResponse(int page, List<Movie> movies, int results, int pages) {
        this.page = page;
        this.results = movies;
        this.total_results = results;
        this.total_pages = pages;
    }


}

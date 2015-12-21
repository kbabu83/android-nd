package com.infinity.popularmovies.netapi;

import java.util.List;

/**
 * Created by KBabu on 18-Dec-15.
 */
public class TmdbResponse {
    private int page;
    private List<MovieResponse> results;
    private int total_results;
    private int total_pages;

    public TmdbResponse(int page, List<MovieResponse> movies, int results, int pages) {
        this.page = page;
        this.results = movies;
        this.total_results = results;
        this.total_pages = pages;
    }

    public int getPage() {
        return page;
    }

    public List<MovieResponse> getResults() {
        return results;
    }

    public int getTotal_results() {
        return total_results;
    }

    public int getTotal_pages() {
        return total_pages;
    }
}

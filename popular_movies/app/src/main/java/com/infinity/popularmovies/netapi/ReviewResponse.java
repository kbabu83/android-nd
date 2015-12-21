package com.infinity.popularmovies.netapi;

/**
 * Created by KBabu on 21-Dec-15.
 */
public class ReviewResponse {
    private int id;
    private int page;
    private ReviewItem[] results;
    private int total_pages;
    private int total_results;

    public class ReviewItem {
        private String id;
        private String author;
        private String content;
        private String url;

        public ReviewItem(String reviewId, String author, String content, String url) {
            this.id = reviewId;
            this.author = author;
            this.content = content;
            this.url = url;
        }

        public String getId() {
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
    }

    public ReviewResponse(int movieId, int page, ReviewItem[] reviewItems, int pages, int results) {
        this.id = movieId;
        this.page = page;
        this.results = reviewItems;
        this.total_pages = pages;
        this.total_results = results;
    }

    public int getId() {
        return id;
    }

    public int getPage() {
        return page;
    }

    public ReviewItem[] getResults() {
        return results;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public int getTotal_results() {
        return total_results;
    }


}


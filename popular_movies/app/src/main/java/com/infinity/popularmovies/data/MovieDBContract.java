package com.infinity.popularmovies.data;

import android.provider.BaseColumns;

/**
 * Created by KBabu on 05-Dec-15.
 */
public class MovieDBContract {
    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_MOVIE_TITLE = "title";
        public static final String COLUMN_MOVIE_PLOT = "plot";
        public static final String COLUMN_MOVIE_POSTER = "poster";
        public static final String COLUMN_MOVIE_RATING = "rating";
        public static final String COLUMN_MOVIE_POPULARITY = "popularity";
        public static final String COLUMN_MOVIE_RELEASE_DATE = "release_date";
        public static final String COLUMN_MOVIE_VOTE_COUNT = "vote_count";
        public static final String COLUMN_MOVIE_TRAILER = "trailer";

    }

    public static final class FavouriteEntry implements BaseColumns {
        public static final String TABLE_NAME = "favourites";

        public static final String COLUMN_MOVIE_KEY = "movie_id";

    }

    static final class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "reviews";

        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_REVIEW_AUTHOR = "author";
        public static final String COLUMN_REVIEW = "review";
        public static final String COLUMN_MOVIE_ID = "movie_id";


    }

}

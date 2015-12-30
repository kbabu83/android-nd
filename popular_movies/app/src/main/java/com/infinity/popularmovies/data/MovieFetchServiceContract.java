package com.infinity.popularmovies.data;

/**
 * Created by KBabu on 16-Dec-15.
 */
public class MovieFetchServiceContract {
    // Actions that the IntentService can perform
    public static final String ACTION_DISCOVER_MOVIES = "com.infinity.popularmovies.action.DISCOVER";
    public static final String ACTION_FETCH_MOVIE_DETAILS = "com.infinity.popularmovies.action.FETCH";

    //Replies for actions IntentService can perform
    public static final String REPLY_DISCOVER_MOVIES_UPDATE = "com.infinity.popularmovies.reply.DISCOVER";
    public static final String REPLY_FETCH_MOVIE_UPDATE = "com.infinity.popularmovies.reply.FETCH";
    public static final String REPLY_FETCH_MOVIE_TRAILERS = "com.infinity.popularmovies.reply.TRAILERS";
    public static final String REPLY_FETCH_MOVIE_REVIEWS = "com.infinity.popularmovies.reply.REVIEWS";

    // Extras attached to Intents and replies
    public static final String EXTRA_PAGE_NUM = "com.infinity.popularmovies.extra.PAGE_NUM";
    public static final String EXTRA_SORT_BY = "com.infinity.popularmovies.extra.SORT_BY";
    public static final String EXTRA_MIN_VOTES = "com.infinity.popularmovies.extra.MIN_VOTES";
    public static final String EXTRA_MOVIE_ID = "com.infinity.popularmovies.extra.MOVIE_ID";
    public static final String EXTRA_MOVIE_LIST = "com.infinity.popularmovies.extra.MOVIES";
    public static final String EXTRA_MOVIE_DETAILS = "com.infinity.popularmovies.extra.MOVIE_DETAILS";
    public static final String EXTRA_MOVIE_TRAILERS = "com.infinity.popularmovies.extra.MOVIES";
    public static final String EXTRA_MOVIE_REVIEWS = "com.infinity.popularmovies.extra.REVIEWS";

    public static final int DEFAULT_MIN_VOTES_REQ = 20;
    public static final String SORT_SETTING_POPULARITY = "com.infinity.popularmovies.POPULARITY";
    public static final String SORT_SETTING_RATING = "com.infinity.popularmovies.VOTE_AVERAGE";
    public static final String SORT_SETTING_FAVOURITE = "com.infinity.popularmovies.USER_FAVOURITES";

}


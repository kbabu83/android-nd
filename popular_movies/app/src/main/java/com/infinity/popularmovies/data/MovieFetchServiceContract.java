package com.infinity.popularmovies.data;

/**
 * Created by KBabu on 16-Dec-15.
 */
public interface MovieFetchServiceContract {
    // Actions that the IntentService can perform
    public static final String ACTION_DISCOVER_MOVIES = "com.infinity.popularmovies.action.DISCOVER";
    public static final String ACTION_FETCH_MOVIE_DETAILS = "com.infinity.popularmovies.action.FETCH";

    //Replies for actions IntentService can perform
    public static final String REPLY_DISCOVER_MOVIES_UPDATE = "com.infinity.popularmovies.reply.DISCOVER";

    // Extras attached to Intents and replies
    public static final String EXTRA_PAGE_NUM = "com.infinity.popularmovies.extra.PAGE_NUM";
    public static final String EXTRA_SORT_BY = "com.infinity.popularmovies.extra.SORT_BY";
    public static final String EXTRA_MOVIE_ID = "com.infinity.popularmovies.extra.MOVIE_ID";
    public static final String EXTRA_MOVIE_CONFIG = "com.infinity.popularmovies.extra.TMDB_CONFIG";
    public static final String EXTRA_MOVIE_LIST = "com.infinity.popularmovies.extra.MOVIES";

    public static final String SORT_SETTING_POPULARITY = "com.infinity.popularmovies.POPULARITY";
    public static final String SORT_SETTING_RATING = "com.infinity.popularmovies.VOTE_AVERAGE";
    public static final String SORT_SETTING_FAVOURITE = "com.infinity.popularmovies.USER_FAVOURITES";



}
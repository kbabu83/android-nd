package com.infinity.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by KBabu on 29-Dec-15.
 */
public class FavouritesOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "the_movie_db";
    public static final int DATABASE_VERSION = 1;

    private final static String CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieDBContract.MovieEntry.TABLE_NAME +
            "(" + MovieDBContract.MovieEntry._ID + " INTEGER PRIMARY KEY, " +
            MovieDBContract.MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
            MovieDBContract.MovieEntry.COLUMN_MOVIE_PLOT + " TEXT NOT NULL, " +
            MovieDBContract.MovieEntry.COLUMN_MOVIE_POSTER + " TEXT NOT NULL, " +
            MovieDBContract.MovieEntry.COLUMN_MOVIE_RATING + " REAL NOT NULL, " +
            MovieDBContract.MovieEntry.COLUMN_MOVIE_POPULARITY + " REAL, " +
            MovieDBContract.MovieEntry.COLUMN_MOVIE_VOTE_COUNT + " INTEGER NOT NULL, " +
            MovieDBContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT" +
            ")";

    private final static String CREATE_FAV_TABLE = "CREATE TABLE " + MovieDBContract.FavouriteEntry.TABLE_NAME +
            "(" + MovieDBContract.FavouriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MovieDBContract.FavouriteEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + MovieDBContract.FavouriteEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
            MovieDBContract.MovieEntry.TABLE_NAME + "(" + MovieDBContract.MovieEntry._ID + ")" +
            ")";

    public FavouritesOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MOVIE_TABLE);
        db.execSQL(CREATE_FAV_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieDBContract.MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieDBContract.FavouriteEntry.TABLE_NAME);
        onCreate(db);
    }
}


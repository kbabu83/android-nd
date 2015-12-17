package com.infinity.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class MovieProvider extends ContentProvider {
    private static final String LOG_TAG = MovieProvider.class.getSimpleName();
    private static final String CONTENT_URI = "com.infinity.popularmovies.provider";

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private MovieDBOpenHelper mOpenHelper;

    private SQLiteDatabase db;

    private static final int CODE_URI_MATCH_MOVIES = 1;
    private static final int CODE_URI_MATCH_FAVOURITES = 2;

    public MovieProvider() { }

    @Override
    public boolean onCreate() {
        sUriMatcher.addURI(CONTENT_URI, "movies", CODE_URI_MATCH_MOVIES);
        sUriMatcher.addURI(CONTENT_URI, "favourites", CODE_URI_MATCH_FAVOURITES);
        mOpenHelper = new MovieDBOpenHelper(getContext());
        db = mOpenHelper.getWritableDatabase();

        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uri == null) {
            return null;
        }

        switch (sUriMatcher.match(uri)) {
            case CODE_URI_MATCH_MOVIES:
                db.insert(MovieDBContract.MovieEntry.TABLE_NAME, null, values);
                break;

            case CODE_URI_MATCH_FAVOURITES:
                db.insert(MovieDBContract.FavouriteEntry.TABLE_NAME, null, values);
                break;

            default:
                break;
        }

        return uri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (uri == null) {
            return -1;
        }

        int code = sUriMatcher.match(uri);
        String tableName = (code == CODE_URI_MATCH_MOVIES) ?
                MovieDBContract.MovieEntry.TABLE_NAME : (code == CODE_URI_MATCH_FAVOURITES) ?
                MovieDBContract.FavouriteEntry.TABLE_NAME : null;
        if (tableName != null) {
            return db.update(tableName, values, selection, selectionArgs);
        }
        else {
            return -1;
        }

    }


    protected static class MovieDBOpenHelper extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = "movie_db";
        public static final int DATABASE_VERSION = 1;

        private final static String CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieDBContract.MovieEntry.TABLE_NAME +
                "(" + MovieDBContract.MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieDBContract.MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                MovieDBContract.MovieEntry.COLUMN_MOVIE_PLOT + " TEXT NOT NULL, " +
                MovieDBContract.MovieEntry.COLUMN_MOVIE_POSTER + " TEXT NOT NULL, " +
                MovieDBContract.MovieEntry.COLUMN_MOVIE_TRAILER + " TEXT NOT NULL, " +
                MovieDBContract.MovieEntry.COLUMN_MOVIE_RATING + " REAL NOT NULL, " +
                MovieDBContract.MovieEntry.COLUMN_MOVIE_POPULARITY + " REAL NOT NULL, " +
                MovieDBContract.MovieEntry.COLUMN_MOVIE_VOTE_COUNT + " INTEGER REAL NOT NULL, " +
                MovieDBContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT" +
                ")";

        private final static String CREATE_FAV_TABLE = "CREATE TABLE " + MovieDBContract.FavouriteEntry.TABLE_NAME +
                "(" + MovieDBContract.FavouriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieDBContract.FavouriteEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + MovieDBContract.FavouriteEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                MovieDBContract.MovieEntry.TABLE_NAME + "(" + MovieDBContract.MovieEntry._ID + ")" +
                ")";

        private final static String CREATE_REVIEW_TABLE = "CREATE TABLE " + MovieDBContract.ReviewEntry.TABLE_NAME +
                "(" + MovieDBContract.ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieDBContract.ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieDBContract.ReviewEntry.COLUMN_REVIEW_ID + " TEXT NOT NULL UNIQUE, " +
                MovieDBContract.ReviewEntry.COLUMN_REVIEW_AUTHOR + " TEXT NOT NULL, " +
                MovieDBContract.ReviewEntry.COLUMN_REVIEW + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + MovieDBContract.ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieDBContract.MovieEntry.TABLE_NAME + "(" + MovieDBContract.MovieEntry._ID + ")" +
                ")";

        public MovieDBOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v(LOG_TAG, "onCreate:: execute query : " + CREATE_MOVIE_TABLE);
            Log.v(LOG_TAG, "onCreate:: execute query : " + CREATE_FAV_TABLE);
            Log.v(LOG_TAG, "onCreate:: execute query : " + CREATE_REVIEW_TABLE);
            //db.execSQL(CREATE_MOVIE_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + MovieDBContract.MovieEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MovieDBContract.FavouriteEntry.TABLE_NAME);
            onCreate(db);

        }

    }
}

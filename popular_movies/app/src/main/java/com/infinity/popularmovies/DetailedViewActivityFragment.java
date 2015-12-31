package com.infinity.popularmovies;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.infinity.popularmovies.data.FavouritesOpenHelper;
import com.infinity.popularmovies.data.Movie;
import com.infinity.popularmovies.data.MovieDBContract;
import com.infinity.popularmovies.data.MovieFetchServiceContract;
import com.infinity.popularmovies.data.Review;
import com.infinity.popularmovies.data.Video;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailedViewActivityFragment extends Fragment {
    private static final String LOG_TAG = DetailedViewActivityFragment.class.getSimpleName();
    private Movie movie;
    protected Activity parent;

    private IntentFilter intentFilter = new IntentFilter();

    public DetailedViewActivityFragment() { }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null)
                return;

            final String action = intent.getAction();
            switch (action) {
                case MovieFetchServiceContract.REPLY_FETCH_MOVIE_UPDATE:
                    movie = intent.getParcelableExtra(MovieFetchServiceContract.EXTRA_MOVIE_DETAILS);
                    updateViewContent();
                    break;

                case MovieFetchServiceContract.REPLY_FETCH_MOVIE_TRAILERS:
                    List<Video> videos = intent.getParcelableArrayListExtra(MovieFetchServiceContract.EXTRA_MOVIE_TRAILERS);
                    if (videos == null || videos.size() == 0) {
                        Log.v(LOG_TAG, "No videos available for movie");
                        return;
                    }
                    movie.setTrailers(videos);
                    updateMovieTrailers();
                    break;

                case MovieFetchServiceContract.REPLY_FETCH_MOVIE_REVIEWS:
                    List<Review> reviews = intent.getParcelableArrayListExtra(MovieFetchServiceContract.EXTRA_MOVIE_REVIEWS);
                    if (reviews == null || reviews.size() ==0) {
                        Log.v(LOG_TAG, "No reviews available for movie");
                        return;
                    }
                    movie.setReviews(reviews);
                    updateMovieReviews();
                    break;

                default:
                    return;
            }

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parent = getActivity();
        intentFilter.addAction(MovieFetchServiceContract.REPLY_FETCH_MOVIE_UPDATE);
        intentFilter.addAction(MovieFetchServiceContract.REPLY_FETCH_MOVIE_TRAILERS);
        intentFilter.addAction(MovieFetchServiceContract.REPLY_FETCH_MOVIE_REVIEWS);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detailed_view, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_set_favourite);
        FavouritesOpenHelper dbHelper = new FavouritesOpenHelper(parent);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (isFavourite(db, movie)) {
            menuItem.setIcon(android.R.drawable.btn_star_big_on);
        }
        else {
            menuItem.setIcon(android.R.drawable.btn_star_big_off);
        }

        db.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_set_favourite) {
            FavouritesOpenHelper dbHelper = new FavouritesOpenHelper(parent);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            if (isFavourite(db, movie)) {
                Log.v(LOG_TAG, "Movie already set as favourite, removing from DB");
                if(removeFavourite(db, movie)) {
                    item.setIcon(android.R.drawable.btn_star_big_off);
                    Toast.makeText(parent, "Removed from favourites", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Log.v(LOG_TAG, "Movie not stored as fav yet; adding to DB");
                if (addFavourite(db, movie)) {
                    item.setIcon(android.R.drawable.btn_star_big_on);
                    Toast.makeText(parent, "Added to favourites", Toast.LENGTH_SHORT).show();
                }
            }

            db.close();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param db
     * @param movie
     * @return
     */
    private boolean addFavourite(SQLiteDatabase db, Movie movie) {
        ContentValues movieContentValues = new ContentValues();
        movieContentValues.put(MovieDBContract.MovieEntry._ID, movie.getId());
        movieContentValues.put(MovieDBContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
        movieContentValues.put(MovieDBContract.MovieEntry.COLUMN_MOVIE_PLOT, movie.getSynopsis());
        movieContentValues.put(MovieDBContract.MovieEntry.COLUMN_MOVIE_POSTER, movie.getPosterThumbnail());
        movieContentValues.put(MovieDBContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                new SimpleDateFormat("yyyy-MM-dd").format(movie.getReleaseDate()));
        movieContentValues.put(MovieDBContract.MovieEntry.COLUMN_MOVIE_RATING, movie.getRating());
        movieContentValues.put(MovieDBContract.MovieEntry.COLUMN_MOVIE_VOTE_COUNT, movie.getVoteCount());

        long rows = db.insert(MovieDBContract.MovieEntry.TABLE_NAME, MovieDBContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE, movieContentValues);

        if (rows != -1) {
            ContentValues favContentValues = new ContentValues();
            favContentValues.put(MovieDBContract.FavouriteEntry.COLUMN_MOVIE_KEY, movie.getId());

            rows = db.insert(MovieDBContract.FavouriteEntry.TABLE_NAME, MovieDBContract.FavouriteEntry.COLUMN_MOVIE_KEY, favContentValues);
        }

        return (rows != -1);
    }

    /**
     *
     * @param db
     * @param movie
     * @return
     */
    private boolean removeFavourite(SQLiteDatabase db, Movie movie) {
        String where = MovieDBContract.MovieEntry._ID + " = ?";
        String[] whereArgs1 = {String.valueOf(movie.getId())};
        int rows = db.delete(MovieDBContract.MovieEntry.TABLE_NAME, where, whereArgs1);

        if (rows > 0) {
            where = MovieDBContract.FavouriteEntry.COLUMN_MOVIE_KEY + " = ?";
            String[] whereArgs2 = {String.valueOf(movie.getId())};
            rows = db.delete(MovieDBContract.FavouriteEntry.TABLE_NAME, where, whereArgs2);
        }
        return (rows > 0);
    }

    /**
     *
     * @param db
     * @param movie
     * @return
     */
    private boolean isFavourite(SQLiteDatabase db, Movie movie) {
        String[] columns = {MovieDBContract.FavouriteEntry.COLUMN_MOVIE_KEY};
        String selection = MovieDBContract.FavouriteEntry.COLUMN_MOVIE_KEY + " = ?";
        String[] selectionArgs = {String.valueOf(movie.getId())};
        Cursor cursor = db.query(MovieDBContract.FavouriteEntry.TABLE_NAME, columns,
                selection, selectionArgs, null, null, null);

        boolean ret = (cursor.getCount() != 0);
        cursor.close();

        return ret;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detailed_view, container, false);
        Intent intent = parent.getIntent();
        movie = intent.getParcelableExtra("selected_movie");
        if (movie == null)
            return rootView;

        updateViewContent();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(parent).registerReceiver(broadcastReceiver, intentFilter);
        fetchMovieDetails(movie.getId());
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(parent).unregisterReceiver(broadcastReceiver);
    }

    /**
     *
     * @param movieId
     */
    private void fetchMovieDetails(int movieId) {
        MovieDataFetchHelperService.startActionFetchMovie(parent, movieId);
    }

    /**
     *
     */
    private void updateViewContent() {
        if (parent == null || movie == null) {
            return;
        }

        String movieName = movie.getTitle();
        String moviePosterUrl = movie.getPosterThumbnail();
        String moviePlot = movie.getSynopsis();
        String movieRating = String.valueOf(movie.getRating());
        String vote_count = String.valueOf(movie.getVoteCount());
        Date releaseDate = movie.getReleaseDate();
        parent.setTitle(movieName + " (" + (releaseDate.getYear() + 1900) + ")");

        ImageView imageView = (ImageView) parent.findViewById(R.id.img_poster_view_large);
        if (imageView != null) {
            Picasso.with(getActivity()).load(moviePosterUrl).into(imageView);
        }

        TextView ratingText = (TextView) parent.findViewById(R.id.txt_movie_rating);
        if (ratingText != null) {
            ratingText.setText(movieRating + "/10\t(" + vote_count + " votes)");
        }

        TextView plotText = (TextView) parent.findViewById(R.id.txt_movie_plot);
        if (plotText != null) {
            plotText.setText(moviePlot);
        }

        TextView runtimeText = (TextView) parent.findViewById(R.id.txt_movie_duration);
        if (runtimeText != null) {
            runtimeText.setText(String.valueOf(movie.getDuration()) + " mins");
        }

        TextView relDateText = (TextView) parent.findViewById(R.id.txt_movie_date);
        if (relDateText != null) {
            relDateText.setText(new SimpleDateFormat("dd-MM-yyyy").format(releaseDate));
        }

        if (movie.getTrailers() != null && !movie.getTrailers().isEmpty())
            updateMovieTrailers();

        if (movie.getReviews() != null && !movie.getReviews().isEmpty())
            updateMovieReviews();

    }

    /**
     *
     */
    private void updateMovieTrailers() {
        ViewGroup trailerListContainer = (ViewGroup) parent.findViewById(R.id.container_movie_trailers);
        if (trailerListContainer == null || movie.getTrailers() == null) {
            Log.v(LOG_TAG, "updateMovieTrailers: No trailer content to render");
            return;
        }

        int childCount = trailerListContainer.getChildCount();
        for (int i = childCount - 1; i > 0; --i) {
            trailerListContainer.removeViewAt(i);
        }

        class ItemTextClickListener implements View.OnClickListener {
            private String clickableLink;

            public ItemTextClickListener(String link) {
                this.clickableLink = link;
            }

            @Override
            public void onClick(View v) {
                Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickableLink));
                if (youtubeIntent.resolveActivity(parent.getPackageManager()) != null)
                    startActivity(youtubeIntent);
            }
        }

        for (Video video : movie.getTrailers()) {
            ViewGroup container = (ViewGroup) LayoutInflater.from(parent).inflate(
                    R.layout.list_item_movie_trailer, trailerListContainer, false);

            ImageView videoThumbnail = (ImageView) container.findViewById(R.id.thumbnail_preview);
            Picasso.with(parent).load(video.getThumbnail()).
                    placeholder(R.drawable.no_preview_available).into(videoThumbnail);

            TextView titleText = (TextView) container.findViewById(R.id.txt_trailer_title);
            titleText.setText(video.getName());

            //titleText.setOnClickListener(new ItemTextClickListener(video.getLink()));

            trailerListContainer.addView(container);
            trailerListContainer.setOnClickListener(new ItemTextClickListener(video.getLink()));
        }

    }

    /**
     *
     */
    private void updateMovieReviews() {
        ViewGroup reviewListContainer = (ViewGroup) parent.findViewById(R.id.container_movie_reviews);
        if (reviewListContainer == null || movie.getReviews() == null) {
            Log.v(LOG_TAG, "updateMovieReviews: No review content to render");
            return;
        }

        int childCount = reviewListContainer.getChildCount();
        for (int i = childCount - 1; i > 0; --i) {
            reviewListContainer.removeViewAt(i);
        }

        List<Review> reviews = movie.getReviews();
        for (Review review : reviews) {
            ViewGroup container = (ViewGroup) LayoutInflater.from(parent).inflate(
                    R.layout.list_item_movie_review, reviewListContainer, false);
            TextView authorText = (TextView) container.findViewById(R.id.txt_movie_review_title);
            authorText.setText(review.getAuthor());

            TextView contentText = (TextView) container.findViewById(R.id.txt_movie_review_content);
            contentText.setText(review.getContent());

            reviewListContainer.addView(container);
        }

    }

}


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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
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
 * A placeholder fragment holding the details of the user selected Movie.
 * This is added to the MainActivity when the user selects a Movie from the GridView.
 *
 */
public class DetailedViewActivityFragment extends Fragment {
    private static final String LOG_TAG = DetailedViewActivityFragment.class.getSimpleName();
    private Movie movie;
    protected Activity parent;

    private IntentFilter intentFilter = new IntentFilter();

    /**
     * BroadcastReceiver instance to handle updates from the IntentService
     */
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
                    if (videos == null) {
                        Log.v(LOG_TAG, "No video records retrieved for movie");
                        return;
                    }
                    movie.setTrailers(videos);
                    updateMovieTrailers();
                    break;

                case MovieFetchServiceContract.REPLY_FETCH_MOVIE_REVIEWS:
                    List<Review> reviews = intent.getParcelableArrayListExtra(MovieFetchServiceContract.EXTRA_MOVIE_REVIEWS);
                    if (reviews == null) {
                        Log.v(LOG_TAG, "No review records retrieved for movie");
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

        if(parent instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) parent).getSupportActionBar();
            if(actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(parent instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) parent).getSupportActionBar();
            if(actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(false);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detailed_view, menu);
        MenuItem sortOrder = menu.findItem(R.id.action_change_sort_order);
        if (sortOrder != null) {
            sortOrder.setVisible(false);
        }
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
        else if (item.getItemId() == android.R.id.home) {
            closeFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detailed_view, container, false);
        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    closeFragment();
                    return true;
                }

                return false;
            }
        });

        Bundle args = getArguments();
        movie = args.getParcelable("selected_movie");
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
     * Helper function to invoke the IntentService's fetch method
     * @param movieId The movie ID whose details are to be fetched
     */
    private void fetchMovieDetails(int movieId) {
        MovieDataFetchHelperService.startActionFetchMovie(parent, movieId);
    }

    /**
     * This method updates the Views in the Fragment with details from the 'movie' member
     * variable
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

        if(parent instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) parent).getSupportActionBar();
            if(actionBar != null) {
                actionBar.setTitle(movieName + " (" + (releaseDate.getYear() + 1900) + ")");
            }
        }

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
            if (moviePlot.isEmpty()) {
                plotText.setText(R.string.txt_movie_no_synopsis_info);
            } else {
                plotText.setText(moviePlot);
            }
        }

        TextView runtimeText = (TextView) parent.findViewById(R.id.txt_movie_duration);
        if (runtimeText != null) {
            runtimeText.setText(String.valueOf(movie.getDuration()) + " mins");
        }

        TextView relDateText = (TextView) parent.findViewById(R.id.txt_movie_date);
        if (relDateText != null) {
            relDateText.setText(new SimpleDateFormat("dd-MM-yyyy").format(releaseDate));
        }

        updateMovieTrailers();
        updateMovieReviews();

    }

    /**
     * This method updates the Trailers section in the Fragment.
     * If no trailers are available, a note is added to indicate this.
     *
     */
    private void updateMovieTrailers() {
        ViewGroup trailerListContainer = (ViewGroup) parent.findViewById(R.id.container_movie_trailers);
        if (trailerListContainer == null || movie.getTrailers() == null) {
            Log.v(LOG_TAG, "updateMovieTrailers: No trailer content to render");
            return;
        }

        int childCount = trailerListContainer.getChildCount();
        for (int i = childCount - 1; i > 1; --i) {
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

        List<Video> trailers = movie.getTrailers();
        TextView text = (TextView)trailerListContainer.findViewById(R.id.txt_movie_no_trailers_found);
        if (text != null) {
            if (trailers.isEmpty()) {
                text.setText(R.string.txt_movie_no_trailers_info);
                return;
            }
            else {
                text.setText("");
            }
        }

        for (Video trailer : trailers) {
            ViewGroup container = (ViewGroup) LayoutInflater.from(parent).inflate(
                    R.layout.list_item_movie_trailer, trailerListContainer, false);

            ImageView videoThumbnail = (ImageView) container.findViewById(R.id.thumbnail_preview);
            Picasso.with(parent).load(trailer.getThumbnail()).
                    placeholder(R.drawable.no_preview_available).into(videoThumbnail);

            TextView titleText = (TextView) container.findViewById(R.id.txt_trailer_title);
            titleText.setText(trailer.getName());

            trailerListContainer.addView(container);
            trailerListContainer.setOnClickListener(new ItemTextClickListener(trailer.getLink()));
        }

    }

    /**
     * This method updates the Reviews section in the Fragment.
     * If no reviews are available, a note is added to indicate this.
     *
     */
    private void updateMovieReviews() {
        ViewGroup reviewListContainer = (ViewGroup) parent.findViewById(R.id.container_movie_reviews);
        if (reviewListContainer == null || movie.getReviews() == null) {
            Log.v(LOG_TAG, "updateMovieReviews: No review content to render");
            return;
        }

        int childCount = reviewListContainer.getChildCount();
        for (int i = childCount - 1; i > 1; --i) {
            reviewListContainer.removeViewAt(i);
        }

        List<Review> reviews = movie.getReviews();
        TextView text = (TextView) reviewListContainer.findViewById(R.id.txt_movie_no_reviews_found);
        if (text != null) {
            if (reviews.isEmpty()) {
                text.setText(R.string.txt_movie_no_reviews_info);
                return;
            }
            else {
                text.setText("");
            }
        }

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

    /**
     * Removes this fragment from the Activity's hierarchy
     */
    private void closeFragment() {
        if(parent instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) parent).getSupportActionBar();
            if(actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle(R.string.app_name);
            }

        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .commit();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    /**
     * Adds a movie to the list of user favourites
     * @param db The writable SQLite database holding the favourites table
     * @param movie The movie object which needs to be added as a favourite
     * @return true if the INSERT was successful; false otherwise
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
     * Removes a movie from the favourite list
     * @param db The writable SQLite database holding the favourites table
     * @param movie The movie object which is no longer a favourite
     * @return true if movie was removed; false otherwise
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
     * Returns true if the passed-in parameter movie is available in the DB as a favourite
     * @param db The readable SQLite database to check for favourites
     * @param movie The movie object to verify
     * @return true if the movie is a favourite; false otherwise
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

}


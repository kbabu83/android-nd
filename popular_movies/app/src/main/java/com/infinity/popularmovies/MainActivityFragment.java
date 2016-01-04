package com.infinity.popularmovies;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.infinity.popularmovies.data.FavouritesOpenHelper;
import com.infinity.popularmovies.data.Movie;
import com.infinity.popularmovies.data.MovieDBContract;
import com.infinity.popularmovies.data.MovieFetchServiceContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();

    private Activity parent;
    private List<Movie> movieList = new ArrayList<>();
    private ImageViewAdapter<Movie> imageAdapter = null;
    private IntentFilter intentFilter = new IntentFilter();
    private SharedPreferences sharedPref = null;
    private int currentPage = 0;
    private boolean requestPending = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (MovieFetchServiceContract.REPLY_DISCOVER_MOVIES_UPDATE.equals(action)) {
                Log.v(LOG_TAG, "Discovery results available: " + String.valueOf(currentPage+1));
                requestPending = false;
                int page = intent.getIntExtra(MovieFetchServiceContract.EXTRA_PAGE_NUM, 1);
                if (page > currentPage) {
                    currentPage = page;
                    List<? extends Parcelable> movies = intent.getParcelableArrayListExtra(
                            MovieFetchServiceContract.EXTRA_MOVIE_LIST);
                    if (movies != null) {
                        for (Parcelable p : movies)
                            movieList.add((Movie) p);
                    }

                    imageAdapter.updateImageDataSet(movieList);
                    imageAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    /**
     *
     * @param <T>
     */
    public interface ListItemClickListener<T> {
        /**
         *
         * @param clickedItem
         */
        void onImageListItemClicked(T clickedItem);
    }

    /**
     *
     */
    public class RecyclerViewScrollListenerImpl extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            final int threshold = 2 * getResources().getInteger(R.integer.main_grid_span_count);
            GridLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
            if (layoutManager == null)
                return;

            int itemCount = layoutManager.getItemCount();
            //int firstVisible = layoutManager.findFirstVisibleItemPosition();
            int lastVisible = layoutManager.findLastVisibleItemPosition();
            //Log.v(LOG_TAG, "Visible positions: " + firstVisible + ", " + lastVisible + ", " + itemCount + " :: " + currentPage);

            if (lastVisible >= (itemCount - threshold) && !requestPending) {
                startMovieDiscovery(currentPage);
            }
        }
    }

    public MainActivityFragment() { }

    @Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parent = getActivity();
        setHasOptionsMenu(true);
        sharedPref = parent.getPreferences(Context.MODE_PRIVATE);
        String movieSortOrder = sharedPref.getString(getString(R.string.preferences_movie_sort_order), "");
        if (movieSortOrder.equals("")) {
            Log.v(LOG_TAG, "No preferences; creating defaults");
            sharedPref.edit().putString(getString(R.string.preferences_movie_sort_order),
                    MovieFetchServiceContract.SORT_SETTING_POPULARITY).apply();
        }

        int minVotes = sharedPref.getInt(getString(R.string.preferences_movie_discover_min_votes), -1);
        if (minVotes == -1) {
            Log.v(LOG_TAG, "No min. vote preferences; creating defaults");
            sharedPref.edit().putInt(getString(R.string.preferences_movie_discover_min_votes),
                    MovieFetchServiceContract.DEFAULT_MIN_VOTES_REQ).apply();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.list_view_movies);
        if (recyclerView == null) {
            Log.e(LOG_TAG, "No GridView instance available in Fragment");
            return rootView;
        }

        intentFilter.addAction(MovieFetchServiceContract.REPLY_DISCOVER_MOVIES_UPDATE);
        imageAdapter = new ImageViewAdapter<>(getActivity(), movieList);
        recyclerView.setAdapter(imageAdapter);
        recyclerView.addOnScrollListener(new RecyclerViewScrollListenerImpl());

        /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int mScrollPage = currentPage;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                final int threshold = 2 * getResources().getInteger(R.integer.main_grid_span_count);
                GridLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
                if (layoutManager == null)
                    return;

                int itemCount = layoutManager.getItemCount();
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                Log.v(LOG_TAG, "Visible positions: " + firstVisible + ", " + lastVisible + ", " + itemCount);

                if (lastVisible >= (itemCount - threshold) && mScrollPage <= currentPage) {
                    startMovieDiscovery(mScrollPage);
                    ++mScrollPage;
                }
            }
        });*/

        String sortSetting = sharedPref.getString(getString(R.string.preferences_movie_sort_order), "");
        if (sortSetting.equals(MovieFetchServiceContract.SORT_SETTING_FAVOURITE)) {
            List<Movie> movies = getFavouritesList();
            movieList.clear();
            movieList.addAll(movies);
            imageAdapter.updateImageDataSet(movieList);
            imageAdapter.notifyDataSetChanged();
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        String movieSortOrder = sharedPref.getString(getString(R.string.preferences_movie_sort_order), null);
        if (MovieFetchServiceContract.SORT_SETTING_FAVOURITE.equals(movieSortOrder))
            menu.findItem(R.id.action_favourite_sort).setChecked(true);
        else if (MovieFetchServiceContract.SORT_SETTING_RATING.equals(movieSortOrder))
            menu.findItem(R.id.action_rating_sort).setChecked(true);
        else
            menu.findItem(R.id.action_popularity_sort).setChecked(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Toast.makeText(getActivity(), "Settings screen yet to be implemented", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_popularity_sort:
            case R.id.action_rating_sort:
                if (!item.isChecked()) {
                    String movieSortOrder = (id == R.id.action_rating_sort) ?
                            MovieFetchServiceContract.SORT_SETTING_RATING :
                            MovieFetchServiceContract.SORT_SETTING_POPULARITY;
                    sharedPref.edit().putString(getString(R.string.preferences_movie_sort_order), movieSortOrder).apply();
                    item.setChecked(true);
                    currentPage = 0;
                    movieList.clear();
                    startMovieDiscovery(currentPage);
                }
                break;

            case R.id.action_favourite_sort:
                List<Movie> movies = getFavouritesList();
                movieList.clear();
                movieList.addAll(movies);
                imageAdapter.updateImageDataSet(movieList);
                imageAdapter.notifyDataSetChanged();

                sharedPref.edit().putString(getString(R.string.preferences_movie_sort_order),
                        MovieFetchServiceContract.SORT_SETTING_FAVOURITE).apply();
                item.setChecked(true);

                break;

            default:
                return false;
        }

        return true;
    }

    private List<Movie> getFavouritesList() {
        List<Movie> movies = new ArrayList<>();
        FavouritesOpenHelper dbHelper = new FavouritesOpenHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] favColumns = {MovieDBContract.FavouriteEntry.COLUMN_MOVIE_KEY};
        Cursor favCursor = db.query(MovieDBContract.FavouriteEntry.TABLE_NAME, favColumns, null, null, null, null, null);
        if (favCursor.moveToFirst()) {
            do {
                String[] columns = { MovieDBContract.MovieEntry._ID,
                                     MovieDBContract.MovieEntry.COLUMN_MOVIE_TITLE,
                                     MovieDBContract.MovieEntry.COLUMN_MOVIE_PLOT,
                                     MovieDBContract.MovieEntry.COLUMN_MOVIE_POSTER,
                                     MovieDBContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                                     MovieDBContract.MovieEntry.COLUMN_MOVIE_RATING,
                                     MovieDBContract.MovieEntry.COLUMN_MOVIE_VOTE_COUNT };

                String selection = MovieDBContract.MovieEntry._ID + " = ?";
                String[] selectionArgs = {String.valueOf(favCursor.getInt(0))};
                Cursor cursor = db.query(MovieDBContract.MovieEntry.TABLE_NAME, columns,
                        selection, selectionArgs, null, null, null);
                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String plot = cursor.getString(2);
                    String poster = cursor.getString(3);
                    String date = cursor.getString(4);

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    Date releaseDate = null;
                    try {
                        releaseDate = df.parse(date);
                    } catch (ParseException e) {
                        Log.e(LOG_TAG, "Movie " + title + ", " + date + " : " + e.getMessage());
                    }

                    double rating = cursor.getDouble(5);
                    int voteCount = cursor.getInt(6);

                    Movie movie = new Movie(id, title, "en", poster, plot, rating, voteCount, 0, releaseDate, null, null);
                    movies.add(movie);

                    cursor.close();
                }

            } while (favCursor.moveToNext());
        }

        favCursor.close();
        db.close();
        return movies;

    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
                receiver, intentFilter);

        String sortOrder = sharedPref.getString(getString(R.string.preferences_movie_sort_order), "");
        if (!sortOrder.equals(MovieFetchServiceContract.SORT_SETTING_FAVOURITE))
            startMovieDiscovery(currentPage);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.app_name);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (movieList != null)
            movieList.clear();
        currentPage = 0;
        LocalBroadcastManager.getInstance(this.getActivity().getApplicationContext()).unregisterReceiver(receiver);
    }

    /**
     * Helper function to trigger Movie discovery request to IntentService
     * @param currentpage The current page shown on screen if content is already shown; else 0.
     */
    private void startMovieDiscovery(int currentpage) {
        int next = currentpage + 1;
        Log.v(LOG_TAG, "Start discovery: " + next);
        String sortOrder = sharedPref.getString(getString(R.string.preferences_movie_sort_order), null);
        int minVotes = sharedPref.getInt(getString(R.string.preferences_movie_discover_min_votes), 0);
        MovieDataFetchHelperService.startActionDiscover(getActivity(), next, sortOrder, minVotes);
        requestPending = true;
    }

}


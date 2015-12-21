package com.infinity.popularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import com.infinity.popularmovies.data.MovieFetchServiceContract;

import java.util.ArrayList;
import java.util.List;

public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();

    private List<Movie> movieList = new ArrayList<>();
    private RecyclerView.Adapter imageAdapter = null;
    private IntentFilter intentFilter = new IntentFilter();
    private SharedPreferences sharedPref = null;
    private int currentPage = 0;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (MovieFetchServiceContract.REPLY_DISCOVER_MOVIES_UPDATE.equals(action)) {
                Log.v(LOG_TAG, "Discovery results available");
                int page = intent.getIntExtra(MovieFetchServiceContract.EXTRA_PAGE_NUM, 1);
                if (page > currentPage) {
                    currentPage = page;
                    List<? extends Parcelable> movies = intent.getParcelableArrayListExtra(
                            MovieFetchServiceContract.EXTRA_MOVIE_LIST);
                    if (movies != null) {
                        for (Parcelable p : movies)
                            movieList.add((Movie) p);
                    }

                    ((ImageViewAdapter) imageAdapter).updateImageDataSet(movieList);
                    imageAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    public MainActivityFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
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
        imageAdapter = new ImageViewAdapter(getActivity(), movieList);
        recyclerView.setAdapter(imageAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                //int firstVisible = layoutManager.findFirstVisibleItemPosition();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                //Log.v(LOG_TAG, "Visible positions: " + firstVisible + ", " + lastVisible + ", " + itemCount);

                if (lastVisible >= (itemCount - threshold) && mScrollPage <= currentPage) {
                    startMovieDiscovery(mScrollPage);
                    ++mScrollPage;
                }
            }
        });

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
                    startMovieDiscovery(0);
                }
                break;

            case R.id.action_favourite_sort:
                Toast.makeText(getContext(), "Feature not yet available", Toast.LENGTH_SHORT).show();
                break;

            default:
                return false;
        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
                receiver, intentFilter);

        startMovieDiscovery(currentPage);
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
        String sortOrder = sharedPref.getString(getString(R.string.preferences_movie_sort_order), null);
        int minVotes = sharedPref.getInt(getString(R.string.preferences_movie_discover_min_votes), 0);
        MovieDataFetchHelperService.startActionDiscover(getActivity(), ++currentpage, sortOrder, minVotes);
    }

}


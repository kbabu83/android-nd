package com.infinity.popularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();

    private String movieSortOrder = MovieDataFetchService.ACTION_PARAM_FETCH_MOVIES_SORT_POPULARITY;
    private List<Movie> movieList = new ArrayList<>();
    private RecyclerView.Adapter imageAdapter = null;
    private IntentFilter intentFilter = new IntentFilter();
    private int currentPage = 0;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (MovieDataFetchService.REPLY_FETCH_MOVIE_LIST_UPDATE.equals(action)) {
                List<? extends Parcelable> movies = intent.getParcelableArrayListExtra("movie_list");
                if (movies != null) {
                    for (Parcelable p : movies)
                        movieList.add((Movie)p);
                }

                ((ImageViewAdapter)imageAdapter).updateImageDataSet(movieList);
                imageAdapter.notifyDataSetChanged();
            }
        }
    };

    public MainActivityFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.list_view_movies);
        if (recyclerView == null) {
            Log.v(LOG_TAG, "No GridView instance available in Fragment");
            return rootView;
        }

        intentFilter.addAction(MovieDataFetchService.REPLY_FETCH_MOVIE_LIST_UPDATE);
        imageAdapter = new ImageViewAdapter(getActivity(), movieList);
        recyclerView.setAdapter(imageAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                final int threshold = 2 * getResources().getInteger(R.integer.main_grid_span_count);
                GridLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
                if (layoutManager == null)
                    return;

                int itemCount = layoutManager.getItemCount();
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                Log.v(LOG_TAG, "Visible positions: " + firstVisible + ", " + lastVisible + ", " + itemCount);

                if (lastVisible >= (itemCount - threshold)) {
                    MovieDataFetchService.sendActionRequest(getActivity(), MovieDataFetchService.ACTION_FETCH_MOVIE_LIST,
                            movieSortOrder, ++currentPage);
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
        MenuItem sortSetting = menu.findItem(R.id.action_sort_order);
        if (MovieDataFetchService.ACTION_PARAM_FETCH_MOVIES_SORT_POPULARITY.equals(movieSortOrder))
            sortSetting.setTitle(getString(R.string.action_sort_by_rating));
        else
            sortSetting.setTitle(getString(R.string.action_sort_by_popularity));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getActivity(), "Settings screen yet to be implemented", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (id == R.id.action_sort_order) {
            if (MovieDataFetchService.ACTION_PARAM_FETCH_MOVIES_SORT_POPULARITY.equals(movieSortOrder))
                movieSortOrder = MovieDataFetchService.ACTION_PARAM_FETCH_MOVIES_SORT_USER_RATING;
            else
                movieSortOrder = MovieDataFetchService.ACTION_PARAM_FETCH_MOVIES_SORT_POPULARITY;
            currentPage = 1;
            movieList.clear();


            MovieDataFetchService.sendActionRequest(getActivity(), MovieDataFetchService.ACTION_FETCH_MOVIE_LIST,
                    movieSortOrder, currentPage);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
                receiver, intentFilter);

        MovieDataFetchService.sendActionRequest(this.getActivity(), MovieDataFetchService.ACTION_FETCH_MOVIE_LIST,
                movieSortOrder, ++currentPage);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (movieList != null)
            movieList.clear();
        currentPage = 0;
        LocalBroadcastManager.getInstance(this.getActivity().getApplicationContext()).unregisterReceiver(receiver);
    }

}


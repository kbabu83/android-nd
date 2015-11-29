package com.infinity.popularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

public class MainActivityFragment extends Fragment {

    private final String logTag = getClass().getSimpleName();

    private List<Movie> movieList = null;
    private List<String> movieThumbnailPaths = new ArrayList<>();
    private ImageAdapter imageAdapter = null;
    private IntentFilter intentFilter = new IntentFilter();

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (MovieDataFetchService.REPLY_FETCH_MOVIE_LIST_UPDATE.equals(action)) {
                movieThumbnailPaths.clear();

                movieList = intent.getParcelableArrayListExtra("movie_list");
                for (Movie movie : movieList) {
                    String imageURL = movie.getPosterThumbnail();
                    /*Log.v(logTag, movie.getTitle() + " " +
                            imageURL + " " + movie.getReleaseDate().toString());*/
                    movieThumbnailPaths.add(imageURL);
                }

                imageAdapter.updateImageDataSet(movieThumbnailPaths);
                imageAdapter.notifyDataSetChanged();
            }
        }
    };

    public MainActivityFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.grid_view);
        if (gridView == null) {
            Log.v(logTag, "No GridView instance available in Fragment");
            return rootView;
        }
        intentFilter.addAction(MovieDataFetchService.REPLY_FETCH_MOVIE_LIST_UPDATE);
        imageAdapter = new ImageAdapter(getActivity());
        gridView.setAdapter(imageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), DetailedViewActivity.class);
                Movie movie = movieList.get(position);
                intent.putExtra("selected_movie", movie);
                startActivity(intent);
            }
        });


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(receiver,
                intentFilter);

        MainActivity parent = (MainActivity)getActivity();
        MovieDataFetchService.sendActionRequest(this.getActivity(), MovieDataFetchService.ACTION_FETCH_MOVIE_LIST,
                parent.getMovieSortOrder());
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity().getApplicationContext()).unregisterReceiver(receiver);
    }

}

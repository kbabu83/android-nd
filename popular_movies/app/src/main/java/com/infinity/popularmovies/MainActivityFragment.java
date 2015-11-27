package com.infinity.popularmovies;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private final String logTag = getClass().getSimpleName();

    public MainActivityFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.v(logTag, "onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.grid_view);
        if (gridView == null)
            Log.v(logTag, "No GridView instance available in Fragment");
        else
            Log.v(logTag, "GridView with ID: " + gridView.getId());

        return rootView;
    }

    @Override
    public void onDestroyView() {
        Log.v(logTag, "onDestroyView()");
        super.onDestroyView();
    }
}

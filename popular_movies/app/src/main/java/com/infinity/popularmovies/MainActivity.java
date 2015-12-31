package com.infinity.popularmovies;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.infinity.popularmovies.data.Movie;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.ListItemClickListener<Movie> {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onImageListItemClicked(Movie selection) {
        if (selection == null) {
            return;
        }

        Log.v(LOG_TAG, "Movie: " + selection.getTitle());
        DetailedViewActivityFragment detailsFragment = new DetailedViewActivityFragment();
        Bundle args = new Bundle();
        args.putParcelable("selected_movie", selection);
        detailsFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_details_view, detailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }
}


package com.infinity.popularmovies;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;

import com.infinity.popularmovies.data.Movie;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.ListItemClickListener<Movie> {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.content.res.Configuration configuration = this.getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
        int smallestScreenWidthDp = configuration.smallestScreenWidthDp; //The smallest screen size an application will see in normal operation, corresponding to smallest screen width resource qualifier.
        int screenHeightDp = configuration.screenHeightDp;

        Log.v(LOG_TAG, "Details: " + screenWidthDp + ", " + screenHeightDp + "; " + smallestScreenWidthDp);

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
        args.putInt("gravity", Gravity.TOP | Gravity.RIGHT);
        detailsFragment.setArguments(args);

        //FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_details_view, detailsFragment)
                .addToBackStack(null)
                .commit();

    }
}


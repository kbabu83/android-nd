package com.infinity.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private String movieSortOrder = MovieDataFetchService.ACTION_PARAM_FETCH_MOVIES_SORT_POPULARITY;

    public String getMovieSortOrder() {
        return movieSortOrder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);
        MenuItem sortSetting = menu.findItem(R.id.action_sort_order);
        if (MovieDataFetchService.ACTION_PARAM_FETCH_MOVIES_SORT_POPULARITY.equals(movieSortOrder))
            sortSetting.setTitle(getString(R.string.action_sort_by_rating));
        else
            sortSetting.setTitle(getString(R.string.action_sort_by_popularity));

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings screen yet to be implemented", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (id == R.id.action_sort_order) {
            if (MovieDataFetchService.ACTION_PARAM_FETCH_MOVIES_SORT_POPULARITY.equals(movieSortOrder))
                movieSortOrder = MovieDataFetchService.ACTION_PARAM_FETCH_MOVIES_SORT_USER_RATING;
            else
                movieSortOrder = MovieDataFetchService.ACTION_PARAM_FETCH_MOVIES_SORT_POPULARITY;

            MovieDataFetchService.sendActionRequest(this, MovieDataFetchService.ACTION_FETCH_MOVIE_LIST,
                    movieSortOrder);

        }

        return super.onOptionsItemSelected(item);
    }

}


package com.infinity.popularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String logTag = getClass().getSimpleName();
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (MovieDataFetchService.REPLY_FETCH_MOVIE_LIST_UPDATE.equals(action)) {
                Configuration tmdbConfig = intent.getParcelableExtra("movie_data_config");
                String baseUrl = tmdbConfig.getImageBaseUrl();
                movieThumbnailPaths.clear();

                movieList = intent.getParcelableArrayListExtra("movie_list");
                for (Movie movie : movieList) {
                    String imageURL = createImageURL(baseUrl, movie.getPosterThumbnail());
                    Log.v(logTag, movie.getTitle() + " " +
                             imageURL + " " + movie.getReleaseDate().toString());
                    movieThumbnailPaths.add(imageURL);
                }

                imageAdapter.updateImageDataSet(movieThumbnailPaths);
                imageAdapter.notifyDataSetChanged();
            }
        }
    };

    private List<Movie> movieList = null;
    private List<String> movieThumbnailPaths = new ArrayList<>();

    private IntentFilter intentFilter = new IntentFilter();
    private ImageAdapter imageAdapter = new ImageAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(logTag, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.content.res.Configuration configuration = this.getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
        int smallestScreenWidthDp = configuration.smallestScreenWidthDp; //The smallest screen size an application will see in normal operation, corresponding to smallest screen width resource qualifier.
        int screenHeightDp = configuration.screenHeightDp;

        Log.v(logTag, "Screen config: " + screenWidthDp + " " + screenHeightDp + " " + smallestScreenWidthDp);

        intentFilter.addAction(MovieDataFetchService.REPLY_FETCH_MOVIE_LIST_UPDATE);

        GridView gridView = (GridView)findViewById(R.id.grid_view);
        if (gridView == null) {
            Log.v(getClass().getSimpleName(), "No GridView to populate");
            return;
        }

        imageAdapter.notifyDataSetChanged();
        gridView.setAdapter(imageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), DetailedViewActivity.class);
                Movie movie = movieList.get(position);
                intent.putExtra("movie_name", movie.getTitle());
                intent.putExtra("movie_poster", createImageURL("image.tmdb.org/t/p/", movie.getPosterThumbnail()));
                intent.putExtra("movie_plot", movie.getSynopsis());
                intent.putExtra("movie_rating", movie.getRating());
                intent.putExtra("movie_release", new SimpleDateFormat("dd-MM-yyyy").format(movie.getReleaseDate()));
                startActivity(intent);
            }
        });

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
    }

    @Override
    protected void onStart() {
        Log.v(logTag, "onStart()");
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver,
                new IntentFilter(MovieDataFetchService.REPLY_FETCH_MOVIE_LIST_UPDATE));

        MovieDataFetchService.sendActionRequest(this, MovieDataFetchService.ACTION_FETCH_MOVIE_LIST);
    }

    @Override
    protected void onStop() {
        Log.v(logTag, "onStop()");
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String createImageURL(@NonNull String basepath, @NonNull String imgpath) {
        /*Uri.Builder builder = new Uri.Builder();
        return builder.scheme("http").authority(basepath).appendPath(imgpath).build().toString();*/
        return "http://" + basepath + "/w500" + imgpath;
    }

}


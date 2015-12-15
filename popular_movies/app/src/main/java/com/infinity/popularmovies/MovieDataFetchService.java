package com.infinity.popularmovies;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MovieDataFetchService extends Service {
    private static final String TMDB_BASE_URL = "api.themoviedb.org";
    private static final String TMDB_API_VERSION = "3";
    private static final String TMDB_DISCOVERY_PATH = "discover";
    private static final String TMDB_DISCOVER_TYPE_MOVIE = "movie";
    private static final String TMDB_DISCOVER_TYPE_TV = "tv";
    private static final String TMDB_CONFIGURATION_PATH="configuration";

    public static final String ACTION_FETCH_MOVIE_LIST = "com.infinity.popularmovies.action.FETCH_MOVIE_LIST";
    public static final String ACTION_FETCH_MOVIE_DETAILS = "com.infinity.popularmovies.action.FETCH_MOVIE_DETAILS";

    public static final String REPLY_FETCH_MOVIE_LIST_UPDATE = "com.infinity.popularmovies.action.REPLY_FETCH_MOVIE_LIST_UPDATE";

    public static final String ACTION_PARAM_FETCH_MOVIES_PAGE_NUM = "com.infinity.popularmovies.extra.PageNum";
    public static final String ACTION_PARAM_FETCH_MOVIES_SORT_ORDER = "com.infinity.popularmovies.extra.SortOrder";
    public static final String ACTION_PARAM_FETCH_MOVIES_SORT_POPULARITY = "com.infinity.popularmovies.arg.Popularity";
    public static final String ACTION_PARAM_FETCH_MOVIES_SORT_USER_RATING = "com.infinity.popularmovies.arg.UserRating";

    private Configuration tmdbConfig;
    private RequestQueue cmdRequestQueue = null;
    private String logTag = getClass().getSimpleName();

    public MovieDataFetchService() { }

    public Configuration getTmdbConfig() {
        return tmdbConfig;
    }

    @Override
    public void onCreate() {
        Log.v(logTag, "onCreate()");
        super.onCreate();
        if (cmdRequestQueue == null) {
            cmdRequestQueue = Volley.newRequestQueue(this);
        }

        tmdbConfig = new Configuration();

        createTMDBConfig();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            String sort_order =
            (ACTION_PARAM_FETCH_MOVIES_SORT_POPULARITY.equals(intent.getStringExtra(ACTION_PARAM_FETCH_MOVIES_SORT_ORDER))) ?
                    "popularity.desc" : "vote_average.desc";
            int page = intent.getIntExtra(ACTION_PARAM_FETCH_MOVIES_PAGE_NUM, 1);

            if (ACTION_FETCH_MOVIE_LIST.equals(action)) {
                handleActionFetchMovies(createMovieDiscoveryURL(sort_order, page));
            }
            else if (ACTION_FETCH_MOVIE_DETAILS.equals(action)) {

            }
            else {
                Log.v(logTag, action+ " has no defined action");
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(logTag, "onBind()");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void sendActionRequest(Context context, @NonNull String action, @Nullable String sortOrderArg, int pageNum) {
        String sortOrder;
        if (sortOrderArg == null || sortOrderArg.isEmpty())
            sortOrder = ACTION_PARAM_FETCH_MOVIES_SORT_POPULARITY;
        else
            sortOrder = sortOrderArg;

        Intent intent = new Intent(context, MovieDataFetchService.class);
        intent.setAction(action);
        intent.putExtra(ACTION_PARAM_FETCH_MOVIES_SORT_ORDER, sortOrder);
        intent.putExtra(ACTION_PARAM_FETCH_MOVIES_PAGE_NUM, pageNum);
        context.startService(intent);
    }

    private String createConfigurationURL() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority(TMDB_BASE_URL)
                .path(TMDB_API_VERSION)
                .appendPath(TMDB_CONFIGURATION_PATH)
                .appendQueryParameter("api_key", APIKey.TMDB_API_KEY)
                .build();

        String configUrl = builder.build().toString();
        Log.v(this.getClass().getSimpleName(), "Config URL: " + configUrl);
        return configUrl;
    }

    private String createMovieDiscoveryURL(String sort_order, int page) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority(TMDB_BASE_URL)
                .path(TMDB_API_VERSION)
                .appendPath(TMDB_DISCOVERY_PATH)
                .appendPath(TMDB_DISCOVER_TYPE_MOVIE)
                .appendQueryParameter("api_key", APIKey.TMDB_API_KEY)
                .appendQueryParameter("page", String.valueOf(page))
                .appendQueryParameter("sort_by", sort_order);

        String discUrl = builder.build().toString();
        Log.v(this.getClass().getSimpleName(), "Discovery URL: " + discUrl);
        return discUrl;
    }

    private void handleActionFetchMovies(String discoveryUrl) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest
        (
            Request.Method.GET, discoveryUrl, "",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<Movie> movies = retrieveMovieDetails(response);

                            Intent intent = new Intent(MovieDataFetchService.this, MainActivity.class);
                            intent.setAction(REPLY_FETCH_MOVIE_LIST_UPDATE);
                            intent.putExtra("movie_data_config", getTmdbConfig());
                            intent.putParcelableArrayListExtra("movie_list", (ArrayList<? extends Parcelable>) movies);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                        } catch (JSONException e) {
                            Log.e(logTag, e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.getMessage() != null)
                            Log.v("Service::Error", error.getMessage());

                        Toast.makeText(MovieDataFetchService.this, "Couldn't connect to network!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        cmdRequestQueue.add(jsonRequest);
    }

    private void createTMDBConfig() {
        JsonObjectRequest jsonRequest = new JsonObjectRequest
        (
            Request.Method.GET, createConfigurationURL(), "",
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response == null) {
                        Log.e("MovieDataFetchService", "Null response to config request");
                        return;
                    }

                    try {
                        JSONObject imageProps = response.getJSONObject("images");
                        String imageUrl = imageProps.getString("base_url");
                        String imageUrlSecure = imageProps.getString("secure_base_url");
                        JSONArray availableSizes = (JSONArray)imageProps.get("poster_sizes");
                        List<String> posterSizes = new ArrayList<>();
                        for (int i = 0; i < availableSizes.length(); ++i) {
                            posterSizes.add(availableSizes.getString(i));
                        }

                        tmdbConfig = new Configuration(imageUrl, imageUrlSecure, posterSizes);
                    } catch (JSONException e) {
                        Log.e("MovieDataFetchService", e.getMessage());
                    }
                    //Log.v("Service::Response", response.toString());
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("Service::Error", error.getMessage());
                }
            }
        );

        cmdRequestQueue.add(jsonRequest);
    }

    private List<Movie> retrieveMovieDetails(@NonNull JSONObject movieData) throws JSONException {
        JSONArray results = movieData.getJSONArray("results");
        if (results == null) {
            Log.e(logTag, "No data available; empty list");
            return new ArrayList<>();
        }

        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < results.length(); ++i) {
            JSONObject result = (JSONObject)results.get(i);
            int id = result.getInt("id");
            String title = result.getString("original_title");
            String language = result.getString("original_language");

            String thumbnails = result.getString("poster_path");
            String thumbnailsLink =  getTmdbConfig().getImageBaseUrl() + Configuration.PREFERRED_IMAGE_SIZE + thumbnails;

            String plot = result.getString("overview");
            double rating = result.getDouble("vote_average");
            String date = result.getString("release_date");

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date releaseDate = null;
            try {
                releaseDate = df.parse(date);
            } catch (ParseException e) {
                Log.e(logTag, e.getMessage());
            }

            Movie movie = new Movie(id, title, language, thumbnailsLink, plot, rating, releaseDate);
            movies.add(movie);

        }

        return movies;
    }

}


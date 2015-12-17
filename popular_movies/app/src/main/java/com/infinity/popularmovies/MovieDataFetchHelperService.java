package com.infinity.popularmovies;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.infinity.popularmovies.data.MovieFetchServiceContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MovieDataFetchHelperService extends IntentService {
    //Logging aid; log tag for all logs from this class
    private static final String LOG_TAG = MovieDataFetchHelperService.class.getSimpleName();

    //TMDB Discovery/Search API constants
    private static final String TMDB_BASE_URL = "api.themoviedb.org";
    private static final String TMDB_API_VERSION = "3";
    private static final String TMDB_CONFIGURATION_PATH="configuration";
    private static final String TMDB_DISCOVERY_PATH = "discover";
    private static final String TMDB_DISCOVER_TYPE_MOVIE = "movie";
    private static final String TMDB_QUERY_PARAM_API_KEY = "api_key";
    private static final String TMDB_QUERY_PARAM_PAGE_NUM = "page";
    private static final String TMDB_QUERY_PARAM_SORT_ORDER = "sort_by";

    //TMDB Discovery - Sort options
    private static final String TMDB_QUERY_SORT_VOTE_DESC = "vote_average.desc";
    private static final String TMDB_QUERY_SORT_POPULARITY_DESC = "popularity.desc";





    // TODO: Rename parameters

    private Configuration tmdbConfig;
    private RequestQueue requestQueue = null;

    public MovieDataFetchHelperService() {
        super("MovieDataFetchHelperService");
    }

    public Configuration getTmdbConfig() {
        return tmdbConfig;
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDiscover(Context context, int page, String sortBy) {
        String sortSetting = "";
        switch (sortBy) {
            case MovieFetchServiceContract.SORT_SETTING_POPULARITY:
                sortSetting = TMDB_QUERY_SORT_POPULARITY_DESC;
                break;

            case MovieFetchServiceContract.SORT_SETTING_RATING:
                sortSetting = TMDB_QUERY_SORT_VOTE_DESC;
                break;

            case MovieFetchServiceContract.SORT_SETTING_FAVOURITE:
            default:
                break;
        }

        Intent intent = new Intent(context, MovieDataFetchHelperService.class);
        intent.setAction(MovieFetchServiceContract.ACTION_DISCOVER_MOVIES);
        intent.putExtra(MovieFetchServiceContract.EXTRA_PAGE_NUM, page);
        intent.putExtra(MovieFetchServiceContract.EXTRA_SORT_BY, sortSetting);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     * @param context
     * @param movieId
     */
    public static void startActionFetchMovie(Context context, int movieId) {
        Intent intent = new Intent(context, MovieDataFetchHelperService.class);
        intent.setAction(MovieFetchServiceContract.ACTION_FETCH_MOVIE_DETAILS);
        intent.putExtra(MovieFetchServiceContract.EXTRA_MOVIE_ID, movieId);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(this);

        tmdbConfig = new Configuration();
        createTMDBConfig();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (MovieFetchServiceContract.ACTION_DISCOVER_MOVIES.equals(action)) {
                final int page = intent.getIntExtra(MovieFetchServiceContract.EXTRA_PAGE_NUM, 1);
                final String sortBy = intent.getStringExtra(MovieFetchServiceContract.EXTRA_SORT_BY);
                handleActionDiscover(page, sortBy);
            } else if (MovieFetchServiceContract.ACTION_FETCH_MOVIE_DETAILS.equals(action)) {
                final int movie = intent.getIntExtra(MovieFetchServiceContract.EXTRA_MOVIE_ID, 0);
                handleActionFetchMovie(movie);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     * @param page
     * @param sortBy
     */
    private void handleActionDiscover(int page, String sortBy) {
        String discoveryUrl = buildMovieDiscoveryURL(page, sortBy, APIKey.TMDB_API_KEY);
        Log.v(LOG_TAG, "Discovery URL: " + discoveryUrl);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                discoveryUrl,
                "",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Map.Entry<Integer, List<Movie>> pageContent = getMoviesList(response);
                            int pageNum = pageContent.getKey();
                            List<Movie> movies = pageContent.getValue();
                            Intent intent = new Intent(MovieDataFetchHelperService.this, MainActivity.class);
                            intent.setAction(MovieFetchServiceContract.REPLY_DISCOVER_MOVIES_UPDATE);
                            intent.putExtra(MovieFetchServiceContract.EXTRA_PAGE_NUM, pageNum);
                            intent.putExtra(MovieFetchServiceContract.EXTRA_MOVIE_CONFIG, getTmdbConfig());
                            intent.putParcelableArrayListExtra(MovieFetchServiceContract.EXTRA_MOVIE_LIST,
                                    (ArrayList<? extends Parcelable>) movies);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                        }
                        catch (JSONException e) {
                            Log.e(LOG_TAG, "JSON parse in getMovies failed: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.getMessage() != null)
                            Log.e(LOG_TAG, "Discovery : " + error.getMessage());
                    }
                }

        );

        requestQueue.add(jsonRequest);

    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchMovie(int movieId) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     */
    private void createTMDBConfig() {
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                buildTMDBConfigURL(APIKey.TMDB_API_KEY),
                "",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response == null) {
                            Log.e(LOG_TAG, "Null response to config request");
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
                            Log.e(LOG_TAG, e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.getMessage() != null)
                            Log.v(LOG_TAG, "Get config error : " + error.getMessage());
                    }
                }
            );

        requestQueue.add(jsonRequest);
    }

    /**
     *
     * @param api_key
     * @return
     */
    private String buildTMDBConfigURL(@NonNull String api_key) {
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme("http").authority(TMDB_BASE_URL).path(TMDB_API_VERSION)
                         .appendPath(TMDB_CONFIGURATION_PATH)
                         .appendQueryParameter(TMDB_QUERY_PARAM_API_KEY, api_key)
                         .build();

        return uri.toString();
    }

    /**
     * Build the URL to invoke the TMDB movie discovery API
     * @param page
     * @param sortBy
     * @param api_key
     * @return url
     */
    private String buildMovieDiscoveryURL(int page, @NonNull String sortBy, @NonNull String api_key) {
        Uri.Builder builder = new Uri.Builder();
        builder = builder.scheme("http").authority(TMDB_BASE_URL).path(TMDB_API_VERSION)
                         .appendPath(TMDB_DISCOVERY_PATH)
                         .appendPath(TMDB_DISCOVER_TYPE_MOVIE)
                         .appendQueryParameter(TMDB_QUERY_PARAM_API_KEY, api_key)
                         .appendQueryParameter(TMDB_QUERY_PARAM_SORT_ORDER, sortBy)
                         .appendQueryParameter(TMDB_QUERY_PARAM_PAGE_NUM, String.valueOf(page));

        if (sortBy.equals(TMDB_QUERY_SORT_VOTE_DESC))
            builder.appendQueryParameter("vote_count.gte", String.valueOf(50));

        Uri uri = builder.build();
        return uri.toString();
    }

    /**
     *
     * @param movieData
     * @return
     * @throws JSONException
     */
    private Map.Entry<Integer, List<Movie>> getMoviesList(@NonNull JSONObject movieData) throws JSONException {
        int page = movieData.getInt("page");
        JSONArray results = movieData.getJSONArray("results");
        if (results == null) {
            Log.e(LOG_TAG, "No data available; empty list");
            return new HashMap.SimpleEntry<Integer, List<Movie>>(page, new ArrayList<Movie>());
        }

        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < results.length(); ++i) {
            JSONObject result = (JSONObject)results.get(i);
            int id = result.getInt("id");
            String title = result.getString("original_title");
            String language = result.getString("original_language");

            String thumbnails =  getTmdbConfig().getImageBaseUrl() +
                    Configuration.PREFERRED_IMAGE_SIZE +
                    result.getString("poster_path");

            String plot = result.getString("overview");
            double rating = result.getDouble("vote_average");
            int vote_count = result.getInt("vote_count");
            String date = result.getString("release_date");

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date releaseDate = null;
            try {
                releaseDate = df.parse(date);
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Movie " + title + ", " + date + " : " + e.getMessage());
            }

            //trailers and reviews require further API calls; these will be retrieved if the user
            //wishes to see more details in the Details Activity
            Movie movie = new Movie(id, page, title, language, thumbnails, plot, rating,
                    vote_count, releaseDate, null, null);
            movies.add(movie);

        }

        return new HashMap.SimpleEntry<>(page, movies);
    }
}


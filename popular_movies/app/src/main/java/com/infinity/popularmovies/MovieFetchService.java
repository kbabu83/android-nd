package com.infinity.popularmovies;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MovieFetchService extends IntentService {
    private static final String TMDB_BASE_URL = "api.themoviedb.org";
    private static final String TMDB_API_VERSION = "3";
    private static final String TMDB_DISCOVERY_PATH = "discover";
    private static final String TMDB_DISCOVER_TYPE_MOVIE = "movie";
    private static final String TMDB_DISCOVER_TYPE_TV = "tv";
    private static final String TMDB_CONFIGURATION_PATH="configuration";

    private static final String ACTION_FETCH_MOVIE_LIST = "com.infinity.popularmovies.action.FETCH_MOVIE_LIST";
    private static final String ACTION_FETCH_MOVIE_DETAILS = "com.infinity.popularmovies.action.FETCH_MOVIE_DETAILS";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.infinity.popularmovies.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.infinity.popularmovies.extra.PARAM2";

    private Configuration tmdbConfig = null;
    private RequestQueue requestQueue = null;

    public MovieFetchService() {
        super("MovieFetchService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("MovieFetchService", "onDestroy()");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionMovieFetch(Context context, String param1, String param2) {
        Intent intent = new Intent(context, MovieFetchService.class);
        intent.setAction(ACTION_FETCH_MOVIE_LIST);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionDetailsFetch(Context context, String param1, String param2) {
        Intent intent = new Intent(context, MovieFetchService.class);
        intent.setAction(ACTION_FETCH_MOVIE_DETAILS);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public void createConfiguration() {
        JsonObjectRequest jsonRequest = new JsonObjectRequest
        (
            Request.Method.GET, createConfigurationURL(), "",
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response == null) {
                        Log.e("MovieFetchService", "Null response to config request");
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
                        Log.e("MovieFetchService", e.getMessage());
                    }
                    Log.v("Service::Response", response.toString());
                }
            },
            new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("Service::Error", error.getMessage());
                }
            }
        );

        requestQueue.add(jsonRequest);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_MOVIE_LIST.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFetchMovies(param1, param2);
            } else if (ACTION_FETCH_MOVIE_DETAILS.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFetchDetails(param1, param2);
            }
        }
    }

    private String createConfigurationURL(String... params) {
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


    private String createDiscoveryURL(String... params) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority(TMDB_BASE_URL)
                .path(TMDB_API_VERSION)
                .appendPath(TMDB_DISCOVERY_PATH)
                .appendPath(TMDB_DISCOVER_TYPE_MOVIE)
/*              .appendQueryParameter("primary_release_date.gte", "2015-10-01")
                .appendQueryParameter("primary_release_date.lte", "2015-10-22")*/
                .appendQueryParameter("api_key", APIKey.TMDB_API_KEY)
                .appendQueryParameter("sort_by", "popularity.desc");

        String url = builder.build().toString();
        Log.v(this.getClass().getSimpleName(), "got URL: " + url);
        return url;

    }


    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchMovies(String param1, String param2) {
        if (tmdbConfig == null || !tmdbConfig.ready()) {
            createConfiguration();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest
        (
            Request.Method.GET, createDiscoveryURL(), "",
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v("Service::Response", response.toString());
                }
            },
            new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("Service::Error", error.getMessage());
                }
            }
        );

        requestQueue.add(jsonRequest);

    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchDetails(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

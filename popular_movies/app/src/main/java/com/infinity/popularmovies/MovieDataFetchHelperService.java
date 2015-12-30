package com.infinity.popularmovies;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.infinity.popularmovies.data.Movie;
import com.infinity.popularmovies.data.MovieFetchServiceContract;
import com.infinity.popularmovies.data.Review;
import com.infinity.popularmovies.data.Video;
import com.infinity.popularmovies.netapi.APIKey;
import com.infinity.popularmovies.netapi.ConfigResponse;
import com.infinity.popularmovies.netapi.MovieResponse;
import com.infinity.popularmovies.netapi.ReviewResponse;
import com.infinity.popularmovies.netapi.TmdbResponse;
import com.infinity.popularmovies.netapi.TmdbService;
import com.infinity.popularmovies.netapi.VideoResponse;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Call;
import retrofit.Retrofit;
import retrofit.GsonConverterFactory;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class MovieDataFetchHelperService extends IntentService {
    //Logging aid; log tag for all logs from this class
    private static final String LOG_TAG = MovieDataFetchHelperService.class.getSimpleName();

    //TMDB Discovery/Search API constants
    private static final String TMDB_BASE_URL = "http://api.themoviedb.org";
    private static final int TMDB_API_VERSION = 3;

    //TMDB Discovery - Sort options
    private static final String TMDB_QUERY_SORT_VOTE_DESC = "vote_average.desc";
    private static final String TMDB_QUERY_SORT_POPULARITY_DESC = "popularity.desc";

    private static final String YOUTUBE_BASE_URL = "www.youtube.com";
    private static final String YOUTUBE_IMG_BASE_URL = "img.youtube.com";
    private static final String YOUTUBE_VIDEO_PATH = "watch";
    private static final String YOUTUBE_VIDEO_QUERY = "v";


    private Configuration tmdbConfig;
    private TmdbService service;

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
    public static void startActionDiscover(Context context, int page, String sortBy, int minVotes) {
        String sortSetting;
        switch (sortBy) {
            case MovieFetchServiceContract.SORT_SETTING_POPULARITY:
                sortSetting = TMDB_QUERY_SORT_POPULARITY_DESC;
                break;

            case MovieFetchServiceContract.SORT_SETTING_RATING:
                sortSetting = TMDB_QUERY_SORT_VOTE_DESC;
                break;

            case MovieFetchServiceContract.SORT_SETTING_FAVOURITE:
            default:
                return;
        }

        Intent intent = new Intent(context, MovieDataFetchHelperService.class);
        intent.setAction(MovieFetchServiceContract.ACTION_DISCOVER_MOVIES);
        intent.putExtra(MovieFetchServiceContract.EXTRA_PAGE_NUM, page);
        intent.putExtra(MovieFetchServiceContract.EXTRA_SORT_BY, sortSetting);
        intent.putExtra(MovieFetchServiceContract.EXTRA_MIN_VOTES, minVotes);
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
        tmdbConfig = new Configuration();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TMDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(TmdbService.class);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (MovieFetchServiceContract.ACTION_DISCOVER_MOVIES.equals(action)) {
                final int page = intent.getIntExtra(MovieFetchServiceContract.EXTRA_PAGE_NUM, 1);
                final String sortBy = intent.getStringExtra(MovieFetchServiceContract.EXTRA_SORT_BY);
                final int minVotes = intent.getIntExtra(MovieFetchServiceContract.EXTRA_MIN_VOTES, 0);
                handleActionDiscover(page, sortBy, minVotes);
            } else if (MovieFetchServiceContract.ACTION_FETCH_MOVIE_DETAILS.equals(action)) {
                final int movieId = intent.getIntExtra(MovieFetchServiceContract.EXTRA_MOVIE_ID, 0);
                handleActionFetchMovie(movieId);
                handleActionFetchMovieTrailers(movieId);
                handleActionFetchMovieReviews(movieId);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     * @param page
     * @param sortBy
     */
    private void handleActionDiscover(int page, String sortBy, int minVotes) {
        createTMDBConfig();
        Call<TmdbResponse> tmdbResponseCall = service.discoverMovies(TMDB_API_VERSION,
                APIKey.TMDB_API_KEY, sortBy, minVotes, page);
        try {
            TmdbResponse tmdbResponse = tmdbResponseCall.execute().body();
            List<MovieResponse> movieResults = tmdbResponse.getResults();
            List<Movie> movies = new ArrayList<>();
            for (MovieResponse result : movieResults) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Date releaseDate = null;
                try {
                    releaseDate = df.parse(result.getReleaseDate());
                } catch (ParseException e) {
                    Log.e(LOG_TAG, "Movie " + result.getTitle() + ", " + result.getReleaseDate() + " : " + e.getMessage());
                }

                String thumbnails =  getTmdbConfig().getImageBaseUrl() +
                        Configuration.PREFERRED_IMAGE_SIZE +
                        result.getPosterPath();
                Movie movie = new Movie(result.getId(), result.getTitle(), result.getLanguage(),
                        thumbnails, result.getOverview(), result.getVoteAverage(),
                        result.getVoteCount(), 0, releaseDate, null, null);
                movies.add(movie);
            }

            Intent intent = new Intent(MovieDataFetchHelperService.this, MainActivity.class);
            intent.setAction(MovieFetchServiceContract.REPLY_DISCOVER_MOVIES_UPDATE);
            intent.putExtra(MovieFetchServiceContract.EXTRA_PAGE_NUM, tmdbResponse.getPage());
            intent.putParcelableArrayListExtra(MovieFetchServiceContract.EXTRA_MOVIE_LIST,
                    (ArrayList<? extends Parcelable>) movies);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Network API failed: " + e.getMessage());
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchMovie(int movieId) {
        Call<MovieResponse> movieResultCall = service.fetchMovieDetails(TMDB_API_VERSION, movieId, APIKey.TMDB_API_KEY);
        try {
            MovieResponse response = movieResultCall.execute().body();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date releaseDate = null;
            try {
                releaseDate = df.parse(response.getReleaseDate());
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Movie " + response.getTitle() + ", " + response.getReleaseDate() + " : " + e.getMessage());
            }

            String thumbnail =  getTmdbConfig().getImageBaseUrl() +
                    Configuration.PREFERRED_IMAGE_SIZE +
                    response.getPosterPath();
            Movie movie = new Movie(response.getId(), response.getTitle(), response.getLanguage(),
                    thumbnail, response.getOverview(), response.getVoteAverage(), response.getVoteCount(),
                    response.getRuntime(), releaseDate, null, null);

            Intent intent = new Intent(MovieFetchServiceContract.REPLY_FETCH_MOVIE_UPDATE);
            intent.putExtra(MovieFetchServiceContract.EXTRA_MOVIE_DETAILS, movie);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Network request failure");
        }

    }

    /**
     *
     * @param movieId
     */
    private void handleActionFetchMovieTrailers(int movieId)
    {
        Call<VideoResponse> videoResponseCall = service.fetchMovieTrailers(TMDB_API_VERSION, movieId, APIKey.TMDB_API_KEY);
        try {
            VideoResponse response = videoResponseCall.execute().body();
            List<Video> videos = new ArrayList<>();
            for (VideoResponse.VideoItem item : response.getResults()) {
                Video video = new Video(item.getId(), item.getName(),
                        createYouTubeURL(item.getKey()), createYouTubeThumbnailURL(item.getKey()),
                        item.getType());
                videos.add(video);
            }

            Intent intent = new Intent(MovieFetchServiceContract.REPLY_FETCH_MOVIE_TRAILERS);
            intent.putParcelableArrayListExtra(MovieFetchServiceContract.EXTRA_MOVIE_TRAILERS,
                    (ArrayList<? extends Parcelable>) videos);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } catch (IOException e) {
            Log.v(LOG_TAG, "handleActionFetchMovieTrailers()::Network API failed; " + e.getMessage());
        }
    }

    /**
     *
     * @param movieId
     */
    private void handleActionFetchMovieReviews(int movieId)
    {
        Call<ReviewResponse> reviewResponseCall = service.fetchMovieReviews(TMDB_API_VERSION, movieId, APIKey.TMDB_API_KEY);
        try {
            ReviewResponse response = reviewResponseCall.execute().body();
            List<Review> reviews = new ArrayList<>(response.getTotal_results());
            for (ReviewResponse.ReviewItem item : response.getResults()) {
                Review review = new Review(item.getId(), item.getAuthor(), item.getContent(), item.getUrl());
                reviews.add(review);
            }

            Intent intent = new Intent(MovieFetchServiceContract.REPLY_FETCH_MOVIE_REVIEWS);
            intent.putParcelableArrayListExtra(MovieFetchServiceContract.EXTRA_MOVIE_REVIEWS,
                    (ArrayList<? extends Parcelable>) reviews);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } catch (IOException e) {
            Log.v(LOG_TAG, "handleActionFetchMovieReviews()::Network API failed; " + e.getMessage());
        }
    }

    /**
     *
     */
    private void createTMDBConfig() {
        Call<ConfigResponse> configResponseCall = service.getConfiguration(TMDB_API_VERSION, APIKey.TMDB_API_KEY);
        try {
            ConfigResponse configResponse = configResponseCall.execute().body();
            tmdbConfig = new Configuration(configResponse.getBaseUrl(), configResponse.getSecureBaseUrl(), configResponse.getPosterSizes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param videoKey
     * @return
     */
    private String createYouTubeURL(String videoKey) {
        Uri uri = new Uri.Builder()
                .scheme("https").authority(YOUTUBE_BASE_URL)
                .appendPath(YOUTUBE_VIDEO_PATH)
                .appendQueryParameter(YOUTUBE_VIDEO_QUERY, videoKey)
                .build();

        return uri.toString();
    }

    private String createYouTubeThumbnailURL(String videoKey) {
        Uri uri = new Uri.Builder()
                .scheme("http").authority(YOUTUBE_IMG_BASE_URL)
                .appendPath("vi")
                .appendPath(videoKey)
                .appendPath("default.jpg")
                .build();

        return uri.toString();

    }

}


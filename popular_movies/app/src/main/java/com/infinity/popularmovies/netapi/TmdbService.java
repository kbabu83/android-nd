package com.infinity.popularmovies.netapi;

import com.infinity.popularmovies.data.Review;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by KBabu on 18-Dec-15.
 */
public interface TmdbService {
        @GET("/{version}/configuration")
        Call<ConfigResponse> getConfiguration(@Path("version") int version, @Query("api_key") String key);

        @GET("/{version}/discover/movie")
        Call<TmdbResponse> discoverMovies(@Path("version") int version, @Query("api_key") String key, @Query("sort_by") String sortSetting, @Query("page") int page);

        @GET("/{version}/movie/{id}")
        Call<MovieResponse> fetchMovieDetails(@Path("version") int version, @Path("id") int movieId, @Query("api_key") String key);

        @GET("/{version}/movie/{id}/videos")
        Call<MovieResponse> fetchMovieTrailers(@Path("version") int version, @Path("id") int movieId, @Query("api_key") String key);

        @GET("/{version}/movie/{id}/videos")
        Call<Review> fetchMovieReviews(@Path("version") int version, @Path("id") int movieId, @Query("api_key") String key);
}

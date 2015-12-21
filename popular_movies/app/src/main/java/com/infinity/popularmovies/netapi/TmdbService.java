package com.infinity.popularmovies.netapi;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by KBabu on 18-Dec-15.
 */
public interface TmdbService {
        @GET("/{version}/configuration")
        Call<ConfigResponse> getConfiguration(@Path("version") int version,
                                              @Query("api_key") String key);

        @GET("/{version}/discover/movie")
        Call<TmdbResponse> discoverMovies(@Path("version") int version,
                                          @Query("api_key") String key,
                                          @Query("sort_by") String sortSetting,
                                          @Query("vote_count.gte") int minVoteCount,
                                          @Query("page") int page);

        @GET("/{version}/movie/{id}")
        Call<MovieResponse> fetchMovieDetails(@Path("version") int version,
                                              @Path("id") int movieId,
                                              @Query("api_key") String key);

        @GET("/{version}/movie/{id}/videos")
        Call<VideoResponse> fetchMovieTrailers(@Path("version") int version,
                                               @Path("id") int movieId,
                                               @Query("api_key") String key);

        @GET("/{version}/movie/{id}/reviews")
        Call<ReviewResponse> fetchMovieReviews(@Path("version") int version,
                                       @Path("id") int movieId,
                                       @Query("api_key") String key);
}

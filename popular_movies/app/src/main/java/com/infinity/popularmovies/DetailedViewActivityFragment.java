package com.infinity.popularmovies;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.infinity.popularmovies.data.MovieFetchServiceContract;
import com.infinity.popularmovies.data.Review;
import com.infinity.popularmovies.data.Video;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailedViewActivityFragment extends Fragment {
    private static final String LOG_TAG = DetailedViewActivityFragment.class.getSimpleName();
    private Movie movie;
    Activity parent;
    View rootView;
    private List<String> movieTrailerList = new ArrayList<>();
    private ArrayAdapter<String> mMovieTrailerDataAdapter;

    private List<String> movieReviewList = new ArrayList<>();
    private ArrayAdapter<String> mMovieReviewDataAdapter;

    private List<Pair<String, String>> movieReviewsList = new ArrayList<>();
    private ArrayAdapter<Pair<String, String>> mMovieReviewAdapter;

    private IntentFilter intentFilter = new IntentFilter();

    public DetailedViewActivityFragment() { }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null)
                return;

            final String action = intent.getAction();
            switch (action) {
                case MovieFetchServiceContract.REPLY_FETCH_MOVIE_UPDATE:
                    movie = intent.getParcelableExtra(MovieFetchServiceContract.EXTRA_MOVIE_DETAILS);
                    updateViewContent();
                    break;

                case MovieFetchServiceContract.REPLY_FETCH_MOVIE_TRAILERS:
                    List<Video> videos = intent.getParcelableArrayListExtra(MovieFetchServiceContract.EXTRA_MOVIE_TRAILERS);
                    if (videos == null || videos.size() == 0) {
                        Log.v(LOG_TAG, "No videos available for movie");
                        return;
                    }
                    movie.setTrailers(videos);
                    updateMovieTrailers();
                    break;

                case MovieFetchServiceContract.REPLY_FETCH_MOVIE_REVIEWS:
                    List<Review> reviews = intent.getParcelableArrayListExtra(MovieFetchServiceContract.EXTRA_MOVIE_REVIEWS);
                    if (reviews == null || reviews.size() ==0) {
                        Log.v(LOG_TAG, "No reviews available for movie");
                        return;
                    }
                    movie.setReviews(reviews);
                    updateMovieReviews();
                    break;

                default:
                    return;
            }

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parent = getActivity();
        intentFilter.addAction(MovieFetchServiceContract.REPLY_FETCH_MOVIE_UPDATE);
        intentFilter.addAction(MovieFetchServiceContract.REPLY_FETCH_MOVIE_TRAILERS);
        intentFilter.addAction(MovieFetchServiceContract.REPLY_FETCH_MOVIE_REVIEWS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_detailed_view, container, false);
        Intent intent = parent.getIntent();
        movie = intent.getParcelableExtra("selected_movie");
        if (movie == null)
            return rootView;

        mMovieTrailerDataAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_movie_trailer,
                R.id.txt_trailer_title, movieTrailerList);

        ListView trailerList = (ListView) rootView.findViewById(R.id.list_view_movie_trailers);
        trailerList.setAdapter(mMovieTrailerDataAdapter);

/*
        mMovieReviewDataAdapter = new ArrayAdapter<>(parent, R.layout.list_item_movie_review,
                R.id.txt_movie_review_content, movieReviewList);
        ListView reviewList = (ListView) rootView.findViewById(R.id.list_view_movie_reviews);
        reviewList.setAdapter(mMovieReviewDataAdapter);
*/

        mMovieReviewAdapter = new ArrayAdapter<Pair<String, String>>(parent,
                R.layout.list_item_movie_review, R.id.container_movie_review_entry, movieReviewsList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LinearLayout containerLayout;
                if (convertView == null) {
                    containerLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie_review, parent, false);
                }
                else {
                    containerLayout = (LinearLayout) convertView;
                }

                Pair<String, String> currentItem = getItem(position);
                TextView authorText = (TextView) containerLayout.findViewById(R.id.txt_movie_review_title);
                authorText.setText(currentItem.first);

                TextView contentText = (TextView) containerLayout.findViewById(R.id.txt_movie_review_content);
                contentText.setText(currentItem.second);

                return containerLayout;
            }

        };

        ListView reviewsList = (ListView) rootView.findViewById(R.id.list_view_movie_reviews);
        reviewsList.setAdapter(mMovieReviewAdapter);

        updateViewContent();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(parent).registerReceiver(broadcastReceiver, intentFilter);
        fetchMovieDetails(movie.getId());
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(parent).unregisterReceiver(broadcastReceiver);
    }

    private void fetchMovieDetails(int movieId) {
        MovieDataFetchHelperService.startActionFetchMovie(parent, movieId);
    }

    private void updateViewContent() {
        if (rootView == null || movie == null) {
            return;
        }

        String movieName = movie.getTitle();
        String moviePosterUrl = movie.getPosterThumbnail();
        String moviePlot = movie.getSynopsis();
        String movieRating = String.valueOf(movie.getRating());
        String vote_count = String.valueOf(movie.getVoteCount());
        Date releaseDate = movie.getReleaseDate();
        parent.setTitle(movieName + " (" + (releaseDate.getYear() + 1900) + ")");

        ImageView imageView = (ImageView) rootView.findViewById(R.id.img_poster_view_large);
        if (imageView != null) {
            Picasso.with(getActivity()).load(moviePosterUrl).into(imageView);
        }

        TextView ratingText = (TextView) rootView.findViewById(R.id.txt_movie_rating);
        if (ratingText != null) {
            ratingText.setText(movieRating + "/10\t(" + vote_count + " votes)");
        }

        TextView plotText = (TextView) rootView.findViewById(R.id.txt_movie_plot);
        if (plotText != null) {
            plotText.setText(moviePlot);
        }

        TextView runtimeText = (TextView) rootView.findViewById(R.id.txt_movie_duration);
        if (runtimeText != null) {
            runtimeText.setText(String.valueOf(movie.getDuration()) + " mins");
        }

        TextView relDateText = (TextView) rootView.findViewById(R.id.txt_movie_date);
        if (relDateText != null) {
            relDateText.setText(new SimpleDateFormat("dd-MM-yyyy").format(releaseDate));
        }

    }

    private void updateMovieTrailers() {
        Log.v(LOG_TAG, "Number of trailers: " + movie.getTrailers().size());
        movieTrailerList.clear();
        for (Video video : movie.getTrailers())
            movieTrailerList.add(video.getName());

        mMovieTrailerDataAdapter.notifyDataSetChanged();

    }

    private void updateMovieReviews() {
        Log.v(LOG_TAG, "Number of reviews: " + movie.getReviews().size());
        movieReviewsList.clear();
        for (Review review : movie.getReviews()) {
            movieReviewsList.add(new Pair<>(review.getAuthor(), review.getContent()));
        }

        mMovieReviewAdapter.notifyDataSetChanged();

    }

    private void updateMovieReviews2() {
        Log.v(LOG_TAG, "Number of reviews: " + movie.getReviews().size());
        movieReviewList.clear();
        for (Review review : movie.getReviews()) {
            movieReviewList.add(review.getContent());
        }

        mMovieReviewDataAdapter.notifyDataSetChanged();

    }

}


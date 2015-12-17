package com.infinity.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailedViewActivityFragment extends Fragment {

    public DetailedViewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detailed_view, container, false);
        Intent intent = getActivity().getIntent();
        Movie movie = intent.getParcelableExtra("selected_movie");
        if (movie == null)
            return rootView;

        String movieName = movie.getTitle();
        String moviePosterUrl = movie.getPosterThumbnail();
        String moviePlot = movie.getSynopsis();
        String movieRating = String.valueOf(movie.getRating());
        String vote_count = String.valueOf(movie.getVoteCount());
        Date releaseDate = movie.getReleaseDate();
        getActivity().setTitle(movieName + " (" + (releaseDate.getYear() + 1900) + ")");

        ImageView imageView = (ImageView) rootView.findViewById(R.id.img_poster_view_large);
        if (imageView != null) {
            Picasso.with(getActivity()).load(moviePosterUrl).into(imageView);
        }

        TextView ratingText = (TextView) rootView.findViewById(R.id.txt_movie_rating);
        if (ratingText != null) {
            ratingText.setText(movieRating +"/10\t(" + vote_count + " votes)");
        }

        TextView plotText = (TextView) rootView.findViewById(R.id.txt_movie_plot);
        if (plotText != null) {
            plotText.setText(moviePlot);
        }

        TextView relDateText = (TextView) rootView.findViewById(R.id.txt_movie_rel_date);
        if (relDateText != null) {
            relDateText.setText(new SimpleDateFormat("dd-MM-yyyy").format(releaseDate));
        }

        return rootView;
    }

}


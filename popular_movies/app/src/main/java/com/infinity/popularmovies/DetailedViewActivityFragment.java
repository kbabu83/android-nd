package com.infinity.popularmovies;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
        String movieName = intent.getStringExtra("movie_name");
        String moviePosterUrl = intent.getStringExtra("movie_poster");
        String moviePlot = intent.getStringExtra("movie_plot");
        String movieRating = String.valueOf(intent.getDoubleExtra("movie_rating", 0.0));
        String movieRelease = intent.getStringExtra("movie_release");


        ImageView imageView = (ImageView) rootView.findViewById(R.id.img_poster_view_large);
        if (imageView != null) {
            Picasso.with(getActivity()).load(moviePosterUrl).into(imageView);
        }

        TextView textView = (TextView) rootView.findViewById(R.id.txt_movie_name_detailed);
        if (textView != null) {
            textView.setText(movieName);
        }

        TextView ratingText = (TextView) rootView.findViewById(R.id.txt_movie_rating);
        if (ratingText != null) {
            ratingText.setText(movieRating +"/10");
        }

        TextView plotText = (TextView) rootView.findViewById(R.id.txt_movie_plot);
        if (plotText != null) {
            plotText.setText(moviePlot);
        }

        return rootView;
    }

}


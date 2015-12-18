package com.infinity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KBabu on 30-Nov-15.
 */
public class ImageViewAdapter extends RecyclerView.Adapter<ImageViewAdapter.ViewHolder> {
    private static final String LOG_TAG = ImageViewAdapter.class.getSimpleName();
    private List<Movie> dataset;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public int position = 0;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class ListImageClickListener implements View.OnClickListener {
        private ViewHolder viewHolder;

        public ListImageClickListener(ViewHolder vh) {
            viewHolder = vh;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), DetailedViewActivity.class);
            Movie movie = dataset.get(viewHolder.position);
            intent.putExtra("selected_movie", movie);
            context.startActivity(intent);
        }
    }

    public ImageViewAdapter(Context c, List<Movie> movieData) {
        context = c;
        dataset = new ArrayList<>();
        if (movieData != null)
            dataset.addAll(movieData);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView imageView = (ImageView)LayoutInflater.from(context).inflate(
                R.layout.grid_list_item_image, null);
        ViewHolder vh = new ViewHolder(imageView);
        imageView.setOnClickListener(new ListImageClickListener(vh));
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.position = position;
        String thumbnail = dataset.get(position).getPosterThumbnail();
        Picasso.with(context).load(thumbnail).placeholder(R.drawable.no_preview_available).into((ImageView) holder.itemView);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public void updateImageDataSet(@NonNull List<Movie> imageData) {
        dataset.clear();
        dataset.addAll(imageData);
    }

}

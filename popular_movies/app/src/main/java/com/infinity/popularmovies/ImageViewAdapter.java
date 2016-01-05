package com.infinity.popularmovies;

import android.content.Context;
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
 * Adapter class to render content in the MainActivity's GridView
 * Created by KBabu on 30-Nov-15.
 */
public class ImageViewAdapter<T> extends RecyclerView.Adapter<ImageViewAdapter.ViewHolder> {
    private static final String LOG_TAG = ImageViewAdapter.class.getSimpleName();
    private List<T> dataset;
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
            try {
                MainActivityFragment.ListItemClickListener<T> l = (MainActivityFragment.ListItemClickListener<T>) context;
                l.onImageListItemClicked(dataset.get(viewHolder.position));
            }
            catch (ClassCastException e) {
                Log.e(LOG_TAG, "Class cast failed; posting a message won't work");
            }
        }
    }

    public ImageViewAdapter(Context c, List<T> tList) {
        context = c;
        dataset = new ArrayList<>();
        if (tList != null)
            dataset.addAll(tList);
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
        T item = dataset.get(position);
        String thumbnail = item.toString();
        Picasso.with(context).load(thumbnail).placeholder(R.drawable.no_preview_available).into((ImageView) holder.itemView);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public void updateImageDataSet(@NonNull List<T> imageData) {
        dataset.clear();
        dataset.addAll(imageData);
    }

}

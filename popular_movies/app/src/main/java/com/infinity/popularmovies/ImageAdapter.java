package com.infinity.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KBabu on 25-Nov-15.
 */
public class ImageAdapter extends BaseAdapter {
    private Context context;
    private List<String> dataSet = new ArrayList<>();

    public ImageAdapter(Context c) {
        context = c;
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setMinimumHeight(278);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setPadding(8, 8, 8, 8);
        }
        else {
            imageView = (ImageView)convertView;
        }

        Picasso.with(context).load(dataSet.get(position)).centerInside().fit().into(imageView);
        return imageView;
    }

    public void updateImageDataSet(@NonNull List<String> imageData) {
        dataSet.clear();
        dataSet.addAll(imageData);
    }
}

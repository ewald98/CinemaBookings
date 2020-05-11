package com.example.cinemabooking;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] filmtitles;
    private final String[] filmdescriptions;
    private final String[] filmpictures;
    private final Integer[] filmID;

    public CustomListAdapter(Activity context, String[] filmtitles, String[] filmdescriptions, String[] filmpictures, Integer[] filmID) {
        super(context, R.layout.list_mainactivity, filmtitles);

        this.context = context;
        this.filmtitles = filmtitles;
        this.filmdescriptions = filmdescriptions;
        this.filmpictures = filmpictures;
        this.filmID = filmID;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater =context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_mainactivity, null,true);

        TextView textTitles = (TextView) rowView.findViewById(R.id.filmtitles);
        TextView textDescriptions = (TextView)  rowView.findViewById(R.id.filmdescriptions);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.filmpictures);

        textTitles.setText(filmtitles[position]);
        textDescriptions.setText(filmdescriptions[position]);
        imageView.setImageDrawable(getImageDrawableRes(filmpictures[position]));

        return rowView;
    };
    protected Drawable getImageDrawableRes(String drawableRes)
    {

        Integer res;
        Drawable d;
        Bitmap bitmap = null;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL url = new URL(drawableRes);
            bitmap =  BitmapFactory.decodeStream((InputStream)url.getContent());
        } catch (IOException e) {
            //Log.e(TAG, e.getMessage());
        }
        d = new BitmapDrawable(context.getResources(), bitmap);
        return d;
    }
}
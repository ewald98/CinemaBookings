package com.example.cinemabooking;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] filmtitles;
    private final String[] filmdescriptions;
    private final Integer[] filmpictures;

    public CustomListAdapter(Activity context, String[] filmtitles, String[] filmdescriptions, Integer[] filmpictures) {
        super(context, R.layout.list_mainactivity, filmtitles);

        this.context = context;
        this.filmtitles = filmtitles;
        this.filmdescriptions = filmdescriptions;
        this.filmpictures = filmpictures;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater =context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_mainactivity, null,true);

        TextView textTitles = (TextView) rowView.findViewById(R.id.filmtitles);
        TextView textDescriptions = (TextView)  rowView.findViewById(R.id.filmdescriptions);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.filmpictures);

        textTitles.setText(filmtitles[position]);
        textDescriptions.setText(filmdescriptions[position]);
        imageView.setImageResource(filmpictures[position]);

        return rowView;
    };
}
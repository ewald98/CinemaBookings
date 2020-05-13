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
import java.util.List;

public class CustomListAdapterReservation extends ArrayAdapter<String>{
    private final Activity context;
    private final String[] filmdates;
    private final Integer[] availableseats;

    public CustomListAdapterReservation(Activity context, String[] filmdates, Integer[] availableseats) {
        super(context, R.layout.list_secondactivity, filmdates);

        this.context = context;
        this.filmdates = filmdates;
        this.availableseats = availableseats;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_secondactivity, null,true);

        TextView textDate = (TextView) rowView.findViewById(R.id.filmdates);
        TextView textSeats = (TextView)  rowView.findViewById(R.id.availableseatnumber);

        textDate.setText(filmdates[position]);
        textSeats.setText("Available seat number: " + Integer.toString(availableseats[position]));

        return rowView;
    };
}
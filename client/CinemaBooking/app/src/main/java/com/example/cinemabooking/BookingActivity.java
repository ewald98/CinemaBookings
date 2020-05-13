package com.example.cinemabooking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class BookingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        Intent intent = getIntent();
        String movieID = intent.getStringExtra("message1");
        String movieName = intent.getStringExtra("message2");
        String movieDates = intent.getStringExtra("message2");
        String message = movieName + " " + movieID + " " + movieDates;

        TextView textView = (TextView)findViewById(R.id.bookingDataTextView);
        textView.setText(message);
    }
}

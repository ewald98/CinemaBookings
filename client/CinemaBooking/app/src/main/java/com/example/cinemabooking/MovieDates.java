package com.example.cinemabooking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MovieDates extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_dates);

        setTitle("dev2qa.com - Target Activity");

        // Get the transferred data from source activity.
        Intent intent = getIntent();
        String message = intent.getStringExtra("message1");
        message += " " + intent.getStringExtra("message2");

        TextView textView = (TextView)findViewById(R.id.requestDataTextView);
        textView.setText(message);
    }
}

package com.example.cinemabooking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class BookingActivity extends AppCompatActivity {

    private TextView chosenmovietitle;
    private TextView chosendate;

    private EditText fullname;
    private EditText phonenumber;
    private EditText nrreservedseats;

    private Button bookseatsbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        Intent intent = getIntent();
        String movieID = intent.getStringExtra("message1");
        String movieName = intent.getStringExtra("message2");
        String movieDates = intent.getStringExtra("message3");
        String message = movieName + " " + movieID + " " + movieDates;
/*
        TextView textView = (TextView)findViewById(R.id.bookingDataTextView);
        textView.setText(message);*/

        TextView movieText = (TextView)findViewById(R.id.chosenmovietitle);
        movieText.setText(movieName + " " + movieID);

        TextView dateText = (TextView) findViewById(R.id.chosendate);
        dateText.setText(movieDates);

        fullname = (EditText)findViewById(R.id.fullname);
        phonenumber = (EditText)findViewById(R.id.phonenumber);
        nrreservedseats = (EditText)findViewById(R.id.nrreservedseats);

        bookseatsbutton = (Button) findViewById(R.id.bookseatsbutton);

        bookseatsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = fullname.getText().toString();
                String phonenr = phonenumber.getText().toString();
                String seats = nrreservedseats.getText().toString();

                if(name.isEmpty()){
                    fullname.setError("Please provide full name");
                    fullname.requestFocus();
                }
                if(phonenr.isEmpty()){
                    phonenumber.setError("Please provide a phone number");
                    phonenumber.requestFocus();
                }
                if(seats.isEmpty()){
                    nrreservedseats.setError("Please provide a number of seats");
                    nrreservedseats.requestFocus();
                }
                if(!name.isEmpty() && !phonenr.isEmpty() && !seats.isEmpty()){
                    Log.d("",name);

                    Log.d("",phonenr);
                    Log.d("",seats);
                    Toast toast = Toast.makeText(BookingActivity.this, "Booking with success", Toast.LENGTH_LONG);
                    toast.show();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent goToMainActivity = new Intent(BookingActivity.this, MainActivity.class);
                            startActivity(goToMainActivity);
                        }
                    }, 1000);
                }
            }
        });
    }
}

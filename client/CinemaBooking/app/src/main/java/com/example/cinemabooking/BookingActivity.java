package com.example.cinemabooking;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BookingActivity extends AppCompatActivity {

    private TextView chosenmovietitle;
    private TextView chosendate;

    private EditText fullname;
    private EditText phonenumber;
    private EditText nrreservedseats;

    private String name;
    private String phoneNo;
    private String seats;

    private String movieID;
    private String showTime;
    private String movieName;

    private Button bookseatsbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        Intent intent = getIntent();
        movieID = intent.getStringExtra("message1");
        movieName = intent.getStringExtra("message2");
        showTime = intent.getStringExtra("message3");
        String message = movieName + " " + movieID + " " + showTime;

        TextView movieText = (TextView) findViewById(R.id.chosenmovietitle);
        movieText.setText(movieName + " " + movieID);

        TextView dateText = (TextView) findViewById(R.id.chosendate);
        dateText.setText(showTime);

        fullname = (EditText) findViewById(R.id.fullname);
        phonenumber = (EditText) findViewById(R.id.phonenumber);
        nrreservedseats = (EditText) findViewById(R.id.nrreservedseats);

        bookseatsbutton = (Button) findViewById(R.id.bookseatsbutton);

        bookseatsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = fullname.getText().toString();
                phoneNo = phonenumber.getText().toString();
                seats = nrreservedseats.getText().toString();

                if (name.isEmpty()) {
                    fullname.setError("Please provide full name");
                    fullname.requestFocus();
                }
                if (phoneNo.isEmpty()) {
                    phonenumber.setError("Please provide a phone number");
                    phonenumber.requestFocus();
                }
                if (seats.isEmpty()) {
                    nrreservedseats.setError("Please provide a number of seats");
                    nrreservedseats.requestFocus();
                }

                new NetworkOperator().execute();
            }
        });

    }

    class NetworkOperator extends AsyncTask<Void, Void, Integer> {

        public static final String IP_ADDRESS = "10.0.2.2";
        public static final int PORT = 2005;

        private String[] filmNames;
        private String[] filmDescriptions;
        private String[] filmImageUrl;
        private Integer[] filmID;


        private final static int REQUEST_CODE_1 = 1;
        private ListView list;

        Socket socket;
        OutputStream os;
        InputStream is;

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Integer doInBackground(Void... voids) {

            socket = null;
            try {
                socket = waitForConnection(IP_ADDRESS, PORT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int result = -1;
            try {
                os = socket.getOutputStream();
                sendMessage();
                is = socket.getInputStream();
                result = readMessages();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return result;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(Integer result) {

            if (!name.isEmpty() && !phoneNo.isEmpty() && !seats.isEmpty()) {
                if (0 == result) {
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
                } else if (5 == result) {
                    String SelectedItem = "Seats number not available";
                    Toast.makeText(getApplicationContext(), SelectedItem, Toast.LENGTH_SHORT).show();
                } else {
                    String SelectedItem = "Server Error, please try again!";
                    Toast.makeText(getApplicationContext(), SelectedItem, Toast.LENGTH_SHORT).show();
                }

            }
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public int readMessages() throws IOException {
            DataInputStream dis = new DataInputStream(is);
            int msgLen = readIntLittleEndian(dis);
            int msgId = readIntLittleEndian(dis);
            int msgResult = readIntLittleEndian(dis);

            if (msgId != 8)
                throw new IOException("Invalid message id");
            return msgResult;
        }

        private Socket waitForConnection(String address, int port) throws InterruptedException {
            while (true) {
                try {
                    return new Socket(address, port);
                } catch (IOException e) {
                    Thread.sleep(3000);
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void sendMessage() {
            DataOutputStream dOut = new DataOutputStream(os);
            byte[] msgLen = new byte[4];
            byte[] msgId = new byte[4];
            byte[] phoneNoLenByte = new byte[4];
            byte[] movieIDByte = new byte[4];
            byte[] seatsByte = new byte[4];
            byte[] showTimeByte = new byte[13];
            byte[] phoneNoByte = phoneNo.getBytes(StandardCharsets.UTF_8);
            byte[] bookingNameByte = name.getBytes(StandardCharsets.UTF_8);

            int len = 29 + phoneNo.length() + name.length();
            msgLen = intToLittleEndian(len);
            msgId = intToLittleEndian(7);
            phoneNoLenByte = intToLittleEndian(phoneNo.length());
            movieIDByte = intToLittleEndian(Integer.parseInt(movieID));
            seatsByte = intToLittleEndian(Integer.parseInt(seats));
            showTimeByte = showTime.getBytes(StandardCharsets.UTF_8);

            byte[] data = new byte[len + 4];

            System.arraycopy(msgLen, 0, data, 0, 4);
            System.arraycopy(msgId, 0, data, 4, 4);
            System.arraycopy(phoneNoLenByte, 0, data, 8, 4);
            System.arraycopy(movieIDByte, 0, data, 12, 4);
            System.arraycopy(seatsByte, 0, data, 16, 4);
            System.arraycopy(showTimeByte, 0, data, 20, 13);
            System.arraycopy(phoneNoByte, 0, data, 33, phoneNo.length());
            System.arraycopy(bookingNameByte, 0, data, 33 + phoneNo.length(), name.length());

            try {
                dOut.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int readIntLittleEndian(DataInputStream dis) throws IOException {
            return (dis.readByte() & 0xff) << 24 |
                    ((int) dis.readByte() & 0xff) << 16 |
                    ((int) dis.readByte() & 0xff) << 8 |
                    ((int) dis.readByte() & 0xff);
        }

        private byte[] intToLittleEndian(int number) {
            byte[] b = new byte[4];
            b[3] = (byte) (number & 0xFF);
            b[2] = (byte) ((number >> 8) & 0xFF);
            b[1] = (byte) ((number >> 16) & 0xFF);
            b[0] = (byte) ((number >> 24) & 0xFF);
            return b;
        }


    }
}

package com.example.cinemabooking;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShowtimesActivity extends AppCompatActivity {
    private ListView list2;
    private String movieID;
    private String movieName;
    private String[] movieDate;
    private Integer[] movieSeatsAvailable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_dates);

        // Get the transferred data from source activity.
        Intent intent = getIntent();
        movieID = intent.getStringExtra("message1");
        movieName = intent.getStringExtra("message2");
//        String message = movieName + " " + movieID;
//
//        TextView textView = (TextView)findViewById(R.id.requestDataTextView);
//        textView.setText(message);

        new NetworkOperator().execute();
    }
    public static byte[] concatenateByteArrays(byte[] a, byte[] b) {

        if (a == null) return b;

        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    class NetworkOperator extends AsyncTask<Void, Void, Void> {

        public static final String IP_ADDRESS = "10.0.2.2";
        public static final int PORT = 2005;

        private List<String> movieDateList = new ArrayList<String>();
        private List<Integer> movieSeatsAvailableList = new ArrayList<Integer>();

        private final static int REQUEST_CODE_1 = 1;

        Socket socket;
        OutputStream os;
        InputStream is;

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(Void... voids) {

            socket = null;
            try {
                socket = waitForConnection(IP_ADDRESS, PORT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                os = socket.getOutputStream();
                sendMessage();
                is = socket.getInputStream();
                readMessages();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            movieDate = new String[movieDateList.size()];
            movieSeatsAvailable = new Integer[movieSeatsAvailableList.size()];

            Iterator itrDates=movieDateList.iterator();
            Iterator itrSeats=movieSeatsAvailableList.iterator();
            int i = 0;

            while(itrDates.hasNext() && itrSeats.hasNext()){
                String dt=(String)itrDates.next();
                Integer st=(Integer)itrSeats.next();
                movieDate[i] = dt;
                movieSeatsAvailable[i] = st;
                i++;
            }
            TextView tv_dynamic = new TextView(ShowtimesActivity.this);
            tv_dynamic = (TextView)findViewById(R.id.requestDataTextView);
            tv_dynamic.setText("This movie isn't available anymore!");

            CustomListAdapterReservation adapter = new CustomListAdapterReservation(ShowtimesActivity.this, movieDate, movieSeatsAvailable);
            list2 = (ListView)findViewById(R.id.list2);
            list2.setAdapter(adapter);

            list2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String SelectedItem = movieDate[+position];
                    Intent intent = new Intent(ShowtimesActivity.this, BookingActivity.class);
                    intent.putExtra("message1", movieID);
                    intent.putExtra("message2", movieName);
                    intent.putExtra("message3", SelectedItem);
                    startActivityForResult(intent, REQUEST_CODE_1);
                }
            });
            try {
                is = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void readMessages() throws IOException{
            DataInputStream dis = new DataInputStream(is);
            int msgLen = readIntLittleEndian(dis);
            int msgId = readIntLittleEndian(dis);
            int msgTimeLen = 13;

            byte[] timeByte;
            int seatsAvailable;

            while(msgId == 5)
            {
                timeByte = new byte[msgTimeLen];
                dis.readFully(timeByte);
                seatsAvailable = readIntLittleEndian(dis);

                String time = new String(timeByte, StandardCharsets.UTF_8);
                movieDateList.add(time);
                movieSeatsAvailableList.add(seatsAvailable);
                System.out.println(time + " " + seatsAvailable  + "\n");
                msgLen = readIntLittleEndian(dis);
                msgId = readIntLittleEndian(dis);

            }
            if(msgId != 5)
                throw new IOException("Invalid message id");
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

        private void sendMessage()
        {
            DataOutputStream dOut = new DataOutputStream(os);
            byte[] data = new byte[12];
            byte[] msgLen = new byte[4];
            byte[] msgId = new byte[4];
            byte[] msgMovieID = new byte[4];

            msgLen = intToLittleEndian(4);
            msgId = intToLittleEndian(4);
            msgMovieID = intToLittleEndian(Integer.parseInt(movieID));
            System.arraycopy(msgLen, 0, data, 0, 4);
            System.arraycopy(msgId, 0, data, 4, 4);
            System.arraycopy(msgMovieID, 0, data, 8, 4);
            try {
                dOut.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int readIntLittleEndian(DataInputStream dis) throws IOException {
            return (dis.readByte() & 0xff) << 24 |
                    ((int)dis.readByte() & 0xff) << 16 |
                    ((int)dis.readByte() & 0xff) << 8 |
                    ((int)dis.readByte() & 0xff);
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

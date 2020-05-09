package com.example.cinemabooking;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new NetworkOperator().execute();
    }

    public static byte[] concatenateByteArrays(byte[] a, byte[] b) {

        if (a == null) return b;

        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    class NetworkOperator extends AsyncTask<Void, Void, ArrayList<Movie>> {

        public static final String IP_ADDRESS = "10.0.2.2";
        public static final int PORT = 2004;

        Socket socket;
        OutputStream os;
        InputStream is;

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected ArrayList<Movie> doInBackground(Void... voids) {
            ArrayList<Movie> movieList = null;

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
                movieList = readMessages();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return movieList;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            TextView x = findViewById(R.id.film1title);
            x.setText("Server data received");
            /*
             TODO: @Adi, trebuie sa legi informatiile de la movie-uri la frontend

            for (i, i < movies.len, i++) {

                title1 = findViewById(R.id.film1title)
                title1.text = movie.title
            }
             */
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public ArrayList<Movie> readMessages() throws IOException {
            DataInputStream dis = new DataInputStream(is);
            int msgNameLen;
            int imageUrlLen;
            int movieId;
            int msgLen = readIntLittleEndian(dis);
            int msgId = readIntLittleEndian(dis);
            int descriptionLen;
            byte[] nameByte;
            byte[] imageUrlByte;
            byte[] descriptionByte;

            ArrayList<Movie> movieList = new ArrayList<Movie>();

            while(msgId == 2)
            {
                msgNameLen = readIntLittleEndian(dis);
                imageUrlLen = readIntLittleEndian(dis);
                movieId = readIntLittleEndian(dis);

                nameByte = new byte[msgNameLen];
                imageUrlByte = new byte[imageUrlLen];

                descriptionLen = msgLen - 16 - msgNameLen - imageUrlLen;
                descriptionByte = new byte[descriptionLen];
                dis.readFully(nameByte);

                dis.readFully(imageUrlByte);
                dis.readFully(descriptionByte);

                String imageUrl = new String(imageUrlByte, StandardCharsets.UTF_8);
                String name = new String(nameByte, StandardCharsets.UTF_8);
                String description = new String(descriptionByte, StandardCharsets.UTF_8);

                Movie movie = new Movie(name, movieId, imageUrl, description);
                movieList.add(movie);

                //System.out.println(movieId + " " + name + " " + imageUrl + " " + description + "\n");
                msgLen = readIntLittleEndian(dis);
                msgId = readIntLittleEndian(dis);

            }

            return movieList;
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
            byte[] data = new byte[8];
            byte[] msgLen = new byte[4];
            byte[] msgId = new byte[4];
            msgLen = intToLittleEndian(4);
            msgId = intToLittleEndian(1);
            System.arraycopy(msgLen, 0, data, 0, 4);
            System.arraycopy(msgId, 0, data, 4, 4);
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



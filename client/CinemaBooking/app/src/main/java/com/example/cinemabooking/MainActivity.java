package com.example.cinemabooking;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Socket socket;
    static OutputStream os;
    static InputStream is;
    private static List<Movie> movieList = new ArrayList<Movie>();

    public static final String ADDRESS = "localhost";
    public static final int PORT = 17175;
    public static final int DELAY = 0;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            Socket socket = null;
            try {
                socket = waitForConnection(ADDRESS, PORT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                os = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendMessage();
            try {
                is = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }
    private static Socket waitForConnection(String address, int port) throws InterruptedException {
        while (true) {
            try {
                return new Socket(address, port);
            } catch (IOException e) {
                Thread.sleep(3000);
            }
        }
    }

    private static byte[] intToLittleEndian(int number) {
        byte[] b = new byte[4];
        b[3] = (byte) (number & 0xFF);
        b[2] = (byte) ((number >> 8) & 0xFF);
        b[1] = (byte) ((number >> 16) & 0xFF);
        b[0] = (byte) ((number >> 24) & 0xFF);
        return b;
    }

    private static void sendMessage()
    {
        DataOutputStream dOut = new DataOutputStream(os);
        byte[] data = new byte[8];
        byte[] msgLen = new byte[4];
        byte[] msgId = new byte[4];
        msgLen = intToLittleEndian(8);
        msgId = intToLittleEndian(1);
        System.arraycopy(msgLen, 0, data, 0, 4);
        System.arraycopy(msgId, 0, data, 4, 4);
        try {
            dOut.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void readMessage() throws IOException
    {
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
    }
    public static int readIntLittleEndian(DataInputStream dis) throws IOException {
        return (dis.readByte() & 0xff) << 24 |
                ((int)dis.readByte() & 0xff) << 16 |
                ((int)dis.readByte() & 0xff) << 8 |
                ((int)dis.readByte() & 0xff);
    }
    public static byte[] concatenateByteArrays(byte[] a, byte[] b) {

        if (a == null) return b;

        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}



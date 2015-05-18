package alexkotsc.wyred.peer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by AlexKotsc on 17-05-2015.
 */
public class ClientAsyncTask extends AsyncTask<String, Void, String> {

    Socket s;
    private final String TAG = "ClientAsyncTask";

    @Override
    protected String doInBackground(String... params) {

        Log.d(TAG, "Starting client socket..");

        s = new Socket();

        try {
            s.bind(null);
            s.connect(new InetSocketAddress(params[0], 8080), 500);

            Log.d(TAG, "Socket connected...");

            OutputStream outputStream = s.getOutputStream();
            InputStream inputStream = s.getInputStream();

            Log.d(TAG, "Opening streams done...");

            PrintWriter pw = new PrintWriter(outputStream, true);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            Log.d(TAG, "Reader/writer initialized...");

            pw.write("I'm a client!");

            Log.d(TAG, "I'm done writing, now I'll listen for a reply.");

            String reply = br.readLine();

            Log.d(TAG, "I have my reply, closing.");

            outputStream.close();
            inputStream.close();

            Log.d(TAG, "Streams closed...");

            Log.d(TAG, "Reply from server: " + reply);
            s.close();
        } catch (Exception e) {
            Log.e(TAG, "FAILED to communicate with server: " + e.getMessage());
        } finally {
            if (s != null) {
                if (s.isConnected()) {
                    try {
                        s.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }
}

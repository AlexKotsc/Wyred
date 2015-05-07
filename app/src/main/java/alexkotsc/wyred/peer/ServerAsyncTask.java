package alexkotsc.wyred.peer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import alexkotsc.wyred.WifiP2P;

/**
 * Created by AlexKotsc on 06-05-2015.
 */
public class ServerAsyncTask extends AsyncTask<Socket, Void, String> {


    @Override
    protected String doInBackground(Socket... params) {

        String result = null;

        try {

            Socket clientSocket = params[0];
            InputStream is = clientSocket.getInputStream();
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);

            pw.println("Hello from Server!");

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            result = br.readLine();

            Log.d(WifiP2P.TAG, "Response from client: " + result);

            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return result;
    }
}

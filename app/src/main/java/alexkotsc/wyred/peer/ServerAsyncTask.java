package alexkotsc.wyred.peer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by AlexKotsc on 06-05-2015.
 */
public class ServerAsyncTask extends AsyncTask<Socket, Void, String> {

    private final String TAG = "ServerAsyncTask";
    private Socket clientSocket;

    @Override
    protected String doInBackground(Socket... params) {

        String result = null;

        Log.d(TAG, "Starting connection with client socket..");

        try {
            clientSocket = params[0];


            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            Log.d(TAG, "Opening streams done...");

            PrintWriter pw = new PrintWriter(outputStream, true);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            Log.d(TAG, "Reader/writers intialized...");

            String s = null;
            while ((s = br.readLine()) != null) {
                Log.d(TAG, "Client sent: " + s);
            }

            pw.println("I received your message!");

            inputStream.close();
            outputStream.close();

            Log.d(TAG, "Streams closed...");

            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(clientSocket!= null){
                if(clientSocket.isConnected()){
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        return result;
    }
}

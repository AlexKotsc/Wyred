package alexkotsc.wyred.peer;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by AlexKotsc on 17-05-2015.
 */
public class ServerRunnable implements Runnable {

    private final String TAG = "ServerRunnable";
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private boolean isStarted = true;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8080);

            while(isStarted){
                clientSocket = null;
                clientSocket = serverSocket.accept();
                Log.d(TAG, "New incoming connection, handing over to AsyncTask");
                ServerAsyncTask serverAsyncTask = new ServerAsyncTask();
                serverAsyncTask.execute(new Socket[]{clientSocket});
            }

            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

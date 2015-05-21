package alexkotsc.wyred.peer.conn;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.PublicKey;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by AlexKotsc on 17-05-2015.
 */
public class GroupSocketHandler extends Thread {

    ServerSocket serverSocket = null;
    private Handler handler;
    private static final String TAG = "GroupSocketHandler";
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(10,10,10, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

    public GroupSocketHandler(Handler handler) throws IOException {


        try {
            serverSocket = new ServerSocket(4545);
            this.handler = handler;
            Log.d(TAG, "ServerSocket started");
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdownNow();
            throw e;
        }
    }



    @Override
    public void run() {
        while(true){
            try {
                pool.execute(new ConnectionManager(serverSocket.accept(), handler));
                Log.d(TAG, "Launching client handler.");
            } catch (IOException e) {
                try {
                    if(serverSocket!= null && !serverSocket.isClosed()){
                        serverSocket.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                e.printStackTrace();
                pool.shutdownNow();
                break;
            }
        }
    }
}

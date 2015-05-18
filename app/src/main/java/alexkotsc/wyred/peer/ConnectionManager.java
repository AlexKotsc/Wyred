package alexkotsc.wyred.peer;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by AlexKotsc on 18-05-2015.
 */
public class ConnectionManager implements Runnable {

    private Socket socket = null;
    private Handler handler;

    private InputStream inputStream;
    private OutputStream outputStream;
    private static final String TAG = "ConnectionManager";

    public ConnectionManager(Socket socket, Handler handler){
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            byte[] buffer = new byte[1024];
            int bytes;

            handler.obtainMessage(666, this).sendToTarget();

            while(true){
                try {
                    bytes = inputStream.read(buffer);

                    if (bytes == -1) {
                        break;
                    }

                    Log.d(TAG, "Received: " + String.valueOf(buffer));

                    handler.obtainMessage(665, bytes, -1, buffer).sendToTarget();
                } catch (IOException e){
                    Log.e(TAG, "Disconnected", e);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                socket.close();
            } catch (IOException e ){
                e.printStackTrace();
            }
        }
    }

    public void write(byte[] buffer){
        try {
            outputStream.write(buffer);
            Log.d(TAG, "Finished writing: " + String.valueOf(buffer));
        } catch (IOException e){
            Log.e(TAG, "Exception during write", e);
        }
    }
}

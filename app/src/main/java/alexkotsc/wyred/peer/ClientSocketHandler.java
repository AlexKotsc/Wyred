package alexkotsc.wyred.peer;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by AlexKotsc on 18-05-2015.
 */
public class ClientSocketHandler extends Thread {

    private static final String TAG = "ClientSocketHandler";
    private Handler handler;
    private InetAddress mAddress;
    private ConnectionManager conMan;

    public ClientSocketHandler(Handler handler, InetAddress groupOwnerAddress) throws IOException {
        this.handler = handler;
        this.mAddress = groupOwnerAddress;
    }

    @Override
    public void run() {
        Socket socket = new Socket();

        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(), 4545), 5000);
            Log.d(TAG, "Launching client handler");

            conMan = new ConnectionManager(socket, handler);
            new Thread(conMan).start();

        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

}

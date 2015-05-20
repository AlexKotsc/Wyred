package alexkotsc.wyred.peer.conn;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import alexkotsc.wyred.peer.ChatMessage;

/**
 * Created by AlexKotsc on 18-05-2015.
 */
public class ConnectionManager implements Runnable {

    public static final int ManagerHandle = 1;
    public static final int ChatMessageBuffer = 2;
    public static final int ChatMessageObject = 3;

    private Socket socket = null;
    private Handler handler;

    private InputStream inputStream;
    private OutputStream outputStream;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
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
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectInputStream = new ObjectInputStream(inputStream);

            byte[] buffer = new byte[1024];
            int bytes;

            Object obj;

            handler.obtainMessage(ManagerHandle, this).sendToTarget();
            Log.d(TAG, "I have sent myself to my manager.");

            while(true){
                try {
                    Log.d(TAG, "Reading object...");
                    obj = objectInputStream.readObject();

                    if(obj instanceof alexkotsc.wyred.peer.ChatMessage){
                        ChatMessage cm = (ChatMessage) obj;
                        handler.obtainMessage(ChatMessageObject, (ChatMessage) obj).sendToTarget();
                        Log.d(TAG, "Received: " + cm.getPeerPublicKey());
                    }

                    /*bytes = inputStream.read(buffer);

                    if (bytes == -1) {
                        break;
                    }*/

                    //Log.d(TAG, "Received: " + String.valueOf(buffer));

                    //handler.obtainMessage(ChatMessageBuffer, bytes, -1, buffer).sendToTarget();
                } catch (IOException e){
                    Log.e(TAG, "Disconnected", e);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
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

    /*public void write(byte[] buffer){
        try {
            outputStream.write(buffer);
            Log.d(TAG, "Finished writing: " + String.valueOf(buffer));
        } catch (IOException e){
            Log.e(TAG, "Exception during write", e);
        }
    }*/

    public void write(Object obj){
        try {
            Log.d(TAG, "Writing object to stream...");
            objectOutputStream.writeObject(obj);
            Log.d(TAG, "Finished writing object to stream.");
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }

    }
}

package alexkotsc.wyred;

import android.content.ContentValues;

/**
 * Created by AlexKotsc on 08-05-2015.
 */
public class ChatMessage {

    private boolean sender;
    private String message;
    private String peerPublicKey;
    private String date;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPeerPublicKey() {
        return peerPublicKey;
    }

    public void setPeerPublicKey(String peerPublicKey) {
        this.peerPublicKey = peerPublicKey;
    }


    public ChatMessage(){

    }
    public ChatMessage(boolean sender){
        this.sender = sender;
    }

    public void isSender(boolean sender){
        this.sender = sender;
    }

    public boolean isSender() {
        return sender;
    }

    public ContentValues generateInsertValues(){
        ContentValues contentValues = new ContentValues();

        if(isSender()) {
            contentValues.put("isSender", 1);
        } else {
            contentValues.put("isSender", 0);
        }

        contentValues.put("message", message);

        contentValues.put("publicKey", peerPublicKey);

        return contentValues;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

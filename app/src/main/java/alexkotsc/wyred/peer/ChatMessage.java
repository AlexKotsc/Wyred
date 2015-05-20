package alexkotsc.wyred.peer;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by AlexKotsc on 08-05-2015.
 */
public class ChatMessage implements Serializable{

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

    public String toJSON(){
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("sender", String.valueOf(sender));
            jsonObject.put("message", message);
            jsonObject.put("timestamp", date);
            jsonObject.put("publicKey", peerPublicKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static final ChatMessage fromJSON(JSONObject msg){
        ChatMessage cm = new ChatMessage();

        try {
            cm.setDate(msg.getString("timestamp"));
            cm.setMessage(msg.getString("message"));
            cm.setPeerPublicKey(msg.getString("publicKey"));
            cm.isSender(Boolean.parseBoolean(msg.getString("sender")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return cm;
    }


}

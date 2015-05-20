package alexkotsc.wyred.aodv.messages;

import java.util.HashMap;

import alexkotsc.wyred.aodv.IMessage;

/**
 * Created by AlexKotsc on 20-05-2015.
 */
public class RREQ implements IMessage {
    @Override
    public int getType() {
        return IMessage.RREQ;
    }

    private HashMap<String, Boolean> flags;

    public HashMap<String, Boolean> getFlags(){
        return flags;
    }

    public void setFlags (HashMap<String, Boolean> flags){
        this.flags = flags;
    }

    private int hopCount;

    public int getHopCount(){
        return hopCount;
    }

    public void setHopCount(int hopCount){
        this.hopCount = hopCount;
    }

    private int requestID;

    public int getRequestID(){
        return requestID;
    }

    public void setRequestID(int requestID){
        this.requestID = requestID;
    }

    private String destPublicKey;

    public String getDestPublicKey(){
        return destPublicKey;
    }

    public void setDestPublicKey(String destPublicKey){
        this.destPublicKey = destPublicKey;
    }

    private int destSeqNumber;

    public int getDestSeqNumber(){
        return destSeqNumber;
    }

    public void setDestSeqNumber(int destSeqNumber){
        this.destSeqNumber = destSeqNumber;
    }

    private String sendPublicKey;

    public String getSendPublicKey(){
        return sendPublicKey;
    }

    public void setSendPublicKey(String sendPublicKey){
        this.sendPublicKey = sendPublicKey;
    }

    private int sendSeqNumber;

    public int getSendSeqNumber(){
        return sendSeqNumber;
    }

    public void setSendSeqNumber(int sendSeqNumber){
        this.sendSeqNumber = sendSeqNumber;
    }
}

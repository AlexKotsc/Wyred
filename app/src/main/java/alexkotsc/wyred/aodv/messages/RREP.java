package alexkotsc.wyred.aodv.messages;

import java.util.HashMap;

import alexkotsc.wyred.aodv.IMessage;

/**
 * Created by AlexKotsc on 20-05-2015.
 */
public class RREP implements IMessage {
    @Override
    public int getType() {
        return IMessage.RREP;
    }

    private HashMap<String, Boolean> flags;

    public HashMap<String, Boolean> getFlags(){
        return flags;
    }

    public void setFlags (HashMap<String, Boolean> flags){
        this.flags = flags;
    }

    private int prefixSize;

    public int getPrefixSize(){
        return prefixSize;
    }

    public void setPrefixSize(int prefixSize){
        this.prefixSize = prefixSize;
    }

    private int hopCount;

    public int getHopCount(){
        return hopCount;
    }

    public void setHopCount(int hopCount){
        this.hopCount = hopCount;
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

    private long lifetime;

    public long getLifetime(){
        return lifetime;
    }

    public void setLifetime(long lifetime){
        this.lifetime = lifetime;
    }
}

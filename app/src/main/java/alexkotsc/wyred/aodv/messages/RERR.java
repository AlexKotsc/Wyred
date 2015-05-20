package alexkotsc.wyred.aodv.messages;

import java.util.HashMap;

import alexkotsc.wyred.aodv.IMessage;

/**
 * Created by AlexKotsc on 20-05-2015.
 */
public class RERR implements IMessage {
    @Override
    public int getType() {
        return IMessage.RRER;
    }

    private HashMap<String, Boolean> flags;

    public HashMap<String, Boolean> getFlags(){
        return flags;
    }

    public void setFlags (HashMap<String, Boolean> flags){
        this.flags = flags;
    }

    private int destCount;

    public int getDestCount(){
        return destCount;
    }

    public void setDestCount(int destCount){
        this.destCount = destCount;
    }

    private String deadPublicKey;

    public String getDeadPublicKey(){
        return deadPublicKey;
    }

    public void setDeadPublicKey(String deadPublicKey){
        this.deadPublicKey = deadPublicKey;
    }

    private int deadSeqNumber;

    public int getDeadSeqNumber(){
        return deadSeqNumber;
    }

    public void setDeadSeqNumber(int deadSeqNumber){
        this.deadSeqNumber = deadSeqNumber;
    }
}

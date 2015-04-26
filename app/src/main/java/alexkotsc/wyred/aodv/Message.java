package alexkotsc.wyred.aodv;

/**
 * Created by AlexKotsc on 22-04-2015.
 */
public interface Message {

    public Flag[] getFlags();
    public void setFlags(Flag[] flags);
    public int getHopCount();
    public int getID();
    public String getDestinationAddress();
    public int getDestinationSeq();
    public String getOriginatorAddress();
    public int getOriginatorSeq();
}

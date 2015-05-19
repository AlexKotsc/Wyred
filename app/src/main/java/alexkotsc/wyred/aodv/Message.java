package alexkotsc.wyred.aodv;

/**
 * Created by AlexKotsc on 22-04-2015.
 */
public interface Message {

    Flag[] getFlags();
    void setFlags(Flag[] flags);
    int getHopCount();
    int getID();
    String getDestinationAddress();
    int getDestinationSeq();
    String getOriginatorAddress();
    int getOriginatorSeq();
}

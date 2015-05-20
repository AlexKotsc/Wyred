package alexkotsc.wyred.aodv;

/**
 * Created by AlexKotsc on 22-04-2015.
 */
public interface IMessage {

    int RREQ = 101;
    int RREP = 102;
    int RREPACK = 104;
    int RRER = 103;

    /*Flag[] getFlags();
    void setFlags(Flag[] flags);
    int getHopCount();
    int getID();
    String getDestinationAddress();
    int getDestinationSeq();
    String getOriginatorAddress();
    int getOriginatorSeq();*/

    int getType();

}

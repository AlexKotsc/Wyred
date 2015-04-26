package alexkotsc.wyred.aodv;

/**
 * Created by AlexKotsc on 22-04-2015.
 */
public class RREQ implements Message {


    @Override
    public Flag[] getFlags() {
        return new Flag[0];
    }

    @Override
    public void setFlags(Flag[] flags) {

    }

    @Override
    public int getHopCount() {
        return 0;
    }

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public String getDestinationAddress() {
        return null;
    }

    @Override
    public int getDestinationSeq() {
        return 0;
    }

    @Override
    public String getOriginatorAddress() {
        return null;
    }

    @Override
    public int getOriginatorSeq() {
        return 0;
    }
}

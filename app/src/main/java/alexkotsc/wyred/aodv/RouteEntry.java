package alexkotsc.wyred.aodv;

import java.util.HashMap;

/**
 * Created by AlexKotsc on 19-05-2015.
 */
public class RouteEntry {

    private String destKey;
    private int destSeq;
    private HashMap<String, Integer> flags;
    private int hopCount;
    private String nextHop;
    //List of precursors
    private float TTL;

    public RouteEntry () {

    }

    public String getDestKey(){
        return destKey;
    }

    public int getDestSeq(){
        return destSeq;
    }

    public void setDestKey(String destKey) {
        this.destKey = destKey;
    }

    public void setDestSeq(int destSeq) {
        this.destSeq = destSeq;
    }

    public HashMap<String, Integer> getFlags() {
        return flags;
    }

    public void setFlags(HashMap<String, Integer> flags) {
        this.flags = flags;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public String getNextHop() {
        return nextHop;
    }

    public void setNextHop(String nextHop) {
        this.nextHop = nextHop;
    }

    public float getTTL() {
        return TTL;
    }

    public void setTTL(float TTL) {
        this.TTL = TTL;
    }
}

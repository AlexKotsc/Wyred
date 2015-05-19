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

}

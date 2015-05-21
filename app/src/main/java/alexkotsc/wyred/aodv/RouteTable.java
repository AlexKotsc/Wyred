package alexkotsc.wyred.aodv;

import android.util.Log;

import java.util.HashMap;

/**
 * Created by AlexKotsc on 19-05-2015.
 */
public class RouteTable {

    HashMap<String, RouteEntry> entries;
    private static final String TAG = "RouteTable";

    public RouteTable(){
        entries = new HashMap<>();
    }

    public void updateEntry(RouteEntry newEntry){
        if(entries.containsKey(newEntry.getDestKey())){
            //Update entry

            RouteEntry currentEntry = entries.get(newEntry.getDestKey());

            if(newEntry.getDestSeq()!=0){

            }

            if(newEntry.getDestSeq()<1){
                entries.put(newEntry.getDestKey(), newEntry);
                return;
            }

            if(newEntry.getDestSeq() > currentEntry.getDestSeq()){
                entries.put(newEntry.getDestKey(), newEntry);
                return;
            }

        } else {
            //New entry
            entries.put(newEntry.getDestKey(), newEntry);
        }
    }

    public boolean entryExists(String destination){
        return (entries.containsKey(destination));
    }

    public void addEntry(RouteEntry e){
        if(entries.containsKey(e.getDestKey())){
            Log.e(TAG, "Entry already in table.");

            //TODO Maybe update instead?
        } else {
            entries.put(e.getDestKey(), e);
        }
    }

    public int getEntryCount(){
        return entries.size();
    }



}

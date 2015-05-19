package alexkotsc.wyred.peer;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by AlexKotsc on 21-04-2015.
 */
public class Peer implements IPeer, Parcelable {

    private String peerName = null;
    private String publicKey = null;
    private WifiP2pDevice wifiP2pDevice;

    public Peer(){
        peerName = "testPeer";
        publicKey = "testPublicKey";
    }

    @Override
    public String getDeviceName() {
        return wifiP2pDevice.deviceName;
    }

    @Override
    public String getDeviceAddress() {
        return wifiP2pDevice.deviceAddress;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public boolean isConnected(){
        if(wifiP2pDevice.status!=WifiP2pDevice.CONNECTED) return false;
        return publicKey != null;
    }

    @Override
    public WifiP2pDevice getWifiP2pDevice() {
        return wifiP2pDevice;
    }

    @Override
    public void setWifiP2pDevice(WifiP2pDevice wifiP2pDevice) {
        this.wifiP2pDevice = wifiP2pDevice;
    }

    protected Peer(Parcel in) {
        peerName = in.readString();
        publicKey = in.readString();
        wifiP2pDevice = in.readParcelable(WifiP2pDevice.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(peerName);
        dest.writeString(publicKey);
        dest.writeParcelable(wifiP2pDevice, flags);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Peer> CREATOR = new Parcelable.Creator<Peer>() {
        @Override
        public Peer createFromParcel(Parcel in) {
            return new Peer(in);
        }

        @Override
        public Peer[] newArray(int size) {
            return new Peer[size];
        }
    };

    @Override
    public boolean equals(Object o) {

        Peer otherPeer = (Peer) o;

        return getDeviceName().equals(otherPeer.getDeviceName());
    }
}

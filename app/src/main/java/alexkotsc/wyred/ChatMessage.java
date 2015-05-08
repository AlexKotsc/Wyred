package alexkotsc.wyred;

/**
 * Created by AlexKotsc on 08-05-2015.
 */
public class ChatMessage {

    private boolean sender;

    public ChatMessage(){

    }
    public ChatMessage(boolean sender){
        this.sender = sender;
    }

    public void isSender(boolean sender){
        this.sender = sender;
    }

    public boolean isSender() {
        return sender;
    }
}

package alexkotsc.wyred.aodv.messages;

import alexkotsc.wyred.aodv.IMessage;

/**
 * Created by AlexKotsc on 20-05-2015.
 */
public class RREPACK implements IMessage {
    @Override
    public int getType() {
        return IMessage.RREPACK;
    }
}

package alexkotsc.wyred.peer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import alexkotsc.wyred.R;

/**
 * Created by AlexKotsc on 08-05-2015.
 */
public class MessageAdapter extends ArrayAdapter<ChatMessage> {

    List<ChatMessage> messages = null;
    Context context;


    public MessageAdapter(Context context, int resource, List<ChatMessage> objects) {
        super(context, resource, objects);
        this.context = context;
        messages = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        View view = null;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ChatMessage currentMessage = messages.get(position);

        if(currentMessage.isSender()){
            view = layoutInflater.inflate(R.layout.message_sender_view, null);
        } else {
            view = layoutInflater.inflate(R.layout.message_receiver_view, null);
        }

        TextView messageText = (TextView) view.findViewById(R.id.messageTextView);
        messageText.setText(currentMessage.getMessage());

        TextView dateText = (TextView) view.findViewById(R.id.messageTimeView);
        dateText.setText(currentMessage.getDate());

        return view;
    }
}

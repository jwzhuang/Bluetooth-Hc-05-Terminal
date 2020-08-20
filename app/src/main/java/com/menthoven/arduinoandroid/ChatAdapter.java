package com.menthoven.arduinoandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.menthoven.arduinoandroid.databinding.ItemMessageBinding;

/**
 * Created by da Ent on 28/11/2015.
 */
public class ChatAdapter extends ArrayAdapter<ChatMessage> {

    // View lookup cache
    static class ViewHolder {

        ItemMessageBinding binding;
        public ViewHolder(View view) {
            binding = ItemMessageBinding.bind(view);
        }
    }

    public ChatAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ChatMessage chatMessage = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_message, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        // Populate the data into the template view using the data object
        if (BluetoothActivity.showTimeIsChecked) {
            viewHolder.binding.timeTextView.setText(chatMessage.getTime());
        } else {
            viewHolder.binding.timeTextView.setText("");
        }
        viewHolder.binding.deviceTextView.setText(chatMessage.getDevice().concat(":"));
        viewHolder.binding.messageTextView.setText(chatMessage.getMessage());
        // Return the completed to render on screen
        return convertView;
    }
}
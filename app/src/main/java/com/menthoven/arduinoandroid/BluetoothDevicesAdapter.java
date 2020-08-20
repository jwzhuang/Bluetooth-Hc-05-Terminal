package com.menthoven.arduinoandroid;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.menthoven.arduinoandroid.databinding.ItemDeviceBinding;


/**
 * Created by da Ent on 28/11/2015.
 */
public class BluetoothDevicesAdapter extends ArrayAdapter<BluetoothDevice> {

    // View lookup cache
    static class ViewHolder {
        public ItemDeviceBinding binding;
        public ViewHolder(View view) {
            binding = ItemDeviceBinding.bind(view);
        }
    }

    public BluetoothDevicesAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BluetoothDevice device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_device, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        viewHolder.binding.deviceName.setText(device.getName());
        viewHolder.binding.deviceAddress.setText(device.getAddress());
        // Return the completed to render on screen
        return convertView;
    }
}
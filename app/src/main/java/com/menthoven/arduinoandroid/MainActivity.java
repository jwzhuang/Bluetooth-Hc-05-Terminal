package com.menthoven.arduinoandroid;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import com.menthoven.arduinoandroid.databinding.ActivityMainBinding;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_LOCATION_PERMISSION = 0;
    BluetoothAdapter bluetoothAdapter;

    BluetoothDevicesAdapter bluetoothDevicesAdapter;
    private ActivityMainBinding binding;

    private void enableBluetooth() {
        setStatus("Enabling Bluetooth");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        setStatus("None");

        bluetoothDevicesAdapter = new BluetoothDevicesAdapter(this);

        binding.content.devicesListView.setAdapter(bluetoothDevicesAdapter);
        binding.content.devicesListView.setEmptyView(binding.content.empty.emptyListItem);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {

            Log.e(Constants.TAG, "Device has no bluetooth");
            new AlertDialog.Builder(MainActivity.this)
                    .setCancelable(false)
                    .setTitle("No Bluetooth")
                    .setMessage("Your device has no bluetooth")
                    .setPositiveButton("Close app", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            Log.d(Constants.TAG, "App closed");
                            finish();
                        }
                    }).show();

        }

        binding.content.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter.isEnabled()) {
                    // Bluetooth enabled
                    startSearching();
                } else {

                    enableBluetooth();
                }
            }
        });


        binding.content.devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                setStatus("Asking to connect");
                final BluetoothDevice device = bluetoothDevicesAdapter.getItem(position);

                new AlertDialog.Builder(MainActivity.this)
                        .setCancelable(false)
                        .setTitle("Connect")
                        .setMessage("Do you want to connect to: " + device.getName() + " - " + device.getAddress())
                        .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                Log.d(Constants.TAG, "Opening new Activity");
                                bluetoothAdapter.cancelDiscovery();
                                binding.toolbarProgressBar.setVisibility(View.INVISIBLE);

                                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);

                                intent.putExtra(Constants.EXTRA_DEVICE, device);

                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                setStatus("Cancelled connection");
                                Log.d(Constants.TAG, "Cancelled ");
                            }
                        }).show();
            }
        });
    }

    @Override protected void onStart() {
        super.onStart();

        Log.d(Constants.TAG, "Registering receiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.checkPermission();
    }

    @Override protected void onStop() {
        super.onStop();
        Log.d(Constants.TAG, "Receiver unregistered");
        unregisterReceiver(mReceiver);
    }


    private void setStatus(String status) {
        binding.toolbar.setSubtitle(status);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startSearching();
            } else {
                setStatus("Error");
                Snackbar.make(binding.coordinatorLayoutMain, "Failed to enable bluetooth", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                enableBluetooth();
                            }
                        }).show();
            }
        }

    }

    private void checkPermission(){
        final boolean isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (isGranted){return;}
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    private void startSearching() {
        if (bluetoothAdapter.startDiscovery()) {
            binding.toolbarProgressBar.setVisibility(View.VISIBLE);
            setStatus("Searching for devices");
        } else {
            setStatus("Error");
            Snackbar.make(binding.coordinatorLayoutMain, "Failed to start searching", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Try Again", new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            startSearching();
                        }
                    }).show();
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                device.fetchUuidsWithSdp();

                if (bluetoothDevicesAdapter.getPosition(device) == -1) {
                    // -1 is returned when the item is not in the adapter
                    bluetoothDevicesAdapter.add(device);
                    bluetoothDevicesAdapter.notifyDataSetChanged();
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                binding.toolbarProgressBar.setVisibility(View.INVISIBLE);
                setStatus("None");

            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Snackbar.make(binding.coordinatorLayoutMain, "Bluetooth turned off", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Turn on", new View.OnClickListener() {
                                    @Override public void onClick(View v) {
                                        enableBluetooth();
                                    }
                                }).show();
                        break;
                }
            }
        }
    };
}



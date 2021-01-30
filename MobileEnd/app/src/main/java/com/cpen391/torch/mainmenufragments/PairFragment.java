package com.cpen391.torch.mainmenufragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


import com.cpen391.torch.R;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class PairFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 10;
    private ArrayAdapter<String> bluetoothArrayAdapter;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    private BluetoothSocket bluetoothSocket;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                bluetoothArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                bluetoothArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_pair, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        Button bluetoothButton = view.findViewById(R.id.pair_button);
        bluetoothButton.setOnClickListener(v -> findDevice());

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            new AlertDialog.Builder(Objects.requireNonNull(this.getContext()))
                    .setTitle(R.string.UI_warning)
                    .setMessage("Please make sure you have turned on bluetooth before proceeding")
                    .setPositiveButton(R.string.OK, ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .show();
        }

        bluetoothArrayAdapter = new ArrayAdapter<String>(Objects.requireNonNull(this.getContext()), android.R.layout.simple_list_item_1);
        bluetoothArrayAdapter.add("broad_cast_addr_testing\nFF:FF:FF:FF:FF:FF");
        ListView deviceListView = view.findViewById(R.id.devices_list_view);
        deviceListView.setAdapter(bluetoothArrayAdapter);
        deviceListView.setOnItemClickListener((adapterView, view1, i, l) -> {
            String info = ((TextView)view1).getText().toString();
            connectToDevice(info);
        });


        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        requireActivity().registerReceiver(receiver, filter);
    }

    private void findDevice() {
        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                new AlertDialog.Builder(Objects.requireNonNull(this.getContext()))
                        .setTitle(R.string.UI_warning)
                        .setMessage("Please make sure you have turned on bluetooth before proceeding")
                        .setPositiveButton(R.string.OK, ((dialogInterface, i) -> dialogInterface.dismiss()))
                        .show();
            }
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Toast.makeText(this.getContext(), "Discovery canceled.", Toast.LENGTH_LONG).show();
        } else {
            bluetoothArrayAdapter.clear();
            bluetoothAdapter.startDiscovery();
            Toast.makeText(this.getContext(), "Discovery started.", Toast.LENGTH_LONG).show();

        }
    }

    private void connectToDevice(String info) {
        // Get the device MAC address, which is the last 17 chars in the View
        final String address = info.substring(info.length() - 17);
        final String name = info.substring(0, info.length() - 17);

        new Thread()
        {
            public void run() {
                boolean fail = false;

                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

                try {
                    bluetoothSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    fail = true;
                    Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                            Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show());
                }
                // Establish the Bluetooth socket connection.
                try {
                    bluetoothSocket.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        bluetoothSocket.close();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                                Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show());
                    }
                }
                if(!fail) {
                    Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                            Toast.makeText(getContext(), name + " pairing succeed", Toast.LENGTH_SHORT).show());
                    try {
                        bluetoothSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregister the ACTION_FOUND receiver.
        Objects.requireNonNull(getActivity()).unregisterReceiver(receiver);
    }
}
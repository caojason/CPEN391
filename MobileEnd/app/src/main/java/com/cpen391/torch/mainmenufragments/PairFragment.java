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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


import com.cpen391.torch.R;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PairFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 10;
    private ArrayAdapter<String> bluetoothArrayAdapter;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    private BluetoothSocket bluetoothSocket;
    private TextView textView;
    private String selectedAddr = "";
    private String pin = "";


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    bluetoothArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    bluetoothArrayAdapter.notifyDataSetChanged();
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                textView.setText("Search finished");
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
        ListView deviceListView = view.findViewById(R.id.devices_list_view);
        deviceListView.setAdapter(bluetoothArrayAdapter);
        deviceListView.setOnItemClickListener((adapterView, view1, i, l) -> {
            String info = ((TextView)view1).getText().toString();
            connectToDevice(info);
        });


        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        requireActivity().registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        requireActivity().registerReceiver(receiver, filter);

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                bluetoothArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            bluetoothArrayAdapter.add("broad_cast_addr_testing\nFF:FF:FF:FF:FF:FF");
        }

        textView = view.findViewById(R.id.search_finished_text);
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
            bluetoothAdapter.startDiscovery();
            Toast.makeText(this.getContext(), "Discovery started. If device is not found in 10s, please pair the device through system bluetooth", Toast.LENGTH_LONG).show();

        }
    }

    private void connectToDevice(String info) {
        // Get the device MAC address, which is the last 17 chars in the View
        final String address = info.substring(info.length() - 17);
        selectedAddr = address;
        final String name = info.substring(0, info.length() - 17);

        LinearLayout enterPinLayout = new LinearLayout(this.getContext());
        enterPinLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(16, 16, 16, 16);

        TextView enterPinText = new TextView(this.getContext());
        enterPinText.setText("Please enter pin, default should be 1234");
        enterPinText.setLayoutParams(layoutParams);
        enterPinLayout.addView(enterPinText);


        EditText pinInput = new EditText(this.getContext());
        pinInput.setLayoutParams(layoutParams);
        pinInput.setHint("e.g. 1234");
        pinInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                pin = editable.toString();
            }
        });
        enterPinLayout.addView(pinInput);

        Thread t = new Thread()
        {
            public void run() {
                boolean fail = false;

                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                device.setPin(pin.getBytes(StandardCharsets.UTF_8));

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
        };

        new AlertDialog.Builder(this.getContext())
                .setTitle("Connecting to device")
                .setView(enterPinLayout)
                .setPositiveButton(R.string.OK, (dialogInterface, i) -> {dialogInterface.dismiss(); t.start();})
                .show();


    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregister the ACTION_FOUND receiver.
//        Objects.requireNonNull(getActivity()).unregisterReceiver(receiver);
    }
}
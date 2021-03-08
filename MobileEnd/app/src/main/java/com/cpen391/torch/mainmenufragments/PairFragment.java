package com.cpen391.torch.mainmenufragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.cpen391.torch.OtherUtils;
import com.cpen391.torch.R;
import com.cpen391.torch.StoreInfoActivity;
import com.cpen391.torch.data.StoreInfo;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PairFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 10;
    private ArrayAdapter<String> bluetoothArrayAdapter;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    private BluetoothSocket bluetoothSocket;
    private TextView searchFinishedAlertText;

    private String selectedAddr = "";



    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                if (OtherUtils.stringIsNullOrEmpty(device.getName()) || device.getName().equals(getString(R.string.NULL)))
                    return;
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    bluetoothArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    bluetoothArrayAdapter.notifyDataSetChanged();
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                searchFinishedAlertText.setText(R.string.UI_search_finished);
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
            return;
        }

        bluetoothArrayAdapter = new ArrayAdapter<String>(Objects.requireNonNull(this.getContext()), android.R.layout.simple_list_item_1);
        ListView deviceListView = view.findViewById(R.id.devices_list_view);
        deviceListView.setAdapter(bluetoothArrayAdapter);
        deviceListView.setOnItemClickListener((adapterView, view1, i, l) -> {
            String info = ((TextView) view1).getText().toString();
            connectToDevice(info);
        });


        // Register for broadcasts when a device is discovered.
        requireActivity().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        requireActivity().registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                bluetoothArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            bluetoothArrayAdapter.add("broad_cast_addr_testing\nFF:FF:FF:FF:FF:FF");
        }

        searchFinishedAlertText = view.findViewById(R.id.search_finished_text);
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
            searchFinishedAlertText.setText("");
            Toast.makeText(this.getContext(), "Discovery started. If device is not found in 10s, please pair the device through system bluetooth", Toast.LENGTH_LONG).show();

        }
    }

    private void connectToDevice(String info) {
        // Get the device MAC address, which is the last 17 chars in the View
        final String address = info.substring(info.length() - 17);
        selectedAddr = address;
        final String name = info.substring(0, info.length() - 17);

        Thread t = new Thread() {
            public void run() {
                // check if the address has been used on the server, if so, do not proceed
                SharedPreferences sp = getContext().getSharedPreferences(getString(R.string.curr_login_user), Context.MODE_PRIVATE);
                String uid = sp.getString(getString(R.string.UID), "");
                String url = getString(R.string.BASE_URL) + getString(R.string.check_is_store_owner) + "?macAddr=" + selectedAddr;
                String result = OtherUtils.readFromURL(url);
                if (!result.equals("\"\"") && !result.contains(uid)) {
                    Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                            Toast.makeText(getContext(), "the address has been registered by someone", Toast.LENGTH_SHORT).show());
                    return;
                }

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
                if (!fail) {
                    Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                            Toast.makeText(getContext(), name + " pairing succeed", Toast.LENGTH_SHORT).show());
                    try {
                        bluetoothSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setupStoreInfo();
                }
            }
        };

        t.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private void setupStoreInfo() {
        Intent i = new Intent(this.getActivity(), StoreInfoActivity.class);
        i.putExtra(getString(R.string.STORE_INFO), getStoreInfo());
        i.putExtra(getString(R.string.MAC_ADDR), selectedAddr);
        startActivity(i);
    }

    private String getStoreInfo() {
        SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.curr_login_user), Context.MODE_PRIVATE);
        String favoriteListStr = sp.getString(getString(R.string.FAVORITES), "");
        String userId = sp.getString(getString(R.string.UID), "");
        Gson g = new Gson();
        try {
            JSONArray favorites = new JSONArray(favoriteListStr);
            for (int  i = 0; i < favorites.length(); i++) {
                String str = favorites.getString(i);
                StoreInfo info = g.fromJson(str, StoreInfo.class);
                if (info.getStoreOwnerId().equals(userId) && info.getMacAddr().equals(selectedAddr)) {
                    return str;
                }
            }
        } catch (Exception e) {
            assert favoriteListStr != null;
            assert userId != null;
            if (favoriteListStr.contains(userId)) {
                return favoriteListStr;
            } else {
                return "";
            }
        }
        return "";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregister the ACTION_FOUND receiver.
        try {
            Objects.requireNonNull(getActivity()).unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.d("D", e.getMessage() + "");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the ACTION_FOUND receiver.
        try {
            Objects.requireNonNull(getActivity()).unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.d("D", e.getMessage() + "");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bluetoothAdapter != null) {
            Objects.requireNonNull(getActivity()).registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            Objects.requireNonNull(getActivity()).registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        }
    }
}
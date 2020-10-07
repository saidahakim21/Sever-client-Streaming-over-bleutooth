package com.ahakim.said.videoredefuser;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BluetoothConnectionActivity extends AppCompatActivity
{
    private static final String TAG = "Client";
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> itemsAdapter;
    private ArrayList<BluetoothDevice> discoveredList;
    BroadcastReceiver mReceiver;
    private ListView newDevicesListView;
    private Button scanButton;
    private ProgressBar progressBar;

    private AdapterView.OnItemClickListener onDeviceClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3)
        {
            bluetoothAdapter.cancelDiscovery();
            String info = ((TextView) v).getText().toString();
            Intent intent = new Intent(BluetoothConnectionActivity.this, ClientPlayerActivity.class);
            intent.putExtra("SELECTED DEVICE", discoveredList.get(arg2));
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.network_discovery_layout);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        discoveredList = new ArrayList<>();

        itemsAdapter = new ArrayAdapter<>(this, R.layout.device_view_holder);
        newDevicesListView = findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(itemsAdapter);
        newDevicesListView.setOnItemClickListener(onDeviceClickListener);

        mReceiver =  new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    discoveredList.add(device);
                    itemsAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    progressBar.setVisibility(View.GONE);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        progressBar = findViewById(R.id.progressBar);

        scanButton = findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                discover();
            }
        });
    }

    private void discover()
    {
        if (bluetoothAdapter.isDiscovering())
        {
            bluetoothAdapter.cancelDiscovery();
            itemsAdapter.clear();
            discoveredList.clear();
        }

        bluetoothAdapter.startDiscovery();
        progressBar.setVisibility(View.VISIBLE);

    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (bluetoothAdapter != null)
        {
            bluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(mReceiver);
    }
}

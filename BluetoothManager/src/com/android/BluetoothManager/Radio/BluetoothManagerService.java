package com.android.BluetoothManager.Radio;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;

public class BluetoothManagerService extends Service {

	Connection connection;

	BluetoothManagerApplication bluetooth_manager;

	private int server_start_status;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		bluetooth_manager = (BluetoothManagerApplication) getApplication();
		connection = new Connection(this);
		//makeDeviceDiscoverable();
	}

	public void makeDeviceDiscoverable() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        
        startActivity(discoverableIntent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		connection = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		server_start_status = connection.startServer();
		bluetooth_manager.connection = connection;
		return START_STICKY;
	}

}

package com.android.BluetoothManager.Radio;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

/*
 * This class receives data from the routing layer and sends it to other devices over the radio layer
 */
public class RadioPacketReceiver extends BroadcastReceiver{

	private final String TAG = "RadioPacketReceiver";
	BluetoothManagerApplication bluetooth_manager;
	
	public RadioPacketReceiver(BluetoothManagerApplication bluetooth_manager) {
		this.bluetooth_manager = bluetooth_manager;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String device = intent.getStringExtra("device");
		String msg = intent.getStringExtra("msg");
		try {
			if(device == null){
				bluetooth_manager.connection.broadcastMessage(msg);
				return;
			}
			bluetooth_manager.connection.sendMessageToDestination(device, msg);
		} catch (RemoteException e) {
			Log.d(TAG, "Unable to send msg to :"+device);
		}
	}

}

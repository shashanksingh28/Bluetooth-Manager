package com.android.BluetoothManager.Application;

import java.util.HashMap;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.util.Log;

import com.android.BluetoothManager.Radio.BluetoothManagerService;
import com.android.BluetoothManager.Radio.Connection;
import com.android.BluetoothManager.Radio.RadioPacketReceiver;
import com.android.BluetoothManager.Routing.PacketHandler;
import com.android.BluetoothManager.Routing.PacketHandlerHelper;
import com.android.BluetoothManager.Routing.RouteTable;
import com.android.BluetoothManager.UI.R;
import com.android.BluetoothManager.UI.UIPacketReceiver;

/*
 * Contains global adapters and other state variables required
 * Initializes the service.
 * All the registration of the BroadcastReceivers are done here.
 */
public class BluetoothManagerApplication extends Application {

	private static final String TAG = "BluetoothManagerApplication";

	// Packet Reciever object
	PacketHandler packet_receiver;

	// Receiver for UI packets.
	UIPacketReceiver ui_packet_receiver;
	
	// Receiver for packets to be send across the Radio layer.
	RadioPacketReceiver radio_packet_receiver;

	public BluetoothManagerService bluetooth_manager_service;

	public Connection connection;

	public RouteTable route_table;

	public PacketHandlerHelper packet_handler_helper;

	@Override
	public void onCreate() {
		super.onCreate();

		// getting the intent strings from the XML file.
		Log.d(TAG,"Application OnCreate");
		String UI_TO_ROUTING = getResources().getString(R.string.UI_TO_ROUTING);
		String RADIO_TO_ROUTING = getResources().getString(
				R.string.RADIO_TO_ROUTING);
		String ROUTING_TO_UI = getResources().getString(R.string.ROUTING_TO_UI);
		String ROUTING_TO_RADIO = getResources().getString(
				R.string.ROUTING_TO_RADIO);

		// Here starts the registration of the listeners for intents.

		// Instantiate the PacketReciever and registering it to listen
		packet_receiver = new PacketHandler(this);
		IntentFilter r = new IntentFilter();
		r.addAction(UI_TO_ROUTING);
		r.addAction(RADIO_TO_ROUTING);
		registerReceiver(packet_receiver, r);

		// Instantiate the UI Receiver and registering it.
		ui_packet_receiver = new UIPacketReceiver(this);
		IntentFilter i = new IntentFilter();
		i.addAction(ROUTING_TO_UI);
		registerReceiver(ui_packet_receiver, i);

		
		// Instantiate the Radio layer receiver and register it.
		radio_packet_receiver = new RadioPacketReceiver(this);
		IntentFilter p = new IntentFilter();
		p.addAction(ROUTING_TO_RADIO);
		registerReceiver(radio_packet_receiver, p);
		
		
		// initialize the route table on startup
		route_table = new RouteTable(this);

		// initialize the packet helper
		packet_handler_helper = new PacketHandlerHelper(this);

		startService(new Intent(this, BluetoothManagerService.class));
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		stopService(new Intent(this, BluetoothManagerService.class));
	}

	public String getSelfAddress() {
		try {
			return connection.getAddress();
		} catch (RemoteException e) {
			Log.d(TAG, " ++ Unable to retrieve selfAddress");
		}
		return null;
	}
	
	public HashMap<String, String> getConnectableDevices(){
		return connection.getConnectableDevices();
	}

}

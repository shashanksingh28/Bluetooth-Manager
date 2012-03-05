package com.android.BluetoothManager.Application;

import java.util.HashMap;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.android.BluetoothManager.Radio.BluetoothManagerService;
import com.android.BluetoothManager.Radio.Connection;
import com.android.BluetoothManager.Radio.RadioPacketReceiver;
import com.android.BluetoothManager.Routing.PacketHandlerService;
import com.android.BluetoothManager.Routing.RoutingPacketReceiver;
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

	// Packet receiver for routing layer
	RoutingPacketReceiver packet_receiver;
	
	PacketHandlerService routing_thread;

	// Packet receiver for UI layer
	public UIPacketReceiver ui_packet_receiver;

	// Packet receiver for radio layer
	RadioPacketReceiver radio_packet_receiver;

	public BluetoothManagerService bluetooth_manager_service;

	public Connection connection;

	public RouteTable route_table;

	@Override
	public void onCreate() {
		super.onCreate();

		// getting the intent strings from the XML file.
		Log.d(TAG, "Application OnCreate");
		String UI_TO_ROUTING = getResources().getString(R.string.UI_TO_ROUTING);
		String RADIO_TO_ROUTING = getResources().getString(
				R.string.RADIO_TO_ROUTING);
		String ROUTING_TO_UI = getResources().getString(R.string.ROUTING_TO_UI);
		String ROUTING_TO_RADIO = getResources().getString(
				R.string.ROUTING_TO_RADIO);

		// Here starts the registration of the listeners for intents.

		// Instantiate the PacketReciever and registering it to listen
		packet_receiver = new RoutingPacketReceiver(this);
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
		
		routing_thread=new PacketHandlerService();
		new Thread(routing_thread).start();
		
		startService(new Intent(this, BluetoothManagerService.class));

		// Testing UI via Stubs
		Thread ui_stub = new Thread(new UIStub());
		ui_stub.start();

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

	public HashMap<String, String> getConnectableDevices() {
		return connection.getConnectableDevices();
	}

	// This class iS SOLELY for testing Chat UI
	private class UIStub implements Runnable {

		@Override
		public void run() {
			pause(5);
			mSendIntent("123", "aru", "chat,hello :D");
			pause(5);
			mSendIntent("123", "aru", "chat,hello hi :D");
			pause(5);
			mSendIntent("321", "arihant", "chat,HAHAHAHA");
			pause(5);
			mSendIntent("123", "aru", "chat,hello sdfsdf");
			pause(5);
			mSendIntent("789", "pik", "chat,Arihant");
			pause(5);
			mSendIntent("789", "pik", "chat,Arihant123");
			pause(5);
			mSendIntent("123", "aru", "chat,hello hi :D");
			pause(10000);
			mSendIntent("888", "God", "chat,This is God !");
		}

		public void pause(int seconds) {
			try {
				Thread.sleep(seconds);
			} catch (InterruptedException e) {
				Log.d(TAG, "Error in Sleep: " + e.getMessage());
			}
		}

		public void mSendIntent(String device, String name, String msg) {
			Intent i = new Intent();
			i.setAction(getResources().getString(R.string.ROUTING_TO_UI));
			i.putExtra("device", device);
			i.putExtra("name", name);
			i.putExtra("msg", msg);
			Log.d(TAG, "Generating Intent: device-" + device + " name-" + name
					+ " msg-" + msg);
			sendBroadcast(i);
		}

	}
}

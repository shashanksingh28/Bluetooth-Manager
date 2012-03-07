package com.android.BluetoothManager.Routing;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;
import com.android.BluetoothManager.Routing.Packet_types.RadioPacket;
import com.android.BluetoothManager.Routing.Packet_types.UIPacket;
import com.android.BluetoothManager.UI.R;

/* 
 * Class that will receive packets, distinguish their type
 * and call respective function of the PacketHandler.
 * The objects will be pushed to two queues, one for the packets from UI
 * and one for packets from Radio. A separate thread will process them
 */
public class RoutingPacketReceiver extends BroadcastReceiver {

	private static final String TAG = "RoutingPacketReceiver";

	public static Queue<UIPacket> objectsFromUI;
	public static Queue<RadioPacket> objectsFromRadio;
	BluetoothManagerApplication bluetooth_manager;

	public RoutingPacketReceiver(BluetoothManagerApplication bluetooth_manager) {
		this.bluetooth_manager = bluetooth_manager;
		objectsFromUI = new LinkedList<UIPacket>();
		objectsFromRadio = new LinkedList<RadioPacket>();
	}

	/*
	 * Function called when protocol layer receives a packet. First check which
	 * layer sent the data and push to the Queue accordingly
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		String device = intent.getExtras().getString("device");
		String msg = intent.getExtras().getString("msg");

		if (intent.getAction().equals(
				bluetooth_manager.getResources().getString(
						R.string.UI_TO_ROUTING))) {
			UIPacket ui_packet = new UIPacket(device, msg);
			Log.d(TAG, "Pushing down msg: " + msg + " to " + device
					+ " on queue");
			objectsFromUI.add(ui_packet);
			Log.d(TAG, "Size:" + objectsFromUI.size());
		} else if (intent.getAction().equals(
				bluetooth_manager.getResources().getString(
						R.string.RADIO_TO_ROUTING))) {
			RadioPacket radio_packet = new RadioPacket(device, msg);
			Log.d(TAG, "Pushing up msg: " + msg + " from " + device
					+ " on queue");
			objectsFromRadio.add(radio_packet);
		}

	}

	public static void printQueues() {
		int ui_queue_size = objectsFromUI.size();
		int radio_queue_size = objectsFromRadio.size();

		Iterator itr;
		if (ui_queue_size > 0) {
			Log.d(TAG, "Size of UI Queue:" + ui_queue_size);
			itr = objectsFromUI.iterator();
			Log.d(TAG, "Printing Objects from UI in queue");
			for (int i = 1; itr.hasNext(); i++) {
				Log.d(TAG, i + ((UIPacket) itr.next()).toString());
			}
		}
		if (radio_queue_size > 0) {
			Log.d(TAG, "Size of Radio Queue:" + radio_queue_size);
			Log.d(TAG, "Printing Objects from radio in queue");
			itr = objectsFromRadio.iterator();
			for (int i = 1; itr.hasNext(); i++) {
				Log.d(TAG, i + ((RadioPacket) itr.next()).toString());
			}
		}
	}
}
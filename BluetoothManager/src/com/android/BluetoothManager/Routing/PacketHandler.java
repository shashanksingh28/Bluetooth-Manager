package com.android.BluetoothManager.Routing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.BluetoothManager.Application.BluetoothManagerApplication;
import com.android.BluetoothManager.Routing.Packet_types.Route_Message;

/* 
 * Class that will recieve packets, distinguish their type
 * and call respective function of the PacketHandler
 * 
 */
public class PacketHandler extends BroadcastReceiver {

	private static final String TAG = "PacketHandler";

	BluetoothManagerApplication bluetooth_manager;

	// Integers for types of packets according to DYMO protocol
	public static final int RREQ = 1;
	public static final int RREP = 2;
	public static final int RERR = 3;
	public static final int DATA = 4;

	public PacketHandler(BluetoothManagerApplication bluetooth_manager) {
		this.bluetooth_manager = bluetooth_manager;
	}

	/*
	 * Function called when protocol layer receives a packet.first check from
	 * which layer sent the data and act accordingly.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		String layer = intent.getExtras().getString("layer");
		String device = intent.getExtras().getString("device");
		String msg = intent.getExtras().getString("msg");

		if (layer.equals("radio")) {
			int type = getType(msg);
			switch (type) {
			case RREQ:
				Log.d(TAG,"RREQ received by routing. Now processing.");
				bluetooth_manager.packet_handler_helper
						.parseRREQ(device, msg);

				break;

			case RREP:
				Log.d(TAG,"RREP received by routing. Now processing.");
				bluetooth_manager.packet_handler_helper
						.parseRREP(device, msg);
				break;

			case RERR:
				bluetooth_manager.packet_handler_helper
						.parseRERR(device, msg);
				break;

			case DATA:
				bluetooth_manager.packet_handler_helper
						.parseData(device, msg);
				break;

			default:
				break;
			}
		} else if (layer.equals("UI")) {
			/*
			 * First check if route already exists for a given destination. If
			 * yes, then send the data to that device. If no, then broadcast a
			 * RREQ packet.
			 */

			
			Log.d(TAG, "Checking if route exist.");

			Route isPresent = bluetooth_manager.route_table.getRouteToDest(device);

			if (isPresent == null) {
				Log.d(TAG, "Route for " + device
						+ "doesn't exists. Sending RREQ");
				Route_Message rreq = new Route_Message(PacketHandler.RREQ,
						RouteTable.getSequenceNumber(),
						bluetooth_manager.getSelfAddress(), device, 1);
				bluetooth_manager.route_table.broadcastRREQ(rreq);
				Log.d(TAG, "RREQ broadcasted");

			} else {
				bluetooth_manager.route_table.forwardMessage(
						isPresent.getNext_hop(), msg);
			}
		}

	}

	/*
	 * Function which will return the type message(packet) which is passed to it
	 */
	int getType(String msg) {
		return Integer.parseInt(msg.charAt(0) + "");
	}
}

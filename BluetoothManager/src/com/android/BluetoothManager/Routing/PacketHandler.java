package com.android.BluetoothManager.Routing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.BluetoothManager.Radio.BluetoothManagerService;
import com.android.BluetoothManager.Routing.Packet_types.Route_Message;

/* 
 * Class that will recieve packets, distinguish their type
 * and call respective function of the PacketHandler
 * 
 * String for this receiver: 
 * PACKET_RECEIVE_INTENT_ACTION = "com.android.BluetoothManager.PACKET_RECEIVED";
 * 
 */
public class PacketHandler extends BroadcastReceiver {

	private static final String TAG = "PacketHandler";
	
	// Integers for types of packets according to DYMO protocol
	public static final int RREQ = 1;
	public static final int RREP = 2;
	public static final int RERR = 3;
	public static final int DATA = 4;

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
				PacketHandlerHelper.processRREQ(device, msg);
				break;

			case RREP:
				PacketHandlerHelper.processRREP(device, msg);
				break;

			case RERR:
				PacketHandlerHelper.processRERR(device, msg);
				break;

			case DATA:
				PacketHandlerHelper.processData(device, msg);
				break;

			default:
				break;
			}
		} else if (layer.equals("UI")) {
			/*
			 * First check if route already exists for a given destination.
			 * If yes, then send the data to that device.
			 * If no, then broadcast a RREQ packet.
			 */
			
			RouteTable.showTable();
			
			Log.d(TAG,"Checking if route exist.");
			
			Route isPresent = RouteTable.routeToDest(device);
			
			if (isPresent == null) {
				Log.d(TAG,"Route for "+device+"doesn't exists. Sending RREQ");
				Route_Message rreq = new Route_Message(PacketHandler.RREQ,
						RouteTable.getSequenceNumber(),
						BluetoothManagerService.selfAddress, device, 1);
				RouteTable.broadcastRREQ(rreq);
				Log.d(TAG,"RREQ broadcasted");
				
			} else {
				RouteTable.forwardMessage(isPresent.getNext_hop(), msg);
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

package com.android.BluetoothManager.Routing;

import java.util.Iterator;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import com.android.BluetoothManager.Routing.Packet_types.DataPacket;
import com.android.BluetoothManager.Routing.Packet_types.RadioPacket;
import com.android.BluetoothManager.Routing.Packet_types.Route_Message;
import com.android.BluetoothManager.Routing.Packet_types.UIPacket;

public class PacketHandlerService extends Thread {

	// Integers for types of packets according to DYMO protocol
	public static final int RREQ = 1;
	public static final int RREP = 2;
	public static final int RERR = 3;
	public static final int DATA = 4;

	public static final String TAG = "PacketHandlerService";

	/*
	 * This thread will loop through both the queues which contain objects from
	 * the above and below layers respectively and process them
	 */
	@Override
	public void run() {

		Log.d(TAG, "Packet Handler Service Started !!");

		Iterator<UIPacket> itr_UI;
		Iterator<RadioPacket> itr_radio;

		UIPacket temp_UI;
		RadioPacket temp_radio;
		try {
		
			
			//Log.d(TAG, "ConnectionObject :"+RouteTable.bluetooth_manager.connection.toString());
			while (true) {
				Log.d(TAG, "Looping through the iterators");
				RoutingPacketReceiver.printQueues();

				itr_UI = RoutingPacketReceiver.objectsFromUI.iterator();
				for (; itr_UI.hasNext();) {
					temp_UI = itr_UI.next();
					this.processUIPacket(temp_UI);
				}

				itr_radio = RoutingPacketReceiver.objectsFromRadio.iterator();
				for (; itr_radio.hasNext();) {
					temp_radio = itr_radio.next();
					this.processRadioPacket(temp_radio);
				}
				
				if (RouteTable.bluetooth_manager.connection
						.getBluetoothAdapter().getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {

					RouteTable.bluetooth_manager.connection
							.makeDeviceDisocverable();
				}

				Thread.sleep(2000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * This function will take UIPacket from queue. If it is very old, delete it
	 * from queue. Check searching flag. If false, check if route exists. If yes
	 * send it and remove from Queue. If no, broadcast an RREQ and set searching
	 * flag as true.
	 */
	void processUIPacket(UIPacket ui_packet) {
		if ((System.currentTimeMillis() / 1000 - ui_packet.getTimestamp()) > 60) {
			RoutingPacketReceiver.objectsFromUI.remove(ui_packet);
			// TODO notify UI could not send
		} else {
			Log.d(TAG,
					"Difference: "
							+ (System.currentTimeMillis() / 1000 - ui_packet
									.getTimestamp()) + "");
			Route gotRoute = RouteTable.bluetooth_manager.route_table
					.getRouteToDest(ui_packet.getDeviceToSend());
			if (gotRoute != null) {
				DataPacket data_packet = new DataPacket(
						ui_packet.getDeviceToSend(), ui_packet.getMsg());
				RouteTable.bluetooth_manager.route_table.forwardMessage(
						gotRoute.getNext_hop(), data_packet.toString());
				Log.d(TAG,
						"Route found, sending message:"
								+ data_packet.toString() + " to "
								+ gotRoute.getNext_hop());
				RoutingPacketReceiver.objectsFromUI.remove(ui_packet);
			} else {
				if (ui_packet.isSearching()) {
					Log.d(TAG,
							"Searching Route for "
									+ ui_packet.getDeviceToSend());
				} else {
					Route_Message rreq = new Route_Message(RREQ,
							RouteTable.getSequenceNumber(),
							RouteTable.bluetooth_manager.getSelfAddress(),
							ui_packet.getDeviceToSend(), 1);
					Log.d(TAG, "Route not found, broadcasting RREQ for "
							+ ui_packet.getDeviceToSend());
					RouteTable.bluetooth_manager.route_table
							.broadcastRREQ(rreq);
					ui_packet.setSearching(true);
				}
			}
		}
	}

	/*
	 * This function takes a radio packet; checks its type, and then calls the
	 * respective parser function, which will parse the message and call
	 * RouteTable object to process it
	 */
	void processRadioPacket(RadioPacket radio_packet) {
		String device = radio_packet.getDeviceFrom();
		String msg = radio_packet.getMsg();

		RoutingPacketReceiver.objectsFromRadio.remove(radio_packet);

		int type = getRadioPacketType(msg);
		switch (type) {
		case RREQ:
			Log.d(TAG, "RREQ packet");
			PacketParser.parseRREQ(device, msg);
			break;

		case RREP:
			Log.d(TAG, "RREP packet");
			PacketParser.parseRREP(device, msg);
			break;

		case RERR:
			Log.d(TAG, "RERR packet");
			PacketParser.parseRERR(device, msg);
			break;

		case DATA:
			Log.d(TAG, "Data packet");
			PacketParser.parseData(device, msg);
			break;

		default:
			break;
		}
	}

	// Function which will return the type message(packet) which is passed to it
	int getRadioPacketType(String msg) {
		return Integer.parseInt(msg.charAt(0) + "");
	}

}

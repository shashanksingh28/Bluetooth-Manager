package com.android.BluetoothManager.Routing;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;
import com.android.BluetoothManager.Routing.Packet_types.Route_Message;
import com.android.BluetoothManager.UI.R;

public class RouteTable {

	final static String TAG = "RouteTable";

	public BluetoothManagerApplication bluetooth_manager;

	// Static routing table, initialize on application start
	ArrayList<Route> table;

	public RouteTable(BluetoothManagerApplication bluetooth_manager) {
		this.bluetooth_manager = bluetooth_manager;
		if (table == null) {
			table = new ArrayList<Route>();
		}
	}

	
	// check if current device is destination
	boolean isDestination(String Bluetooth_Addr) {
		if (bluetooth_manager.getSelfAddress().equals(Bluetooth_Addr)) {
			return true;
		} else
			return false;
	}

	public void checkRREQ(String device, Route_Message rreq) {
		if (isDestination(rreq.getDest_addr())) {
			Route new_r = new Route(getSequenceNumber(),
					rreq.getOriginator_addr(), device, rreq.getHop_count());
			table.add(new_r);
			showTable();
			Route_Message rrep = new Route_Message(PacketHandler.RREP,
					new_r.getSeq_Number(), bluetooth_manager.getSelfAddress(),
					new_r.getDest_addr(), 1);
			unicastRREP(device, rrep);
		} else {
			Route isPresent = routeToDest(rreq.getOriginator_addr());
			if (isPresent != null) {
				// if Route Present, check if entry is stale and update if req
				if (rreq.getOriginator_seqNumber() > isPresent.getSeq_Number()
						|| rreq.getHop_count() < isPresent.getHop_count()) {
					isPresent.setSeq_Number(rreq.getOriginator_seqNumber());
					isPresent.setNext_hop(device);
					isPresent.setHop_count(rreq.getHop_count());
					rreq.setHop_count(rreq.getHop_count() + 1);
					broadcastRREQ(rreq);
				}
			} else {
				// Route not present, add new
				Route new_r = new Route(rreq.getOriginator_seqNumber(),
						rreq.getOriginator_addr(), device, rreq.getHop_count());
				table.add(new_r);
				showTable();
				rreq.setHop_count(rreq.getHop_count() + 1);
				broadcastRREQ(rreq);
			}
		}
	}

	public void checkRREP(String device, Route_Message rrep) {

		if (isDestination(rrep.getDest_addr())) {
			Route new_r = new Route(rrep.getOriginator_seqNumber(),
					rrep.getOriginator_addr(), device, rrep.getHop_count());
			table.add(new_r);
			showTable();
			// Connection established... Send Message here
		} else {
			Route new_r = new Route(rrep.getOriginator_seqNumber(),
					rrep.getOriginator_addr(), device, rrep.getHop_count());
			table.add(new_r);
			showTable();
			rrep.setHop_count(rrep.getHop_count() + 1);
			Route r = routeToDest(rrep.getDest_addr());
			unicastRREP(r.getNext_hop(), rrep);
		}

	}

	public  int checkData(String device, String dest_addr, String data) {
		if (isDestination(dest_addr)) {
			String ACTION = bluetooth_manager.getResources().getString(R.string.ROUTING_TO_UI);
			Intent i = new Intent();
			i.putExtra("device", device);
			i.putExtra("msg",data);
			i.setAction(ACTION);
			bluetooth_manager.sendBroadcast(i);
		} else {
			Route isPresent = routeToDest(dest_addr);
			if (isPresent != null) {
				forwardMessage(isPresent.getNext_hop(), data);
			} else {
				// Create RERR
				return -1;
			}
		}
		return 0;
	}

	// Function which returns a current route to a specific destination
	Route routeToDest(String dest) {
		Route temp;
		Iterator<Route> itr = table.iterator();
		while (itr.hasNext()) {
			temp = itr.next();
			if (temp.getDest_addr().equals(dest)) {
				return temp;
			}
		}
		return null;
	}

	void broadcastRREQ(Route_Message rreq) {
		// code here to send broadcast RREQ
		try {
			
			// Intent to be put here.
			bluetooth_manager.connection.broadcastMessage(rreq.toString());
		} catch (RemoteException e) {
			Log.d(TAG,"++ Error in broadcastRREQ()");
		}
	}

	void unicastRREP(String device, Route_Message rrep) {
		// code here to send unicast RREP to device
		
		// Intent to be put here
		bluetooth_manager.connection.sendMessage(device, rrep.toString());
	}

	void forwardMessage(String device, String data) {
		// code here to forward data to device
		// intent to be put here
		
		bluetooth_manager.connection.sendMessage(device, data);
	}

	void showTable() {
		Iterator<Route> itr = table.iterator();
		Route temp;
		String msg;
		Log.d(TAG, "Displaying route table at " + getSequenceNumber());
		while (itr.hasNext()) {
			temp = itr.next();
			msg = "Seq_No : " + temp.getSeq_Number() + " Dest_addr : "
					+ temp.getDest_addr() + " Next Hop : " + temp.getNext_hop()
					+ " Hop Count : " + temp.getHop_count();
			Log.d(TAG, msg);
		}
	}

	static long getSequenceNumber() {
		return System.currentTimeMillis() / 1000;
	}

}

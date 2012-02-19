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
		Log.d(TAG,"My BT ADD:"+bluetooth_manager.getSelfAddress());
		Log.d(TAG,"Received BT ADD:"+Bluetooth_Addr);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bluetooth_manager.getSelfAddress().equals(Bluetooth_Addr)) {
			return true;
		} else
			return false;
	}

	public void checkRREQ(String device, Route_Message rreq) {
		// First check if route is present for the originator
		Log.d(TAG, "RREQ received at " + bluetooth_manager.getSelfAddress()
				+ "\n" + rreq.toString());

		Route isPresent = routeToDest(rreq.getOriginator_addr());

		if (isPresent == null) {
			// not present, make new entry
			Route new_r = new Route(rreq.getOriginator_seqNumber(),
					rreq.getOriginator_addr(), device, rreq.getHop_count());
			Log.d(TAG, "Route created at " + bluetooth_manager.getSelfAddress());
			table.add(new_r);
			showTable();
		} else {
			// entry present, check if its latest and shorter?
			if (isPresent.getSeq_Number() < rreq.getOriginator_seqNumber()
					|| isPresent.getHop_count() > rreq.getHop_count()) {
				isPresent.setSeq_Number(rreq.getOriginator_seqNumber());
				isPresent.setNext_hop(device);
				isPresent.setHop_count(rreq.getHop_count());
				Log.d(TAG,
						"Route Modified at "
								+ bluetooth_manager.getSelfAddress()
								+ isPresent.toString());
			}
		}

		// Table done, now decide if broadcast or send RREP
		if (isDestination(rreq.getDest_addr())) {
			Route_Message rrep = new Route_Message(PacketHandler.RREP,
					getSequenceNumber(), bluetooth_manager.getSelfAddress(),
					rreq.getOriginator_addr(), 1);
			unicastRREP(device, rrep);
		} else {
			rreq.setHop_count(rreq.getHop_count() + 1);
			broadcastRREQ(rreq);
		}
	}

	public void checkRREP(String device, Route_Message rrep) {
		Log.d(TAG, "RREP received at " + bluetooth_manager.getSelfAddress()
				+ "\n" + rrep.toString());
		// First check if route to Originator Present

		Route isPresent = routeToDest(rrep.getOriginator_addr());
		if (isPresent == null) {
			// Route not present,(which should be ideally), add a new route
			Route new_r = new Route(rrep.getOriginator_seqNumber(),
					rrep.getOriginator_addr(), device, rrep.getHop_count());
			table.add(new_r);
			Log.d(TAG, "Route created at " + bluetooth_manager.getSelfAddress());
			showTable();
		} else {
			// Route Present, check if its small or latest
			if (isPresent.getSeq_Number() < rrep.getOriginator_seqNumber()
					|| isPresent.getHop_count() > rrep.getHop_count()) {
				isPresent.setSeq_Number(rrep.getOriginator_seqNumber());
				isPresent.setNext_hop(device);
				isPresent.setHop_count(rrep.getHop_count());
				Log.d(TAG,
						"Route Modified at "
								+ bluetooth_manager.getSelfAddress()
								+ isPresent.toString());
			}
		}

		// Table done, now if this is destination,noitify route found, else
		// unicast RREP
		if (isDestination(rrep.getDest_addr())) {
			// Connection established... Send Message here
			Log.d(TAG, "Yipee!! I found a way to him");
		} else {
			Route r = routeToDest(rrep.getDest_addr());
			rrep.setHop_count(rrep.getHop_count() + 1);
			unicastRREP(r.getNext_hop(), rrep);
		}

	}

	public int checkData(String device, String dest_addr, String data) {
		if (isDestination(dest_addr)) {
			String ACTION = bluetooth_manager.getResources().getString(
					R.string.ROUTING_TO_UI);
			Intent i = new Intent();
			i.putExtra("device", device);
			i.putExtra("msg", data);
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
		String ACTION = bluetooth_manager.getResources().getString(
				R.string.ROUTING_TO_RADIO);
		Intent i = new Intent();
		i.setAction(ACTION);
		i.putExtra("msg", rreq.toString());
		bluetooth_manager.sendBroadcast(i);

	}

	void unicastRREP(String device, Route_Message rrep) {
		// code here to send unicast RREP to device

		// Intent to be put here
		String ACTION = bluetooth_manager.getResources().getString(
				R.string.ROUTING_TO_RADIO);
		Intent i = new Intent();
		i.setAction(ACTION);
		i.putExtra("device", device);
		i.putExtra("msg", rrep.toString());
		bluetooth_manager.sendBroadcast(i);
	}

	void forwardMessage(String device, String data) {
		// code here to forward data to device
		// intent to be put here
		String ACTION = bluetooth_manager.getResources().getString(
				R.string.ROUTING_TO_RADIO);
		Intent i = new Intent();
		i.setAction(ACTION);
		i.putExtra("device", device);
		i.putExtra("msg", data);
		bluetooth_manager.sendBroadcast(i);

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

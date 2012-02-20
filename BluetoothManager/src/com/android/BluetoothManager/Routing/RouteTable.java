package com.android.BluetoothManager.Routing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

import android.content.Intent;
import android.util.Log;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;
import com.android.BluetoothManager.Routing.Packet_types.Route_Error;
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

	public void processRREQ(String device, Route_Message rreq) {
		
		/* First check if route to Originator present. If yes, compare the two and update.
		 * If no, add a new Route to originator of RREQ. Then check if self is destination.
		 * If yes, then create an RREP and unicast it as the reply.
		 * If no, broadcast the RREQ to other nodes
		 */
		
		Log.d(TAG, "RREQ received at " + bluetooth_manager.getSelfAddress()
				+ "\n" + rreq.toString());

		Route isPresent = getRouteToDest(rreq.getOriginator_addr());

		if (isPresent == null) {
			Route new_r = new Route(rreq.getOriginator_seqNumber(),
					rreq.getOriginator_addr(), device, rreq.getHop_count());
			Log.d(TAG, "Route created at " + bluetooth_manager.getSelfAddress());
			table.add(new_r);
			showTable();
		} else {
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

	public void processRREP(String device, Route_Message rrep) {
		
		/* First check if route to Originator present. If yes, compare the two and update
		 * If no, add a new Route to originator of RREP. Then check if self is destination.
		 * If yes, then channel established, do something 
		 * If no, unicast the RREP to the nextHop for the intended Destination from table
		 */
		Log.d(TAG, "RREP received at " + bluetooth_manager.getSelfAddress()
				+ "\n" + rrep.toString());

		Route isPresent = getRouteToDest(rrep.getOriginator_addr());
		if (isPresent == null) {
			Route new_r = new Route(rrep.getOriginator_seqNumber(),
					rrep.getOriginator_addr(), device, rrep.getHop_count());
			table.add(new_r);
			Log.d(TAG, "Route created at " + bluetooth_manager.getSelfAddress());
			showTable();
		} else {
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

		if (isDestination(rrep.getDest_addr())) {
			//Do something here
			Log.d(TAG, "Yipee!! I found a way to him");
		} else {
			Route r = getRouteToDest(rrep.getDest_addr());
			rrep.setHop_count(rrep.getHop_count() + 1);
			if(r!=null)	//It shouldn't be null, but what if?
			{
				unicastRREP(r.getNext_hop(), rrep);
			}
		}

	}

	public void processRERR(String device, Route_Error rerr)
	{
		/* First check if the unreachable device is a destination in table.
		 * If yes, check if the nextHop is the same as device.
		 * 	If yes, delete the route and send RERR to all NextHops
		 * If no, IGNORE
		 */
		
		Route isPresent= this.getRouteToDest(rerr.getUnreachable_addr());
		if(isPresent!=null)
		{
			if(isPresent.getNext_hop().equals(device))
			{
				table.remove(isPresent);
				LinkedHashSet<String> nextHops=this.getAllNextHops();
				Iterator<String> itr=nextHops.iterator();
				while(itr.hasNext())
				{
					this.forwardMessage(itr.next(),rerr.toString());
				}
			}
		}
		
	}
	
	public int processData(String device, String dest_addr, String data) {
		/* First check if self is destination. If yes, pass on the data as an intent to UI
		 * If no, check if route exists. If yes, then forward to next hop
		 * If no, generate an RERR and send it to all routes present
		 */
		
		if (isDestination(dest_addr)) {
			String ACTION = bluetooth_manager.getResources().getString(
					R.string.ROUTING_TO_UI);
			Intent i = new Intent();
			i.putExtra("device", device);
			i.putExtra("msg", data);
			i.setAction(ACTION);
			bluetooth_manager.sendBroadcast(i);
		} else {
			Route isPresent = getRouteToDest(dest_addr);
			if (isPresent != null) {
				forwardMessage(isPresent.getNext_hop(), data);
			} 
			else 
			{
				Route_Error rerr=new Route_Error(PacketHandler.RERR,getSequenceNumber(),dest_addr);
				LinkedHashSet<String> nextHops=this.getAllNextHops();
				Iterator<String> itr=nextHops.iterator();
				while(itr.hasNext())
				{
					this.forwardMessage(itr.next(),rerr.toString());
				}
				return -1;
			}
		}
		return 0;
	}

	// Function which returns a current route to a specific destination
	Route getRouteToDest(String dest) {
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

	//Function returning non repeated set neighbors who are a part of a route
	LinkedHashSet<String> getAllNextHops()
	{
		LinkedHashSet<String> allHops= new LinkedHashSet<String>();
		Iterator<Route> itr=table.iterator();
		while(itr.hasNext())
		{
			allHops.add(((Route)itr.next()).getNext_hop());
		}
		
		return allHops;
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

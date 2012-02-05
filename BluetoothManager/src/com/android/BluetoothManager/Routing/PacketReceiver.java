package com.android.BluetoothManager.Routing;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/* 
 * Class that will recieve packets, distinguish their type
 * and call respective function of the PacketHandler
 */
public class PacketReceiver extends BroadcastReceiver {

	// Integers for types of packets according to DYMO protocol
	public static final int RREQ = 1;
	public static final int RREP = 2;
	public static final int RERR = 3;
	public static final int DATA = 4;

	// Route
	
	
	public PacketReceiver() {
		RouteTable.table = new ArrayList<Route>();
	}
	
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String device = intent.getExtras().getString("device");
		String msg = intent.getExtras().getString("msg");
		int type = getType(msg);

		switch (type) 
		{
			case RREQ:
				PacketHandler.processRREQ(device,msg);
				break;
				
			case RREP:
				PacketHandler.processRREP(device,msg);
				break;
				
			case RERR:
				PacketHandler.processRERR(device,msg);
				break;
				
			case DATA:
				PacketHandler.processData(device,msg);
				break;

			default:
				break;
		}

	}

	/*
	 * Function which will return the type message(packet) which is passed to it
	 */
	int getType(String msg) {
		return Integer.parseInt(msg.charAt(0) + "");

	}
}

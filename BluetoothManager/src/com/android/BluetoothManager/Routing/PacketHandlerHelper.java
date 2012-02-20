package com.android.BluetoothManager.Routing;

import android.util.Log;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;
import com.android.BluetoothManager.Routing.Packet_types.Route_Error;
import com.android.BluetoothManager.Routing.Packet_types.Route_Message;

/*
 * Class that will handle processing of packets
 */
public class PacketHandlerHelper {

	private static final String TAG = "PacketHandlerHelper";
	BluetoothManagerApplication bluetooth_manager;
	
	public PacketHandlerHelper(BluetoothManagerApplication bluetooth_manager) {
		this.bluetooth_manager = bluetooth_manager;
	}
	
	
	public int parseRREQ(String device, String msg) {
		//parse RREQ from msg
		Log.d(TAG,"Msg in PHH: "+msg);
		String packet_fields[] = msg.split(",");
		long originator_seqNumber = Long.parseLong(packet_fields[1]);
		String originator_addr = packet_fields[2];
		String dest_addr = packet_fields[3];
		int no_of_hops = Integer.parseInt(packet_fields[4]);
		Route_Message rreq= new Route_Message(PacketHandler.RREQ,originator_seqNumber, originator_addr, dest_addr,no_of_hops);
		//RREQ parsed, now check
		bluetooth_manager.route_table.processRREQ(device,rreq);
		
		return 0;
	}

	public int parseRREP(String device, String msg) {
		//parse RREP from msg
		String packet_fields[] = msg.split(",");
		long originator_seqNumber = Long.parseLong(packet_fields[1]);
		String originator_addr = packet_fields[2];
		String dest_addr = packet_fields[3];
		int no_of_hops = Integer.parseInt(packet_fields[4]);
		Route_Message rrep= new Route_Message(PacketHandler.RREP,originator_seqNumber, originator_addr, dest_addr,no_of_hops);
		//RREP parsed, now check
		bluetooth_manager.route_table.processRREP(device,rrep);

		return 0;
	}

	public int parseRERR(String device, String msg) {
		String packet_fields[] = msg.split(",");
		long unreachable_seqNumber = Long.parseLong(packet_fields[1]);
		String unreachable_addr = packet_fields[2];
		Route_Error rerr= new Route_Error(PacketHandler.RERR, unreachable_seqNumber, unreachable_addr);
		bluetooth_manager.route_table.processRERR(device, rerr);
		return 0;
	}

	//function to process data, returns -1 if an RERR occurs, 0 if not
	public int parseData(String device, String msg) {
		
		String packet_fields[] = msg.split(",");
		String dest_addr=packet_fields[1];
		String data=packet_fields[2];
		return bluetooth_manager.route_table.processData(device, dest_addr, data);
	}

}

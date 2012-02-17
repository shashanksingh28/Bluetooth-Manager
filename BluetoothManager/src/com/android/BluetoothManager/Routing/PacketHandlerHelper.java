package com.android.BluetoothManager.Routing;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;
import com.android.BluetoothManager.Routing.Packet_types.*;

/*
 * Class that will handle processing of packets
 */
public class PacketHandlerHelper {

	BluetoothManagerApplication bluetooth_manager;
	
	public PacketHandlerHelper(BluetoothManagerApplication bluetooth_manager) {
		this.bluetooth_manager = bluetooth_manager;
	}
	
	
	public int processRREQ(String device, String msg) {
		//parse RREQ from msg
		String packet_fields[] = msg.split(",");
		long originator_seqNumber = Long.parseLong(packet_fields[1]);
		String originator_addr = packet_fields[2];
		String dest_addr = packet_fields[3];
		int no_of_hops = Integer.parseInt(packet_fields[4]);
		Route_Message rreq= new Route_Message(PacketHandler.RREQ,originator_seqNumber, originator_addr, dest_addr,no_of_hops);
		//RREQ parsed, now check
		bluetooth_manager.route_table.checkRREQ(device,rreq);
		
		return 0;
	}

	public int processRREP(String device, String msg) {
		//parse RREP from msg
		String packet_fields[] = msg.split(",");
		long originator_seqNumber = Long.parseLong(packet_fields[1]);
		String originator_addr = packet_fields[2];
		String dest_addr = packet_fields[3];
		int no_of_hops = Integer.parseInt(packet_fields[4]);
		Route_Message rrep= new Route_Message(PacketHandler.RREP,originator_seqNumber, originator_addr, dest_addr,no_of_hops);
		//RREP parsed, now check
		bluetooth_manager.route_table.checkRREP(device,rrep);

		return 0;
	}

	public int processRERR(String device, String msg) {

		return 0;
	}

	//function to process data, returns -1 if an RERR occurs, 0 if not
	public int processData(String device, String msg) {
		
		String packet_fields[] = msg.split(",");
		String dest_addr=packet_fields[1];
		String data=packet_fields[2];
		return bluetooth_manager.route_table.checkData(device, dest_addr, data);
	}

}

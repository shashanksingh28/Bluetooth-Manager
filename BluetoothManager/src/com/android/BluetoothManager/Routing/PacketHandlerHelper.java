package com.android.BluetoothManager.Routing;

import com.android.BluetoothManager.Routing.Packet_types.*;

/*
 * Class that will handle processing of packets
 */
public class PacketHandlerHelper {

	
	public static int processRREQ(String device, String msg) {
		//parse RREQ from msg
		String packet_fields[] = msg.split(",");
		long originator_seqNumber = Long.parseLong(packet_fields[1]);
		String originator_addr = packet_fields[2];
		String dest_addr = packet_fields[3];
		int no_of_hops = Integer.parseInt(packet_fields[4]);
		Route_Message rreq= new Route_Message(PacketHandler.RREQ,originator_seqNumber, originator_addr, dest_addr,no_of_hops);
		//RREQ parsed, now check
		RouteTable.checkRREQ(device,rreq);
		
		return 0;
	}

	public static int processRREP(String device, String msg) {
		//parse RREP from msg
		String packet_fields[] = msg.split(",");
		long originator_seqNumber = Long.parseLong(packet_fields[1]);
		String originator_addr = packet_fields[2];
		String dest_addr = packet_fields[3];
		int no_of_hops = Integer.parseInt(packet_fields[4]);
		Route_Message rrep= new Route_Message(PacketHandler.RREP,originator_seqNumber, originator_addr, dest_addr,no_of_hops);
		//RREP parsed, now check
		RouteTable.checkRREP(device,rrep);

		return 0;
	}

	public static int processRERR(String device, String msg) {

		return 0;
	}

	//function to process data, returns -1 if an RERR occurs, 0 if not
	public static int processData(String device, String msg) {
		
		String packet_fields[] = msg.split(",");
		String dest_addr=packet_fields[1];
		String data=packet_fields[2];
		return RouteTable.checkData(device, dest_addr, data);
	}

}

package com.android.BluetoothManager.Routing;

import java.util.Date;

import com.android.BluetoothManager.Routing.Packet_types.RREQ_packet;

/*
 * Class that will handle procesing of packets
 */
public class PacketHandler {

	
	public static int processRREQ(String device, String msg) {
		//make the RREQ object after splitting
		String packet_fields[] = msg.split(",");
		Date seq_number = new Date(Long.parseLong(packet_fields[1]));
		String source_mac = packet_fields[2];
		String dest_mac = packet_fields[3];
		String from_mac= packet_fields[4];
		int no_of_hops = Integer.parseInt(packet_fields[5]);
		RREQ_packet rreq= new RREQ_packet(seq_number, source_mac, dest_mac, from_mac, no_of_hops);
		//Route packet created, now check
		RouteTable.checkRREQ(rreq);
		
		return 0;
	}

	public static int processRREP(String device, String msg) {

		return 0;
	}

	public static int processRERR(String device, String msg) {

		return 0;
	}

	public static int processData(String device, String msg) {

		return 0;
	}

}

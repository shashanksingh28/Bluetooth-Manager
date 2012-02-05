package com.android.BluetoothManager.Routing;

/*
 * Class that will handle procesing of packets
 */
public class PacketHandler {

	
	public static int processRREQ(String device, String msg) {
		String packet_fields[] = msg.split(",");
		int seq_number = Integer.parseInt(packet_fields[1]);
		String source_mac = packet_fields[2];
		String dest_mac = packet_fields[3];
		int no_of_hops = Integer.parseInt(packet_fields[4]);
		Route r = new Route(seq_number,device,source_mac,dest_mac,"?",no_of_hops);
		
		
		
		if(RouteTable.isDestination(r)){
			RouteTable.addRoute(r,true);
			return 0;
		}
		
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

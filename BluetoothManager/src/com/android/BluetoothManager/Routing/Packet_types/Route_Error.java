package com.android.BluetoothManager.Routing.Packet_types;

public class Route_Error {

	int packet_type;
	long unreachable_seqNumber;
	String unreachable_addr;
	
	public Route_Error(int packet_type, long unreachable_seqNumber, String unreachable_addr) {
		super();
		this.packet_type=packet_type;
		this.unreachable_seqNumber = unreachable_seqNumber;
		this.unreachable_addr = unreachable_addr;
	}
	
	public String toString()
	{
		return packet_type+","+unreachable_seqNumber+","+unreachable_addr;
	}

	public long getUnreachable_seqNumber() {
		return unreachable_seqNumber;
	}

	public void setUnreachable_seqNumber(long unreachable_seqNumber) {
		this.unreachable_seqNumber = unreachable_seqNumber;
	}

	public String getUnreachable_addr() {
		return unreachable_addr;
	}

	public void setUnreachable_addr(String unreachable_addr) {
		this.unreachable_addr = unreachable_addr;
	}

	
}

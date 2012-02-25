package com.android.BluetoothManager.Routing.Packet_types;

public class DataPacket {
	String dest_addr;
	String msg;
	
	public DataPacket(String dest_addr, String msg) {
		super();
		this.dest_addr = dest_addr;
		this.msg = msg;
	}

	public String getDest_addr() {
		return dest_addr;
	}

	public void setDest_addr(String dest_addr) {
		this.dest_addr = dest_addr;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
	
}

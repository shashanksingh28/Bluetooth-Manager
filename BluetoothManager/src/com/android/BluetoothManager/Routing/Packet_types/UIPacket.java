package com.android.BluetoothManager.Routing.Packet_types;

/* Created within node and only in Routing layer
 * Represents a packet which the UI layer has sent to the radio layer
 * It should be added to the Queue and processed by the routing service
 */
public class UIPacket {
	
	String deviceToSend;
	String msg;
	long timestamp;
	boolean searching=false;
	
	public UIPacket(String deviceToSend, String msg) {
		super();
		this.deviceToSend = deviceToSend;
		this.msg = msg;
		timestamp=System.currentTimeMillis()/1000;
	}

	public String getDeviceToSend() {
		return deviceToSend;
	}

	public void setDeviceToSend(String deviceToSend) {
		this.deviceToSend = deviceToSend;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isSearching() {
		return searching;
	}

	public void setSearching(boolean searching) {
		this.searching = searching;
	}
	
	public String toString()
	{
		return "Device To Send : "+deviceToSend+"\nMessage : "+msg+"\nTime : "+timestamp+"\n";
	}
	

}

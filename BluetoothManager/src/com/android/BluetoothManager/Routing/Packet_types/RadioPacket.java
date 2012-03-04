package com.android.BluetoothManager.Routing.Packet_types;

/* Created and used within node and only in Routing layer
 * Represents a packet which Radio layer sends routing layer
 * This should be added to the Queue and processed
 */
public class RadioPacket {

	String deviceFrom;
	String msg;
	long timestamp;
	
	public RadioPacket(String deviceFrom,String msg)
	{
		this.deviceFrom=deviceFrom;
		this.msg=msg;
		this.timestamp=System.currentTimeMillis()/1000; 
	}

	public String getDeviceFrom() {
		return deviceFrom;
	}

	public void setDeviceFrom(String deviceFrom) {
		this.deviceFrom = deviceFrom;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public String toString()
	{
		return "Received from : "+deviceFrom+"\nMessage : "+msg+"\n Time : "+timestamp+"\n";
	}
}

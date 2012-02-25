package com.android.BluetoothManager.Routing;

import java.util.LinkedList;
import java.util.Queue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;
import com.android.BluetoothManager.UI.R;

/* 
 * Class that will receive packets, distinguish their type
 * and call respective function of the PacketHandler.
 * The objects will be pushed to two queues, one for the packets from UI
 * and one for packets from Radio. A separate thread will process them
 */
public class RoutingPacketReceiver extends BroadcastReceiver {

	private static final String TAG = "Routing_PacketReceiver";

	public static Queue<UIPacket> objectsFromUI;
	public static Queue<RadioPacket> objectsFromRadio;
	BluetoothManagerApplication bluetooth_manager;



	public RoutingPacketReceiver(BluetoothManagerApplication bluetooth_manager) {
		this.bluetooth_manager = bluetooth_manager;
		objectsFromUI= new LinkedList<UIPacket>();
		objectsFromRadio= new LinkedList<RadioPacket>();
	}

	/* Function called when protocol layer receives a packet.
	 * First check which layer sent the data and push to the Queue accordingly
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		String device = intent.getExtras().getString("device");
		String msg = intent.getExtras().getString("msg");

		if (intent.getAction().equals(bluetooth_manager.getResources().getString(R.string.UI_TO_ROUTING))) 
		{
			UIPacket ui_packet= new UIPacket(device, msg);
			Log.d(TAG, "Pushing down msg: "+msg+" from "+device +" on queue");
			objectsFromUI.add(ui_packet);
		}
		else if(intent.getAction().equals(bluetooth_manager.getResources().getString(R.string.RADIO_TO_ROUTING)))
		{
			RadioPacket radio_packet= new RadioPacket(device, msg);
			Log.d(TAG, "Pushing up msg: "+msg+" from "+device +" on queue");
			objectsFromRadio.add(radio_packet);
		}
		
	}
	
}

class UIPacket
{
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
	
}

class RadioPacket
{
	String deviceFrom;
	String msg;
	
	public RadioPacket(String deviceFrom,String msg)
	{
		this.deviceFrom=deviceFrom;
		this.msg=msg;
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
	
	
}
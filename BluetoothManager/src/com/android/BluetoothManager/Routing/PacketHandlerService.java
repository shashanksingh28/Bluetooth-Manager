package com.android.BluetoothManager.Routing;

import java.util.Iterator;

import android.util.Log;

public class PacketHandlerService implements Runnable{

	// Integers for types of packets according to DYMO protocol
	public static final int RREQ = 1;
	public static final int RREP = 2;
	public static final int RERR = 3;
	public static final int DATA = 4;
	
	public static final String TAG="PacketHandlerService";
	
	/* This thread will loop through both the queues which contain objects from the above
	 * and below layers respectively and process them
	 */
	@Override
	public void run() {
		Iterator<UIPacket> itr_UI;
		Iterator<RadioPacket> itr_radio;
		
		UIPacket temp_UI;
		RadioPacket temp_radio;
		try
		{
			while(true)
			{
				itr_UI=RoutingPacketReceiver.objectsFromUI.iterator();
				for(;itr_UI.hasNext();)
				{
					temp_UI=itr_UI.next();
					this.processUIPacket(temp_UI);
				}
				
				itr_radio=RoutingPacketReceiver.objectsFromRadio.iterator();
				for(;itr_radio.hasNext();)
				{
					temp_radio=itr_radio.next();
					this.processRadioPacket(temp_radio);
				}
				
				Thread.sleep(100);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void processUIPacket(UIPacket ui_packet)
	{
		if((System.currentTimeMillis()-ui_packet.getTimestamp())>15000)
		{
			
		}
	}
	
	
	void processRadioPacket(RadioPacket radio_packet)
	{
		String device=radio_packet.getDeviceFrom();
		String msg =radio_packet.getMsg();
		
		int type=getType(msg);
		switch (type) {
		case RREQ:
			Log.d(TAG,"RREQ received by routing. Now processing.");
			PacketParser.parseRREQ(device, msg);

			break;

		case RREP:
			Log.d(TAG,"RREP received by routing. Now processing.");
			PacketParser.parseRREP(device, msg);
			break;

		case RERR:
			PacketParser.parseRERR(device, msg);
			break;

		case DATA:
			PacketParser.parseData(device, msg);
			break;

		default:
			break;
		}
	}
	
	//Function which will return the type message(packet) which is passed to it
		int getType(String msg) {
			return Integer.parseInt(msg.charAt(0) + "");
		}

}

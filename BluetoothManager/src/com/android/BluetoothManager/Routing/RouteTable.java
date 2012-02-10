package com.android.BluetoothManager.Routing;

import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;

import com.android.BluetoothManager.Radio.BluetoothManagerService;
import com.android.BluetoothManager.Routing.Packet_types.Route_Message;

public class RouteTable {

	final static String TAG="RouteTable";
	
	//Static routing table, initialize on application start
	static ArrayList<Route> table;
	
	//method called by application on startup to initialize
	public static boolean initializeRouteTable()
	{
		if(table==null){
			table = new ArrayList<Route>();
			return true;
		}
		else
			return false;
	}
	
	//check if current device is destination
	static boolean isDestination(String Bluetooth_Addr){
		if(BluetoothManagerService.selfAddress.equals(Bluetooth_Addr)){
			return true;
		}
		else 
			return false;
	}

	public static void checkRREQ(String device, Route_Message rreq) {
		if(isDestination(rreq.getDest_addr()))
		{
			Route new_r= new Route(System.currentTimeMillis()/1000, rreq.getOriginator_addr(), 
					device, rreq.getHop_count());
			table.add(new_r);
			showTable();
			Route_Message rrep= new Route_Message(PacketReceiver.RREP,new_r.getSeq_Number(),BluetoothManagerService.selfAddress, 
					new_r.getDest_addr(),1);
			unicastRREP(device, rrep);
		}
		else
		{
			Route isPresent= routeToDest(rreq.getOriginator_addr());
			if(isPresent!=null)
			{
				//if Route Present, check if entry is stale and update if req
				if(rreq.getOriginator_seqNumber()>isPresent.getSeq_Number() ||
						rreq.getHop_count()<isPresent.getHop_count())
				{
					isPresent.setSeq_Number(rreq.getOriginator_seqNumber());
					isPresent.setNext_hop(device);
					isPresent.setHop_count(rreq.getHop_count());
					rreq.setHop_count(rreq.getHop_count()+1);
					broadcastRREQ(rreq);
				}
			}
			else
			{
				//Route not present, add new
				Route new_r = new Route(rreq.getOriginator_seqNumber(), rreq.getOriginator_addr(), 
						device,rreq.getHop_count());
				table.add(new_r);
				showTable();
				rreq.setHop_count(rreq.getHop_count()+1);
				broadcastRREQ(rreq);
			}
		}
	}

	public static void checkRREP(String device, Route_Message rrep) {
		
		if(isDestination(rrep.getDest_addr()))
		{
			Route new_r= new Route(rrep.getOriginator_seqNumber(), rrep.getOriginator_addr(), 
					device,rrep.getHop_count());
			table.add(new_r);
			showTable();
			//Connection established... Send Message here
		}
		else
		{
			Route new_r= new Route(rrep.getOriginator_seqNumber(), rrep.getOriginator_addr(), 
					device,rrep.getHop_count());
			table.add(new_r);
			showTable();
			rrep.setHop_count(rrep.getHop_count()+1);
			Route r= routeToDest(rrep.getDest_addr());
			unicastRREP(r.getNext_hop(), rrep);
		}
			
	}
	
	public static int checkData(String device, String dest_addr, String data) {
		if(isDestination(dest_addr))
		{
			//Send intent to upper layers with data
		}
		else
		{
			Route isPresent= routeToDest(dest_addr);
			if(isPresent!=null)
			{
				forwardMessage(isPresent.getNext_hop(), data);
			}
			else
			{
				//Create RERR
				return -1;
			}
		}
		return 0;
	}
	
	//Function which returns a current route to a specific destination
	static Route routeToDest(String dest)
	{
		Route temp;
		Iterator<Route> itr=table.iterator();
		while(itr.hasNext())
		{
			temp=itr.next();
			if(temp.getDest_addr().equals(dest))
			{
				return temp;
			}
		}
		return null;
	}

	static void broadcastRREQ(Route_Message rreq)
	{
		//code here to send broadcast RREQ
		BluetoothManagerService.connection.broadcastMessage(rreq.toString());
	}
	
	static void unicastRREP(String device,Route_Message rrep)
	{
		//code here to send unicast RREP to device
		BluetoothManagerService.connection.sendMessage(device, rrep.toString());
	}
	
	static void forwardMessage(String device,String data)
	{
		//code here to forward data to device
	}
	
	static void showTable()
	{
		Iterator<Route> itr= table.iterator();
		Route temp;
		String msg;
		Log.d(TAG, "Displaying route table at "+System.currentTimeMillis()/1000);
		while(itr.hasNext())
		{
			temp=itr.next();
			msg="Seq_No : "+temp.getSeq_Number()+" Dest_addr : "+temp.getDest_addr()+
					" Next Hop : "+temp.getNext_hop()+" Hop Count : "+temp.getHop_count();
			Log.d(TAG, msg);
		}
	}

	
}

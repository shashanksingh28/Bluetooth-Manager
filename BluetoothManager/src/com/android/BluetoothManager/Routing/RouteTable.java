package com.android.BluetoothManager.Routing;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.android.BluetoothManager.Radio.BluetoothManagerService;
import com.android.BluetoothManager.Routing.Packet_types.*;

public class RouteTable {

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
	
	/*
	 * Function called when node receives an RREQ packet
	 * First check if self is the destination, if yes then send an RREP
	 * If no then check if the route is already present or not, 
	 * if yes, RREP, if not add new
	 */
	public static int checkRREQ(RREQ_packet rreq) {
		
		if(isDestination(rreq.getDest_addr()))
		{
			//self is destination, send RREP
		}
		else
		{
			Route present;
			if((present=routePresent(rreq.getDest_addr()))!=null)
			{
				//route already present,
				//if Next_hop is not '?' send RREP
				if(!(present.getHop_addr().equals("?")))
				{
					//send RREP
				}
			}
			else
			{
				//add route to table
				addRREQRoute(rreq);
				//create new RREQ, increment hop and broadcast
				RREQ_packet new_rreq= new RREQ_packet(rreq.getSeq_number(),BluetoothManagerService.selfAddress,
						rreq.getDest_addr(),"?",rreq.getHop_count()+1);
				//Broadcast new RREQ here
				
			}
			
		}
		return 0;
	}
	
	public static int checkRREP(RREP_packet rrep)
	{
		if(isDestination(rrep.getDest_addr()))
		{
			//self is destination, route complete
		}
		else
		{
			Route present;
			if((present=routePresent(rrep.getSrc_addr()))!=null)
			{
				if(present.getHop_addr().equals("?"))
				{
					//Complete the entry, add Hop_addr to the one who sent the rrep and increment hop
					present.setHop_addr(rrep.getFrom_addr());
					RREP_packet new_rrep= new RREP_packet(rrep.getSeq_number(), rrep.getSrc_addr(),
							rrep.getDest_addr(), BluetoothManagerService.selfAddress, rrep.getHop_count()+1);
					
					//Unicast new_rrep to from_addr in table 
				}
			}
			
		}
		return 0;
	}

	
	//add new RREQ route entry
	public static void addRREQRoute(RREQ_packet rreq)
	{
		// ? Stands for not knowing next hop and -1 for not knowing sequence number
		Route r= new Route(rreq.getSeq_number(),rreq.getFrom_addr(),rreq.getDest_addr(),
				"?",-1);
		table.add(r);
	}
	
	//check if route exists for RREQ on table and return null if no and route Entry if yes
	static Route routePresent(String dest_addr)
	{

		Iterator<Route> itr=table.iterator();
		Route temp;
		for(;itr.hasNext();)
		{
			temp=(Route)itr.next();
			if(temp.getDest_addr().equals(dest_addr))
			{
				return temp;
			}
		}
				
		return null;
	}
	
	//check if current device is destination
	static boolean isDestination(String addr){
		if(BluetoothManagerService.selfAddress.equals(addr)){
			return true;
		}
		else 
			return false;
	}

}

/* Class that represents each route on the route table
 * 
 */
class Route {

	private long seq_number;
	private String req_from;
	private String dest_addr;
	private String hop_addr;
	private int numberOfHops;
	
	public Route(long l, String dest_addr,String req_from,
			String hop_addr, int numberOfHops) {
		super();
		this.seq_number = l;
		this.dest_addr = dest_addr;
		this.hop_addr = hop_addr;
		this.numberOfHops = numberOfHops;
	}
	public long getSeq_number() {
		return seq_number;
	}
	public void setSeq_number(long seq_number) {
		this.seq_number = seq_number;
	}
	public String getDest_addr() {
		return dest_addr;
	}
	public void setDest_addr(String dest_addr) {
		this.dest_addr = dest_addr;
	}
	public String getHop_addr() {
		return hop_addr;
	}
	public void setHop_addr(String hop_addr) {
		this.hop_addr = hop_addr;
	}
	public int getNumberOfHops() {
		return numberOfHops;
	}
	public void setNumberOfHops(int numberOfHops) {
		this.numberOfHops = numberOfHops;
	}
	public String getReq_from() {
		return req_from;
	}
	public void setReq_from(String req_from) {
		this.req_from = req_from;
	}

}

package com.android.BluetoothManager.Routing;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.android.BluetoothManager.Radio.BluetoothManagerService;
import com.android.BluetoothManager.Routing.Packet_types.RREQ_packet;

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
			if((present=routePresent(rreq))!=null)
			{
				//route already present, send RREP
			}
			else
			{
				//add route to table
				addRoute(rreq);
				//create new RREQ, increment hop and broadcast
				RREQ_packet new_rreq= new RREQ_packet(rreq.getSeq_number(),rreq.getSrc_addr(),
						rreq.getDest_addr(),BluetoothManagerService.selfAddress,rreq.getHop_count()+1);
				//Broadcast new RREQ here
				
			}
			
		}
		return 0;
	}
	
	//add new route entry
	public static void addRoute(RREQ_packet rreq)
	{
		//create a new route with Question mark as next_hop and incremented hop count
		Route r= new Route(rreq.getSeq_number(),rreq.getSrc_addr(),rreq.getDest_addr(),
				"?",rreq.getHop_count());
		table.add(r);
	}
	
	//check if route exists for RREQ on table and return null if no and route Entry if yes
	static Route routePresent(RREQ_packet rreq)
	{

		Iterator<Route> itr=table.iterator();
		Route temp;
		for(;itr.hasNext();)
		{
			temp=(Route)itr.next();
			if(temp.getDest_addr().equals(rreq.getDest_addr()))
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

	private Date seq_number;
	private String src_addr;
	private String dest_addr;
	private String hop_addr;
	private int numberOfHops;
	
	public Route(Date seq_number,String src_addr, String dest_addr,
			String hop_addr, int numberOfHops) {
		super();
		this.seq_number = seq_number;
		this.src_addr = src_addr;
		this.dest_addr = dest_addr;
		this.hop_addr = hop_addr;
		this.numberOfHops = numberOfHops;
	}
	public Date getSeq_number() {
		return seq_number;
	}
	public void setSeq_number(Date seq_number) {
		this.seq_number = seq_number;
	}
	public String getSrc_addr() {
		return src_addr;
	}
	public void setSrc_addr(String src_addr) {
		this.src_addr = src_addr;
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

}

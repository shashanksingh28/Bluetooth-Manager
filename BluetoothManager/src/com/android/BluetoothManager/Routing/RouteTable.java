package com.android.BluetoothManager.Routing;

import java.util.ArrayList;

import com.android.BluetoothManager.Application.BluetoothManagerService;

public class RouteTable {

	static ArrayList<Route> table;
	

	public static int addRoute(Route r,boolean isDestination) {
		if(isDestination){
			
		}
		return 0;
	}
	
	boolean routePresent(Route r){
		
		return true;
	}
	
	/*
	 * Function to check if current device is the destination
	 */
	static boolean isDestination(Route r){
		if(BluetoothManagerService.selfAddress.equals(r.getDestination())){
			return true;
		}
		else {
			return false;
		}
		
	}

}

class Route {

	private int sequenceNumber;
	private String from;
	private String source;
	private String destination;
	private String nextHop;
	private int numberOfHops;

	Route(int sequenceNumber, String from, String source, String destination, String nextHop, int numberOfHops) {
		this.sequenceNumber = sequenceNumber;
		this.from = from;
		this.source = source;
		this.destination = destination;
		this.nextHop = nextHop;
		this.numberOfHops = numberOfHops;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getNextHop() {
		return nextHop;
	}

	public void setNextHop(String nextHop) {
		this.nextHop = nextHop;
	}

	public int getNumberOfHops() {
		return numberOfHops;
	}

	public void setNumberOfHops(int numberOfHops) {
		this.numberOfHops = numberOfHops;
	}

}

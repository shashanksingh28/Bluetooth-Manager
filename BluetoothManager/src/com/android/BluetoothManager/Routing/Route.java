package com.android.BluetoothManager.Routing;


public class Route {

	private int sequenceNumber;
	private String from;
	private String source;
	private String destination;
	private String nextHop;
	private int numberOfHops;
	
	
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


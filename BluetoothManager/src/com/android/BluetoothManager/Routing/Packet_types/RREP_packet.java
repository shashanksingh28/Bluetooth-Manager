package com.android.BluetoothManager.Routing.Packet_types;

public class RREP_packet {
	
	long seq_number;
	String src_addr;
	String dest_addr;
	String from_addr;
	int hop_count;
	
	public RREP_packet(long seq_number, String src_addr, String dest_addr,
			String from_addr, int hop_count) {
		super();
		this.seq_number = seq_number;
		this.src_addr = src_addr;
		this.dest_addr = dest_addr;
		this.from_addr = from_addr;
		this.hop_count = hop_count;
	}

	public long getSeq_number() {
		return seq_number;
	}

	public void setSeq_number(long seq_number) {
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

	public String getFrom_addr() {
		return from_addr;
	}

	public void setFrom_addr(String from_addr) {
		this.from_addr = from_addr;
	}

	public int getHop_count() {
		return hop_count;
	}

	public void setHop_count(int hop_count) {
		this.hop_count = hop_count;
	}
	
	

}

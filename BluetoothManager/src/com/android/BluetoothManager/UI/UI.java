package com.android.BluetoothManager.UI;

import android.app.TabActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class UI extends TabActivity {
	
	
	private final String TAG = "UI";
	
	//Receiver for UI packets.
	UIPacketReceiver ui_packet_receiver;
	
	//Action String for receiving packets at UI layer.
	private final String UI_PACKET_RECEIVED = "com.android.BluetoothManager.UI_PACKET_RECEIVED";
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui);

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; 			// Resusable TabSpec for each tab
		Intent intent; 					// Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, MessageUI.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost
				.newTabSpec("message")
				.setIndicator("Message", res.getDrawable(R.drawable.ic_tab_msg))
				.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, ChatUI.class);
		spec = tabHost.newTabSpec("chat")
				.setIndicator("Chat", res.getDrawable(R.drawable.ic_tab_chat))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, FTPUI.class);
		spec = tabHost.newTabSpec("ftp")
				.setIndicator("FTP", res.getDrawable(R.drawable.ic_tab_ftp))
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
		
		
		/*
		 * Instatiating the Reciver and registering it.
		 */
		ui_packet_receiver = new UIPacketReceiver();
		registerReceiver(ui_packet_receiver, new IntentFilter());
	}
}
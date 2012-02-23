package com.android.BluetoothManager.UI;

import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

public class UI extends TabActivity {

	private static final int DEVICE_LIST_ACTIVITY = 0;

	private final String TAG = "UI";

	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui);
		
		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

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
		
		
		/*****TESTING*****/  
		// the rest for just testing purpose
		String address = BluetoothAdapter.getDefaultAdapter().getAddress();//((BluetoothManagerApplication)getApplication()).getSelfAddress();
		//Toast.makeText(this, address, Toast.LENGTH_SHORT).show();
		if (address.equals("F4:9F:54:58:11:6A")) {
			Toast.makeText(this, "Sending msg after 5 secs awrsd", Toast.LENGTH_SHORT)
					.show();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Log.d(TAG, "++ Sleep Interrupted ++");
			}

			
			// Temporarily sending fake data:
			String destination = "7C:61:93:B7:54:CD";// data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
			String ACTION = getResources().getString(R.string.UI_TO_ROUTING);
			Intent i = new Intent();
			i.setAction(ACTION);
			i.putExtra("layer", "UI");
			i.putExtra("type", "singlehop"); // to skip routing while testing
												// the radio layer.
			i.putExtra("device", destination);
			i.putExtra("msg", "4,7C:61:93:B7:54:CD,This is a test msg !!");
			sendBroadcast(i);
		}

		// startActivityForResult(new Intent(this,DeviceListActivity.class),
		// DEVICE_LIST_ACTIVITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		if ((requestCode == DEVICE_LIST_ACTIVITY)
//				&& (resultCode == Activity.RESULT_OK)) {
//			String destination = "7C:61:93:B7:54:CD";// data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//			String ACTION = getResources().getString(R.string.UI_TO_ROUTING);
//			Intent i = new Intent();
//			i.setAction(ACTION);
//			i.putExtra("layer", "UI");
//			i.putExtra("device", destination);
//			i.putExtra("msg", "This is a test msg !!");
//			sendBroadcast(i);
//		}
	}
}
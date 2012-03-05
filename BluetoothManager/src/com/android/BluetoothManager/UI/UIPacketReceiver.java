package com.android.BluetoothManager.UI;

import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;

public class UIPacketReceiver extends BroadcastReceiver {

	public HashMap<String, ArrayAdapter<String>> conversation_map;

	private final String TAG = "UIPacketReceiver";

	private String MSG_TYPE = "msg";
	private String CHAT_TYPE = "chat";

	private int NOTIFICATION_ID = 1;

	// Members related to ChatUI
	public ViewPagerAdapter adapter;

	String notification_string = Context.NOTIFICATION_SERVICE;
	NotificationManager notification_service;

	BluetoothManagerApplication bluetooth_manager;

	public UIPacketReceiver(BluetoothManagerApplication bluetooth_manager) {
		this.bluetooth_manager = bluetooth_manager;
		notification_service = (NotificationManager) bluetooth_manager
				.getSystemService(notification_string);

		conversation_map = new HashMap<String, ArrayAdapter<String>>();

		// Related to ChatUIs
		adapter = new ViewPagerAdapter(
				this.bluetooth_manager.getApplicationContext(),
				conversation_map);
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String device = intent.getStringExtra("device");
		String name = intent.getStringExtra("name");
		String msg = intent.getStringExtra("msg");

		Log.d(TAG, "Received msg:" + msg + " from:" + device);
		
		String dataType = msg.substring(0, msg.indexOf(","));

		if (dataType.equals(MSG_TYPE)) {
			processMsgData(device, name, msg.substring(msg.indexOf(",") + 1));
			return;
		}
		if (dataType.equals(CHAT_TYPE)) {
			processChatData(device, name, msg.substring(msg.indexOf(",") + 1));
			return;
		}

	}

	private void processChatData(String device, String name, String msg) {
		if (conversation_map.containsKey(device)) {
			Log.d(TAG, "Device found: " + device);
			ArrayAdapter<String> chatAdapter = conversation_map.get(device);
			Log.d(TAG, "chatAdapter:" + chatAdapter);
			chatAdapter.add(name + ": " + msg);
			chatAdapter.notifyDataSetChanged();
		} else {
			adapter.addDevice(device, name, msg);
			adapter.notifyDataSetChanged();
		}
	}

	private void processMsgData(String device, String name, String msg) {
		setNotification("New message from: " + name, name, msg);
	}

	public void setNotification(String ticker, String title, String text) {

		int icon = R.drawable.ic_menu_dialog;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, ticker, when);
		Context context = bluetooth_manager.getApplicationContext();
		Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		notificationIntent.setType("vnd.android-dir/mms-sms");
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, title, text, contentIntent);
		notification_service.notify(NOTIFICATION_ID, notification);

	}
}

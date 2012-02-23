package com.android.BluetoothManager.UI;

import java.util.HashMap;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;

public class UIPacketReceiver extends BroadcastReceiver {

	HashMap<String, ArrayAdapter<String>> chat_adapters;

	private final String TAG = "UIPacketReceiver";

	private String MSG_TYPE = "msg";
	private String CHAT_TYPE = "chat";
	
	String notification_string = Context.NOTIFICATION_SERVICE;
	NotificationManager notification_service;
	

	BluetoothManagerApplication bluetooth_manager;

	public UIPacketReceiver(BluetoothManagerApplication bluetooth_manager) {
		this.bluetooth_manager = bluetooth_manager;
		notification_service = (NotificationManager) bluetooth_manager.getSystemService(notification_string);

	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String device = intent.getStringExtra("device");
		String name = intent.getStringExtra("name");
		String msg = intent.getStringExtra("msg");

		Log.d(TAG, "Received msg:" + msg + " from:" + device);
		String packs[] = msg.split(",");
		String dataType = packs[0];

		if (dataType.equals(MSG_TYPE)) {
			processMsgData(device, name, msg);
			return;
		}
		if (dataType.equals(CHAT_TYPE)) {
			processChatData(device, name, msg);
			return;
		}

	}

	private void processChatData(String device, String name, String msg) {

	}

	private void processMsgData(String device, String name, String msg) {

	}

	public void setNotification(String ticker, String title, String text) {

//		int icon = R.drawable.ic_menu_dialog;
//		long when = System.currentTimeMillis();
//		Notification notification = new Notification(icon, ticker, when);
//		Context context = bluetooth_manager.getApplicationContext();
//		Intent notificationIntent = new Intent(this,NotificationsActivity.class);
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//				notificationIntent, 0);
//
//		notification.setLatestEventInfo(context, title, text, contentIntent);
//		notification_service.notify(1, notification);

	}
}

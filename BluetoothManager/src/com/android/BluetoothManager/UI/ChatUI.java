package com.android.BluetoothManager.UI;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;
import com.android.BluetoothManager.UI.viewpager.TitlePageIndicator;

public class ChatUI extends BaseActivity {

	ViewPagerAdapter adapter;
	ViewPager pager;
	TitlePageIndicator indicator;
	BluetoothManagerApplication bluetooth_manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_ui);
		bluetooth_manager = (BluetoothManagerApplication) getApplication();
		adapter = bluetooth_manager.ui_packet_receiver.adapter;
		indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		pager = (ViewPager) findViewById(R.id.viewpager);
		pager.setAdapter(adapter);
		indicator.setViewPager(pager);
		adapter.notifyDataSetChanged();
		indicator.notifyDataSetChanged();
	}

}

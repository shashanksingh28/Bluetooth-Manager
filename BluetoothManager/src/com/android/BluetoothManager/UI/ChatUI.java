package com.android.BluetoothManager.UI;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.android.BluetoothManager.UI.viewpager.TitlePageIndicator;

public class ChatUI extends Activity {
	
	ViewPagerAdapter adapter;
	ViewPager pager;
	TitlePageIndicator indicator;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_ui);
		adapter = new ViewPagerAdapter(this);
		indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		pager = (ViewPager) findViewById(R.id.viewpager);
		
		pager.setAdapter(adapter);
		indicator.setViewPager(pager);
		
		adapter.addNewView(pager,null);
		adapter.notifyDataSetChanged();
		indicator.notifyDataSetChanged();
	}

}

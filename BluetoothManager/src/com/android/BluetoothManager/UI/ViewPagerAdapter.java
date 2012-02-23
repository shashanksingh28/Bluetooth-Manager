package com.android.BluetoothManager.UI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.BluetoothManager.UI.viewpager.TitleProvider;

public class ViewPagerAdapter extends PagerAdapter implements TitleProvider {
	
	int count = 0;
	private final Context context;
	private final String TAG = "ViewPagerAdapter";

	public ViewPagerAdapter(Context context) {
		Log.d(TAG, " +++ Constructor Called");
		this.context = context;
	}

	@Override
	public String getTitle(int position) {
		Log.d(TAG, " +++ getTitle() Called");
		return "Person X";
	}

	@Override
	public int getCount() {
		Log.d(TAG, " +++ getCount() Called");
		return 3;
	}

	@Override
	public Object instantiateItem(ViewGroup pager, int position) {
		Log.d(TAG, " +++ instantiateItem Called");
		ListView v = new ListView(context);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.chat_msg);
		v.setAdapter(adapter);
		adapter.add("me: Hello");
		adapter.add("aru: hi !! ");
		((ViewPager) pager).addView(v, 0);
		return v;
		
	}
	public void addNewView(ViewGroup pager,ListView lv){
		//((ViewPager) pager).addView(lv, 0);
		count++;
	}
	
	public void setCount(){
		
	}

	@Override
	public void destroyItem(View pager, int position, Object view) {
		Log.d(TAG, " +++ destroyItem Called");
		((ViewPager) pager).removeView((ListView) view);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		Log.d(TAG, " +++ isViewFromObject Called");
		return view.equals(object);
	}

	@Override
	public void finishUpdate(View view) {
		Log.d(TAG, " +++ finishUpdate Called");
	}

	@Override
	public void restoreState(Parcelable p, ClassLoader c) {
		Log.d(TAG, " +++ restoreState Called");
	}

	@Override
	public Parcelable saveState() {
		Log.d(TAG, " +++ saveState Called");
		return null;
	}

	@Override
	public void startUpdate(View view) {
		Log.d(TAG, " +++ startUpdate Called");
	}
}


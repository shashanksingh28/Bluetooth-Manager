package com.android.BluetoothManager.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends Activity {

	private String TAG = "BaseActivity";
	int GET_DEVICE_FOR_CHAT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		Log.d(TAG, "Base Activity: on Create Options!!");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_startChat:
			Log.d(TAG, "Start New Chat");
			startActivityForResult(new Intent(this, DeviceListActivity.class),
					GET_DEVICE_FOR_CHAT);
			break;
		default:
			break;
		}
		Log.d(TAG, "Base Activity: On Option Item Selected !!");
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GET_DEVICE_FOR_CHAT && resultCode == RESULT_OK) {
			String device = data
					.getStringExtra(DeviceListActivity.DEVICE_ADDRESS);
			Toast.makeText(this, device, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.setAction(getResources().getString(R.string.UI_TO_ROUTING));
			intent.putExtra("layer", "UI");
			intent.putExtra("device", device);
			intent.putExtra("msg", "chat,Hello RREQ");
			sendBroadcast(intent);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

}

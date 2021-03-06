package com.android.BluetoothManager.UI;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MessageUI extends BaseActivity {

	Button msg_send;
	EditText msg_input;
	int GET_DEVICE_FOR_MSG = 0;
	BluetoothManagerApplication bluetooth_manager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_ui);
		msg_send = (Button)findViewById(R.id.msg_button_send);
		msg_input = (EditText)findViewById(R.id.msg_text_input);
		
		bluetooth_manager = (BluetoothManagerApplication)getApplication();
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public void sendMsg(View v){
		startActivityForResult(new Intent(this,DeviceListActivity.class), GET_DEVICE_FOR_MSG);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == GET_DEVICE_FOR_MSG && resultCode == RESULT_OK){
			String device = data.getStringExtra(DeviceListActivity.DEVICE_ADDRESS);
			String msg = "msg,"+msg_input.getText().toString();
			Toast.makeText(this, device, Toast.LENGTH_SHORT).show();
			bluetooth_manager.sendDataToRoutingFromUI(device, msg);
			
		}
		
	}
	
	
}


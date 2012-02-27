package com.android.BluetoothManager.UI;

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
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_ui);
		msg_send = (Button)findViewById(R.id.msg_button_send);
		msg_input = (EditText)findViewById(R.id.msg_text_input);
	}

	public void sendMsg(View v){
		startActivityForResult(new Intent(this,DeviceListActivity.class), GET_DEVICE_FOR_MSG);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == GET_DEVICE_FOR_MSG && resultCode == RESULT_OK){
			String device = data.getStringExtra(DeviceListActivity.DEVICE_ADDRESS);
			Toast.makeText(this, device, Toast.LENGTH_SHORT).show();
			Intent intent=new Intent();
			intent.setAction(getResources().getString(R.string.UI_TO_ROUTING));
			intent.putExtra("layer", "UI");
			intent.putExtra("device", device);
			intent.putExtra("msg", "msg,Hello RREQ");
			sendBroadcast(intent);
		}
		
	}
	
	
}


import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.android.BluetoothManager.Radio.Connection;
import com.android.BluetoothManager.Radio.Connection.OnConnectionLostListener;
import com.android.BluetoothManager.Radio.Connection.OnConnectionServiceReadyListener;
import com.android.BluetoothManager.Radio.Connection.OnIncomingConnectionListener;
import com.android.BluetoothManager.Radio.Connection.OnMaxConnectionsReachedListener;
import com.android.BluetoothManager.Radio.Connection.OnMessageReceivedListener;
import com.android.BluetoothManager.Routing.PacketReceiver;

/*
 * Service that starts server and initializes the radio layer
 * To be started after Application object is initialized
 */
public class BluetoothManagerService extends Service{

	//Maximum number of bluetooth connections allowed
	private static final int MAX_CONNECTIONS = 7;
	
	

	//Packet Reciever object
	PacketReceiver packet_receiver;
	
	static final String PACKET_RECEIVE_INTENT = "com.android.BluetoothManager.PACKET_RECEIVED";

	private final String TAG="com.android.BluetoothManager.Service";
	
	//Connection object which initiates the radio level
	Connection connection;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		
		super.onCreate();
		//Initialization of connection
		connection = new Connection(this,serviceReadyListener);
		// Instantiate the PacketReciever
		packet_receiver= new PacketReceiver();
		
		//register the PacketReciever to listen
		registerReceiver(packet_receiver, new IntentFilter(PACKET_RECEIVE_INTENT));
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		//start Server
		startServer();
		return 0;
	}
	
	/* Listeners to be Called back by framework on events
	 * Radio layer calls these defined functions on registered events
	*/
	private OnConnectionServiceReadyListener serviceReadyListener = new OnConnectionServiceReadyListener() {
		public void OnConnectionServiceReady() {
		}
	};
	
	//Callback function for any bluetooth message recieved from this application
	private OnMessageReceivedListener dataReceivedListener = new OnMessageReceivedListener() {
		@Override
		public void OnMessageReceived(String device, String message) {
			Log.d(TAG, "Message Received:" + message + " from:" + device);
			Intent intent = new Intent(PACKET_RECEIVE_INTENT);
			intent.putExtra("device", device);
			intent.putExtra("msg", message);
			
			//Broadcast that a bluetooth message has been recieved,the device and the message as extras
			sendBroadcast(intent);
			//blueman.ui_handler.obtainMessage(BlueManApplication.MESSAGE_READ, "Message Received:" + message + " from:" + device).sendToTarget();
			// print("Message Received:" + message + "from" + device);
		}
	};
	
	//Function that starts the bluetooth server of the radio layer
	public void startServer() {
		Log.d(TAG, "Trying to start server !!");
		int result = connection.startServer(MAX_CONNECTIONS, connectedListener,
				maxConnectionsListener, dataReceivedListener,
				disconnectedListener);
		if (result == Connection.FAILURE) {
			Log.d(TAG, "Server starting failed !!");
		}
		Log.d(TAG, "Server Started !!");
	}
	
	private OnMaxConnectionsReachedListener maxConnectionsListener = new OnMaxConnectionsReachedListener() {
		public void OnMaxConnectionsReached() {
			Log.d(TAG, "Max Connections Reached !");
		}
	};

	private OnIncomingConnectionListener connectedListener = new OnIncomingConnectionListener() {
		public void OnIncomingConnection(String device) {
			Log.d(TAG, "Incoming Connection from: " + device);
		}
	};

	private OnConnectionLostListener disconnectedListener = new OnConnectionLostListener() {
		public void OnConnectionLost(String device) {
			Log.d(TAG, "Connection Lost for: " + device);
		}
	};

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		connection.shutdown();
		connection=null;
	}
}

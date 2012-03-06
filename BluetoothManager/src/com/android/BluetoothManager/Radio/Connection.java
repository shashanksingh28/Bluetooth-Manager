package com.android.BluetoothManager.Radio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.util.Log;

import com.android.BluetoothManager.UI.R;

public class Connection {

	private static final String TAG = "Connection";

	public static final int MAX_CONNECTIONS_SUPPORTED = 7;

	public static final int SUCCESS = 0;

	public static final int FAILURE = 1;

	private boolean server_started = false;

	private BluetoothAdapter BtAdapter;

	private String service_name = "BluetoothManagerService"; // Random String
																// used for
																// starting
																// server.

	private ArrayList<UUID> Uuids; // List of UUID's

	private ArrayList<String> BtConnectedDeviceAddresses; // List of addresses to which
													// the devices are currently
													// connected

	private HashMap<String, BluetoothSocket> BtSockets; // Mapping between
														// address and the
														// corresponding Scoket

	private HashMap<String, String> BtFoundDevices; // Mapping between the
													// devices and the names.
													// this list to be passed to
													// the UI layer.contains
													// only found devices

	private HashMap<String, String> BtBondedDevices; // Mapping between the
														// devices and the
														// names. this list to
														// be passed to the UI
														// layer. contains only
														// Bonded devices

	private HashMap<String, Thread> BtStreamWatcherThreads;

	Object lock;

	Context app_context;
	
	private long lastDiscovery=0; // Stores the time of the last discovery

	private boolean isSending = false;

	Connection(Context context) {

		BtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (BtAdapter != null) {
			BtAdapter.enable();
		}

		BtSockets = new HashMap<String, BluetoothSocket>();

		BtConnectedDeviceAddresses = new ArrayList<String>();

		BtBondedDevices = new HashMap<String, String>();

		BtFoundDevices = new HashMap<String, String>();

		BtStreamWatcherThreads = new HashMap<String, Thread>();

		lock = new Object();

		app_context = context;

		Uuids = new ArrayList<UUID>();
		// Allow up to 7 devices to connect to the server
		Uuids.add(UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"));
		Uuids.add(UUID.fromString("503c7430-bc23-11de-8a39-0800200c9a66"));
		Uuids.add(UUID.fromString("503c7431-bc23-11de-8a39-0800200c9a66"));
		Uuids.add(UUID.fromString("503c7432-bc23-11de-8a39-0800200c9a66"));
		Uuids.add(UUID.fromString("503c7433-bc23-11de-8a39-0800200c9a66"));
		Uuids.add(UUID.fromString("503c7434-bc23-11de-8a39-0800200c9a66"));
		Uuids.add(UUID.fromString("503c7435-bc23-11de-8a39-0800200c9a66"));

		// Registration for Bluetooth Events.
		IntentFilter i = new IntentFilter();
		i.addAction(BluetoothDevice.ACTION_FOUND);
		i.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		app_context.registerReceiver(receiver, i);

	}

	public int startServer() {
		if (server_started) {
			return Connection.FAILURE;
		}
		if (BtAdapter.isEnabled()) {
			(new Thread(new ConnectionWaiter())).start();
			Log.d(TAG, " ++ Server Started ++");
			server_started=true;
			return Connection.SUCCESS;
		}
		return Connection.FAILURE;
	}

	private class BtStreamWatcher implements Runnable {
		private String address;

		public BtStreamWatcher(String deviceAddress) {
			address = deviceAddress;
		}

		public void run() {
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			BluetoothSocket bSock = BtSockets.get(address);
			try {
				InputStream instream = bSock.getInputStream();
				int bytesRead = -1;
				String message = "";
				while (true) {
					message = "";
					bytesRead = instream.read(buffer);
					if (bytesRead != -1) {
						while ((bytesRead == bufferSize)
								&& (buffer[bufferSize - 1] != 0)) {
							message = message
									+ new String(buffer, 0, bytesRead);
							bytesRead = instream.read(buffer);
						}
						message = message
								+ new String(buffer, 0, bytesRead - 1); // Remove
						// the
						// stop
						// marker

						// Intent to be changed in future.
						Log.d(TAG,"Received "+message+" from "+address+"In Connection");
						String ACTION = app_context.getResources().getString(
								R.string.RADIO_TO_ROUTING);
						Intent i = new Intent();
						i.setAction(ACTION);
						i.putExtra("layer", "radio");
						i.putExtra("device", address);
						i.putExtra("msg", message);
						app_context.sendBroadcast(i);
						Log.d(TAG,"Intent Send from Radio to routing");
						
					}
				}
			} catch (IOException e) {
				Log.i(TAG,
						"IOException in BtStreamWatcher - probably caused by normal disconnection",
						e);
				Log.d(TAG,"Closing Thread since probably disconnected");
				
			}
			// Getting out of the while loop means the connection is dead.
			try {
				BtConnectedDeviceAddresses.remove(address);
				BtSockets.remove(address);
				BtStreamWatcherThreads.remove(address);

			} catch (Exception e) {
				Log.e(TAG, "Exception in BtStreamWatcher while disconnecting",
						e);
			}
		}
	}

	/*
	 * This class is responsible for listening for new connections. Once a
	 * connections is accepted, a new thread is created to manage the i/p, o/p
	 * with the newly established connection
	 */
	private class ConnectionWaiter implements Runnable {

		public ConnectionWaiter() {

		}

		public void run() {
			try {
				for (int i = 0; i < Connection.MAX_CONNECTIONS_SUPPORTED; i++) {
					BluetoothServerSocket myServerSocket = BtAdapter
							.listenUsingRfcommWithServiceRecord(service_name,
									Uuids.get(i));
					BluetoothSocket myBSock = myServerSocket.accept();
					myServerSocket.close(); // Close the socket now that the
					// connection has been made.

					String address = myBSock.getRemoteDevice().getAddress();
					// String name = myBSock.getRemoteDevice().getName();

					BtSockets.put(address, myBSock);
					BtConnectedDeviceAddresses.add(address);
					Thread BtStreamWatcherThread = new Thread(
							new BtStreamWatcher(address));
					BtStreamWatcherThread.start();
					BtStreamWatcherThreads.put(address, BtStreamWatcherThread);

				}

			} catch (IOException e) {
				Log.i(TAG, "IOException in ConnectionService:ConnectionWaiter",
						e);
			}
		}
	}

	private int connect(String device) throws RemoteException {

		Log.d(TAG, "Trying to connect to: " + device);
		if (BtConnectedDeviceAddresses.contains(device)) {
			Log.d(TAG, "Already connected to: " + device);
			return Connection.SUCCESS;
		}

		BluetoothDevice myBtServer = BtAdapter.getRemoteDevice(device);

		BluetoothSocket myBSock = null;

		Log.d(TAG, "Creating Sockets");

		for (int i = 0; i < Connection.MAX_CONNECTIONS_SUPPORTED
				&& myBSock == null; i++) {
			myBSock = getConnectedSocket(myBtServer, Uuids.get(i));
			Log.d(TAG, "After getConnectedSocket(): " + myBSock);
			if (myBSock == null) {
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					Log.e(TAG, "InterruptedException in connect", e);
				}
			} else {
				break;
			}
		}
		if (myBSock == null) {
			return Connection.FAILURE;
		}

		BtSockets.put(device, myBSock);
		BtConnectedDeviceAddresses.add(device);
		Thread BtStreamWatcherThread = new Thread(new BtStreamWatcher(device));
		BtStreamWatcherThread.start();
		BtStreamWatcherThreads.put(device, BtStreamWatcherThread);
		return Connection.SUCCESS;
	}

	private BluetoothSocket getConnectedSocket(BluetoothDevice myBtServer,
			UUID uuidToTry) {
		BluetoothSocket myBSock;
		try {
			myBSock = myBtServer.createRfcommSocketToServiceRecord(uuidToTry);
			Log.d(TAG,
					"Trying to connect to socket of:" + myBtServer.getAddress());
			myBSock.connect();
			return myBSock;
		} catch (IOException e) {
			Log.i(TAG,
					"IOException in getConnectedSocket. Msg:" + e.getMessage());
		}
		return null;
	}

	public int broadcastMessage(String message) throws RemoteException {

		appStartDiscovery();
		connectToFoundDevices();
		int size = BtConnectedDeviceAddresses.size();
		for (int i = 0; i < size; i++) {
			sendMessageToDestination(BtConnectedDeviceAddresses.get(i), message);
		}
		return Connection.SUCCESS;
	}

	public String getConnections(String srcApp) throws RemoteException {

		String connections = "";
		int size = BtConnectedDeviceAddresses.size();
		for (int i = 0; i < size; i++) {
			connections = connections + BtConnectedDeviceAddresses.get(i) + ",";
		}
		return connections;
	}

	public int sendMessageToDestination(String destination, String message)
			throws RemoteException {

		int status = connect(destination);

		if (status == Connection.SUCCESS) {
			try {
				BluetoothSocket myBsock = BtSockets.get(destination);
				if (myBsock != null) {
					OutputStream outStream = myBsock.getOutputStream();
					byte[] stringAsBytes = (message + " ").getBytes();
					stringAsBytes[stringAsBytes.length - 1] = 0; // Add a stop
					// marker
					outStream.write(stringAsBytes);
					return Connection.SUCCESS;
				}
			} catch (IOException e) {
				Log.i(TAG, "IOException in sendMessage - Dest:" + destination
						+ ", Msg:" + message, e);
			}
		}
		return Connection.FAILURE;
	}

	public void shutdown(String srcApp) throws RemoteException {
		try {
			int size = BtConnectedDeviceAddresses.size();
			for (int i = 0; i < size; i++) {
				BluetoothSocket myBsock = BtSockets.get(BtConnectedDeviceAddresses
						.get(i));
				myBsock.close();
			}
			BtSockets = new HashMap<String, BluetoothSocket>();
			BtStreamWatcherThreads = new HashMap<String, Thread>();
			BtConnectedDeviceAddresses = new ArrayList<String>();

		} catch (IOException e) {
			Log.i(TAG, "IOException in shutdown", e);
		}
	}

	public String getAddress() throws RemoteException {
		return BtAdapter.getAddress();
	}

	public String getName() throws RemoteException {
		return BtAdapter.getName();
	}

	/*
	 * 
	 */
	public HashMap<String, String> getConnectableDevices() {

		Set<BluetoothDevice> devices = BtAdapter.getBondedDevices();
		for (BluetoothDevice device : devices) {
			BtBondedDevices.put(device.getAddress(), device.getName());
		}
		if (BtAdapter.isDiscovering()) {
			BtAdapter.cancelDiscovery();
		}
		Log.d(TAG, "Starting Discovery !!");
		appStartDiscovery();
		return BtBondedDevices;
	}

	public void connectToFoundDevices() {
		Log.d(TAG, "connectToFoundDevices() called");
		Iterator devices = BtFoundDevices.entrySet().iterator();
		while (devices.hasNext()) {
			Map.Entry<String, String> device = (Map.Entry<String, String>) devices
					.next();
			try {
				connect(device.getKey());
			} catch (RemoteException e) {
				Log.d(TAG, "Couldn't connect to " + device.getKey());
			}
		}
	}

	/*
	 * Discover devices only if the last discovery was made a minute ago.
	 */
	public void appStartDiscovery() {
		if(BtAdapter.isDiscovering()){
			return;
		}
		if(System.currentTimeMillis()/1000 - lastDiscovery > 60){
			BtAdapter.startDiscovery();
			lastDiscovery = System.currentTimeMillis()/1000;
			try {
				Thread.sleep(12000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				BtFoundDevices.put(device.getAddress(), device.getName());
				Log.d(TAG, "Found " + device.getAddress());
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				Log.d(TAG,"Service Discovery Finished !");
			}
		}
	};

}

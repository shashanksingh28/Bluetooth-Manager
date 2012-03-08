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
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.util.Log;

import com.android.BluetoothManager.Application.BluetoothManagerApplication;
import com.android.BluetoothManager.Routing.RouteTable;
import com.android.BluetoothManager.UI.R;

public class Connection {

	private static final String TAG = "Connection";

	public static final int MAX_CONNECTIONS_SUPPORTED = 7;

	public static final int SUCCESS = 0;

	public static final int FAILURE = 1;

	private boolean server_started = false;

	BluetoothAdapter BtAdapter;

	String service_name = "BluetoothManagerService"; // Random String
																// used for
																// starting
																// server.

	ArrayList<UUID> Uuids; // List of UUID's

	ArrayList<String> BtConnectedDeviceAddresses; // List of addresses
															// to which
	// the devices are currently
	// connected

	HashMap<String, BluetoothSocket> BtSockets; // Mapping between
														// address and the
														// corresponding Scoket

	HashMap<String, String> BtFoundDevices; // Mapping between the
													// devices and the names.
													// this list to be passed to
													// the UI layer.contains
													// only found devices

	HashMap<String, String> BtBondedDevices; // Mapping between the
														// devices and the
														// names. this list to
														// be passed to the UI
														// layer. contains only
														// Bonded devices

	HashMap<String, Thread> BtStreamWatcherThreads;

	BluetoothManagerApplication bluetooth_manager;

	private long lastDiscovery = 0; // Stores the time of the last discovery

	public Connection(BluetoothManagerApplication bluetooth_manager) {

		Log.d(TAG,"Started at");
		BtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (BtAdapter != null) {
			BtAdapter.enable();
		}

		this.bluetooth_manager = bluetooth_manager;

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
		i.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		bluetooth_manager.registerReceiver(receiver, i);

	}

	public int startServer() {
		if (server_started) {
			return Connection.FAILURE;
		}
		if (BtAdapter.isEnabled()) {
			(new Thread(new ConnectionWaiter())).start();
			Log.d(TAG, " ++ Server Started ++");
			server_started = true;
			return Connection.SUCCESS;
		}
		return Connection.FAILURE;
	}

	

	/* Function that will try to establish a connection 
	 * 
	 */
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

	public String getAddress() throws RemoteException {
		return BtAdapter.getAddress();
	}

	public String getName() throws RemoteException {
		return BtAdapter.getName();
	}

	public BluetoothAdapter getBluetoothAdapter() {
		return BtAdapter;
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
		if (BtAdapter.isDiscovering()) {
			return;
		}
		if (System.currentTimeMillis() / 1000 - lastDiscovery > 60) {
			BtAdapter.startDiscovery();
			lastDiscovery = System.currentTimeMillis() / 1000;
			try {
				Thread.sleep(12000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void makeDeviceDisocverable() {
		Log.d(TAG,"Making Device Discoverable.");
		Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		bluetooth_manager.startActivity(i);
		Log.d(TAG,"Made Device Discoverable");
	}
	
	/* Function called when Bluetooth will be turned off
	 * Stops the thread which listens for other connections
	 * Cleans up resources, removes the threads for GC
	 * and make the Routing thread wait till it is started again
	 */
	private void stopRadio()
	{
		try {
			int size = BtConnectedDeviceAddresses.size();
			for (int i = 0; i < size; i++) {
				BluetoothSocket myBsock = BtSockets
						.get(BtConnectedDeviceAddresses.get(i));
				myBsock.close();
			}
			BtSockets = null;// new HashMap<String, BluetoothSocket>();
			BtStreamWatcherThreads = null;// new HashMap<String, Thread>();
			BtConnectedDeviceAddresses = null;// new ArrayList<String>();
			BtFoundDevices = null;// new ArrayList<String>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* Function to be called when Bluetooth will be turned on
	 * Will instantiate the threads and start the main Server thread that
	 * listens to other connections
	 */
	private void startRadio()
	{
		BtSockets = new HashMap<String, BluetoothSocket>();

		BtConnectedDeviceAddresses = new ArrayList<String>();

		BtBondedDevices = new HashMap<String, String>();

		BtFoundDevices = new HashMap<String, String>();

		BtStreamWatcherThreads = new HashMap<String, Thread>();

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
				BluetoothClass bc = device.getBluetoothClass();
				if (bc.getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE) {
					BtFoundDevices.put(device.getAddress(), device.getName());
				}
				Log.d(TAG, "Found " + device.getAddress());

			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				// Do something when the search finishes.
				Log.d(TAG, "Service Discovery Finished !");
			} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				/*
				 * Check if Local bluetooth adapter is on or off and make
				 * changes accordingly.
				 */

				String state = intent.getStringExtra(BtAdapter.EXTRA_STATE);
				if (state.equals(BtAdapter.STATE_TURNING_OFF)) {
					stopRadio();
				} else {
					if (state.equals(BtAdapter.STATE_ON)) {
						//startService();
					}
				}

			}
		}
	};


	/* This class is responsible for listening for new connections. Once a
	 * connections is accepted, a new thread is created to manage the i/p, o/p
	 * with the newly established connection
	 */
	private class ConnectionWaiter implements Runnable {

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
	
	/* Thread which maintains the I/O of
	 * one stream for one device
	 */
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
								+ new String(buffer, 0, bytesRead - 1); 

						Log.d(TAG, "Received " + message + " from " + address
								+ "In Connection");
						String ACTION = bluetooth_manager.getResources()
								.getString(R.string.RADIO_TO_ROUTING);
						Intent i = new Intent();
						i.setAction(ACTION);
						i.putExtra("layer", "radio");
						i.putExtra("device", address);
						i.putExtra("msg", message);
						bluetooth_manager.sendBroadcast(i);
						Log.d(TAG, "Intent Send from Radio to routing");

					}
				}
			} catch (IOException e) {
				Log.i(TAG,
						"IOException in BtStreamWatcher - probably caused by normal disconnection",
						e);
				Log.d(TAG, "Closing Thread since probably disconnected");

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
}

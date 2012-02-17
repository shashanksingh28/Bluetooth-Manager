package com.android.BluetoothManager.Radio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
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

	private String service_name = "BluetoothManagerService";

	private ArrayList<UUID> Uuids;

	private HashMap<String, BluetoothSocket> BtSockets;

	public ArrayList<String> BtDeviceAddresses;

	private HashMap<String, Thread> BtStreamWatcherThreads;

	Object lock;

	Context app_context;

	private boolean isSending = false;

	Connection(Context context) {
		BtAdapter = BluetoothAdapter.getDefaultAdapter();

		BtSockets = new HashMap<String, BluetoothSocket>();

		BtDeviceAddresses = new ArrayList<String>();

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

	}

	public int startServer() {
		if (server_started) {
			return Connection.FAILURE;
		}
		if (BtAdapter.isEnabled()) {
			(new Thread(new ConnectionWaiter())).start();
			Log.d(TAG," ++ Server Started ++");
//			Intent i = new Intent();
//			i.setClass(app_context, StartDiscoverableModeActivity.class);
//			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			app_context.startActivity(i);
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
						String ACTION = app_context.getResources().getString(R.string.RADIO_TO_ROUTING);
						Intent i = new Intent();
						i.setAction(ACTION);
						i.putExtra("layer", "radio");
						i.putExtra("device", address);
						i.putExtra("msg", message);
						app_context.sendBroadcast(i);
					}
				}
			} catch (IOException e) {
				Log.i(TAG,
						"IOException in BtStreamWatcher - probably caused by normal disconnection",
						e);
			}
			// Getting out of the while loop means the connection is dead.
			try {
				BtDeviceAddresses.remove(address);
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
					BtDeviceAddresses.add(address);
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

		BluetoothDevice myBtServer = BtAdapter.getRemoteDevice(device);

		BluetoothSocket myBSock = null;

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
		BtDeviceAddresses.add(device);
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
			myBSock.connect();
			return myBSock;
		} catch (IOException e) {
			Log.i(TAG, "IOException in getConnectedSocket", e);
		}
		return null;
	}

	public int broadcastMessage(String message) throws RemoteException {
		int size = BtDeviceAddresses.size();
		for (int i = 0; i < size; i++) {
			sendMessageToDestination(BtDeviceAddresses.get(i), message);
		}
		return Connection.SUCCESS;
	}

	public String getConnections(String srcApp) throws RemoteException {

		String connections = "";
		int size = BtDeviceAddresses.size();
		for (int i = 0; i < size; i++) {
			connections = connections + BtDeviceAddresses.get(i) + ",";
		}
		return connections;
	}

	private int sendMessageToDestination(String destination, String message)
			throws RemoteException {

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

		return Connection.FAILURE;
	}

	@SuppressWarnings("finally")
	public int sendMessage(String destination, String message) {

		/*
		 * Check if device is already connected if yes, send message using the
		 * existing connection. if no, create a new connection and send the msg.
		 */
		int send_status = Connection.FAILURE;
		try {
			if (BtDeviceAddresses.contains(destination)) {
				send_status = sendMessageToDestination(destination, message);
			} else {
				int connect_status = connect(destination);
				if (connect_status == Connection.SUCCESS) {
					send_status = sendMessageToDestination(destination, message);
				} else {
					send_status = Connection.FAILURE;
				}
			}
		} catch (RemoteException e) {
			Log.d(TAG, e.getMessage());
		} finally {
			isSending = false;
			return send_status;
		}

	}

	public void shutdown(String srcApp) throws RemoteException {
		try {
			int size = BtDeviceAddresses.size();
			for (int i = 0; i < size; i++) {
				BluetoothSocket myBsock = BtSockets.get(BtDeviceAddresses
						.get(i));
				myBsock.close();
			}
			BtSockets = new HashMap<String, BluetoothSocket>();
			BtStreamWatcherThreads = new HashMap<String, Thread>();
			BtDeviceAddresses = new ArrayList<String>();

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

}

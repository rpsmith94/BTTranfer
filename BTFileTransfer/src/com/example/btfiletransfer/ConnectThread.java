package com.example.btfiletransfer;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class ConnectThread extends Thread
{
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static  int SUCCESS_CONNECT = 0;
	private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private Handler mHandler;
    BluetoothAdapter btAdapter;
    
    public ConnectThread(BluetoothDevice device,Handler mHandler,int SUCCESS_CONNECT,BluetoothAdapter btAdapter)
    {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        this.btAdapter = btAdapter;
        this.mHandler = mHandler;
        this.SUCCESS_CONNECT = SUCCESS_CONNECT;
        mmDevice = device;
 
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        mmSocket = tmp;
    }
 
    public void run() {
       
		// Cancel discovery because it will slow down the connection
        btAdapter.cancelDiscovery();
 
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try
            {
                mmSocket.close();
            }
            catch (IOException closeException) { }
            return;
        }
 
        // Do work to manage the connection (in a separate thread)
        
        mHandler.obtainMessage(SUCCESS_CONNECT,mmSocket).sendToTarget();
    }
 
    

	/** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
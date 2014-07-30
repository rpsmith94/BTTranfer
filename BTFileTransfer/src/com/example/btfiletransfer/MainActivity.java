package com.example.btfiletransfer;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.*;
import java.net.URL;

public class MainActivity extends Activity implements OnItemClickListener
{
	protected static final int SUCCESS_CONNECT = 0;
	ArrayAdapter<String> listAdapter;
	Button connectNew;
	ListView listview; 
	BluetoothAdapter btAdapter;
	Set<BluetoothDevice> devicesArray;
	ArrayList<String> pairedDevices;
	ArrayList<BluetoothDevice> devices;
	final int   MESSAGE_READ = 1;
	IOUtil iou;
	File send;
	URL url;
	
	
	Handler mhandler = new Handler()
	{
		
		public void handleMessage(Message msg)
		{
			Toast.makeText(getApplicationContext(), "Connect",Toast.LENGTH_SHORT).show();
			super.handleMessage(msg);
			switch(msg.what)
			{
			case SUCCESS_CONNECT:
				//do
				ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj,MESSAGE_READ,mhandler);
				String s = "Successfully connected";
				byte[] a= null;
				try {
					a = IOUtil.readFile(send);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
					connectedThread.write(a);
				
				Toast.makeText(getApplicationContext(), "Connect", 0).show();
				break;
			case MESSAGE_READ:	
				byte[] readBuf = (byte[])msg.obj;
				String p = new String(readBuf);				
				Toast.makeText(getApplicationContext(),p, 0).show();
				break;
			}
		}
	};
	IntentFilter filter;
	BroadcastReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
			init();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(btAdapter == null)
        {
        	Toast.makeText(getApplicationContext(),"No bluetooth ",Toast.LENGTH_SHORT).show();
        	finish();
        }
        else
        {
        	if(!btAdapter.isEnabled())
        	{
        		turnOnBT();
        	}
        	
        }
       
        getPairedDevices();
        startDiscovery();
        
    }
    private void startDiscovery()
    {
		// TODO Auto-generated method stub
    	btAdapter.cancelDiscovery();
    	btAdapter.startDiscovery();
		
	}
	private void turnOnBT() 
    {

		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent,1);
		// TODO Auto-generated method stub
		
	}
	private void getPairedDevices() 
    {
    	devicesArray = btAdapter.getBondedDevices();
    	if(devicesArray.size()>0)
    	{
    		for(BluetoothDevice bt:devicesArray)
    		{
    			pairedDevices.add(bt.getName());
    		}
    	}
		// TODO Auto-generated method stub
		
	}
	private void init() throws FileNotFoundException
    {
    	connectNew = (Button)findViewById(R.id.bConnectNew);
    	 listview = (ListView)findViewById(R.id.listView);
    	 listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,0);
    	 listview.setAdapter(listAdapter);
    	 listview.setOnItemClickListener(this);
    	 btAdapter = BluetoothAdapter.getDefaultAdapter();
    	 pairedDevices = new ArrayList<String>();
    	 filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    	 devices = new ArrayList<BluetoothDevice>();
    	 iou = new IOUtil();
    	 
    	 send = new File("/storage/sdcard0/DCIM/Camera/1371657510415.jpg");
    	  
    	 receiver = new BroadcastReceiver(){    		     		 
			@Override
			public void onReceive(Context arg0, Intent intent) 
			{
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if(BluetoothDevice.ACTION_FOUND.equals(action))
				{
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					devices.add(device);
					String s = "";
					for(int a = 0;a < pairedDevices.size();a++)
					{
						if(device.getName().equals(pairedDevices.get(a)))
						{
							
							s = "(Paired)";														
							break;
						}
					}
					listAdapter.add(device.getName()+" "+s+" "+"\n"+ device.getAddress());
					
				}
				
				else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
				{
					
				}
				else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
				{
					if(listAdapter.getCount()>0)
					{
						for(int i = 0;i< listAdapter.getCount();i++)
						{
							
						}
					}
				}
				else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
				{
					if(btAdapter.getState()==btAdapter.STATE_OFF)
					{
						turnOnBT();
					}
				}
				
				
				
			}
			
    	 };
    	 registerReceiver(receiver,filter);
    	 IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
    	 registerReceiver(receiver,filter);
    	 filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    	 registerReceiver(receiver,filter);
    	 filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    	 
    	 
    }
	protected void onPause()
	{
		super.onPause();
		unregisterReceiver(receiver);
	}
    protected void onActivitResult(int requestCode,int resultCode,Intent data)
    {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode == RESULT_CANCELED)
    	{
    		Toast.makeText(getApplicationContext(),"Enable bluetooth",Toast.LENGTH_SHORT).show();
    		finish();
    	}
    }
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		// TODO Auto-generated method stub
		if(btAdapter.isDiscovering())
		{
			btAdapter.cancelDiscovery();
		}
		if(listAdapter.getItem(arg2).contains("Paired"))
		{
						
			BluetoothDevice selectedDevice = devices.get(arg2);
			ConnectThread connect = new ConnectThread(selectedDevice,mhandler,SUCCESS_CONNECT,btAdapter);
			connect.start();
		}
		else
		{
			Toast.makeText(getApplicationContext(), "device is not paired", Toast.LENGTH_SHORT).show();
		}
		
		
	}
    
}

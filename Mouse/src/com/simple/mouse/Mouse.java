package com.simple.mouse;

import com.simple.mouse.ConnectionService;
import com.simple.mouse.DeviceListActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the activity_mouse Activity that displays the current chat session.
 */
@TargetApi(5)
public class Mouse extends Activity implements SensorEventListener {
    private static final String TAG = "Mouse";
    private static final boolean D = true;

    // Message types sent from the ConnectionService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the ConnectionService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
//    private TextView mTitle;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private ConnectionService mMouseService = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    
    //BIGX
    byte[] bigX = "X".getBytes();
    byte[] bigY = "Y".getBytes();
    
    //Buttons
    Button mRightClick;
    Button mLeftClick;
    public static final String RIGHT_CLICK = "RC";
    public static final String lEFT_CLICK = "LC";

    //Sensor stuff
    private SensorManager mSensorManager;
    private Sensor mSensor;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        

        // Set up the window layout
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_mouse);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
//        mTitle = (TextView) findViewById(R.id.title_left_text);
//        mTitle.setText(R.string.app_name);
//        mTitle = (TextView) findViewById(R.id.title_right_text);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        ImageView image = (ImageView) findViewById(R.id.arrowup);
    }

	@Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupMouse() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mMouseService == null) setupMouse();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mMouseService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mMouseService.getState() == ConnectionService.STATE_NONE) {
              // Start the Bluetooth chat services
              mMouseService.start();
            }
        }
    }

    private void setupMouse() {
        Log.d(TAG, "setupMouse()");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // Define the Buttons
        mRightClick = (Button) findViewById(R.id.button2);
        mLeftClick = (Button) findViewById(R.id.button1);
        
        // Button Listener
     // Initialize the send button with a listene r that for click events
        mLeftClick.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	sendLeftClick();
            }
        }); 
        mRightClick.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	sendRightClick();
            }
        });
        mSensorManager.registerListener(this, mSensor,SensorManager.SENSOR_DELAY_FASTEST);
        
        // Initialize the ConnectionService to perform bluetooth connections
        mMouseService = new ConnectionService(this, mHandler);

        mOutStringBuffer = new  StringBuffer("");
    }
    
    public void onSensorChanged(SensorEvent event) {
    	if (mMouseService.getState() == ConnectionService.STATE_CONNECTED) {
    		float x = event.values[0];
        	float y = event.values[1];
        	
        	byte[] xsend = Float.toString(x).getBytes();
        	byte[] ysend = Float.toString(y).getBytes();
        	
        	mMouseService.write(xsend);
        	mOutStringBuffer.setLength(0);
        	mMouseService.write(bigX);
        	mOutStringBuffer.setLength(0);
        	mMouseService.write(ysend);
        	mOutStringBuffer.setLength(0);
        	mMouseService.write(bigY);
        	mOutStringBuffer.setLength(0);
    	}
    }
    
    public void onAccuracyChanged(Sensor sen, int accuracy) {
    	
    }
    
    
    private void sendLeftClick() {
        // Check that we're actually connected before trying anything
        if (mMouseService.getState() != ConnectionService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] send = "LC".getBytes();
        mMouseService.write(send);
        mOutStringBuffer.setLength(0);
        mMouseService.write(bigX);
    	mOutStringBuffer.setLength(0);
    }
    
    private void sendRightClick() {
        // Check that we're actually connected before trying anything
        if (mMouseService.getState() != ConnectionService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] send = "RC".getBytes();
        mMouseService.write(send);
        mOutStringBuffer.setLength(0);
        mMouseService.write(bigX);
    	mOutStringBuffer.setLength(0);
    }
    

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mMouseService != null) mMouseService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case ConnectionService.STATE_CONNECTED:
//                    mTitle.setText(R.string.title_connected_to);
//                    mTitle.append(mConnectedDeviceName);
                    break;
                case ConnectionService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
                    break;
                case ConnectionService.STATE_LISTEN:
                case ConnectionService.STATE_NONE:
//                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mMouseService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupMouse();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }

}

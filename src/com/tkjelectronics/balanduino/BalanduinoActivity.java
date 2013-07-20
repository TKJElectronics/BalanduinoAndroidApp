/*************************************************************************************
 * Copyright (C) 2012 Kristian Lauszus, TKJ Electronics. All rights reserved.
 *
 * This software may be distributed and modified under the terms of the GNU
 * General Public License version 2 (GPL2) as published by the Free Software
 * Foundation and appearing in the file GPL2.TXT included in the packaging of
 * this file. Please note that GPL2 Section 2[b] requires that all works based
 * on this software must also be made publicly available under the terms of
 * the GPL2 ("Copyleft").
 *
 * Contact information
 * -------------------
 *
 * Kristian Lauszus, TKJ Electronics
 * Web      :  http://www.tkjelectronics.com
 * e-mail   :  kristianl@tkjelectronics.com
 * 
 ************************************************************************************/

package com.tkjelectronics.balanduino;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.UnderlinePageIndicator;

public class BalanduinoActivity extends SherlockFragmentActivity implements ActionBar.TabListener {
	private static final String TAG = "Balanduino";
	public static final boolean D = BuildConfig.DEBUG; // This is automatally set by Gradle

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_DEVICE_NAME = 3;
	public static final int MESSAGE_TOAST = 4;
	public static final int MESSAGE_RETRY = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	public static BluetoothChatService mChatService = null;
	public static SensorFusion mSensorFusion = null;
	
	boolean btSecure; // If it's a new device we will pair with the device	
	BluetoothDevice btDevice; // The BluetoothDevice object
	
	private UnderlinePageIndicator mUnderlinePageIndicator;
	
	public static int currentTabSelected;
	
	public static String accValue = "";
	public static String gyroValue = "";
	public static String kalmanValue = "";
    public static boolean newIMUValues;

    public static String Qangle = "";
    public static String Qbias = "";
    public static String Rmeasure = "";
	public static boolean newKalmanValues;
	
	public static String pValue = "";
	public static String iValue = "";
	public static String dValue = "";
	public static String targetAngleValue = "";
	public static boolean newPIDValues;
	
	public static boolean backToSpot = true;
	public static int maxAngle = 8; // Eight is the default value
	public static int maxTurning = 20; // Twenty is the default value
	public static boolean newSettings;
	
	public static String appVersion;
	public static String firmwareVersion;
	public static String mcu;
	public static String batteryLevel;
	public static double runtime;
	public static boolean newInfo;
	
	public static boolean pairingWithWii;
	
	public final static String getPIDValues = "GP;";
	public final static String getSettings = "GS;";
	public final static String getInfo = "GI;";
    public final static String getKalman = "GK;";
		
	public final static String setPValue = "SP,";
	public final static String setIValue = "SI,";
	public final static String setDValue = "SD,";
    public final static String setKalman = "SK,";
	public final static String setTargetAngle = "ST,";
	public final static String setMaxAngle = "SA,";
	public final static String setMaxTurning = "SU,";
	public final static String setBackToSpot = "SB";
	
	public final static String imuBegin = "IB;";
	public final static String imuStop = "IS;";
	
	public final static String sendStop = "CS;";
	public final static String sendIMUValues = "CM,";
	public final static String sendJoystickValues = "CJ,";	
	public final static String sendPairWithWii = "CW;";
	
	public final static String restoreDefaultValues = "CR;";
		
	public final static String responsePIDValues = "P";
    public final static String responseKalmanValues = "K";
	public final static String responseSettings = "S";
	public final static String responseInfo = "I";
	public final static String responseIMU = "V";
	public final static String responsePairWii = "WC";
	
	public final static int responsePIDValuesLength = 5;
    public final static int responseKalmanValuesLength = 4;
	public final static int responseSettingsLength = 4;
	public final static int responseInfoLength = 5;
	public final static int responseIMULength = 4;
	public final static int responsePairWiiLength = 1;
	
	private Toast mToast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			showToast("Bluetooth is not available", Toast.LENGTH_LONG);
			finish();
			return;
		}

		// get sensorManager and initialize sensor listeners
		SensorManager mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		mSensorFusion = new SensorFusion(mSensorManager);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the adapter.
		ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mViewPagerAdapter);
		
		// Bind the underline indicator to the adapter
		mUnderlinePageIndicator = (UnderlinePageIndicator)findViewById(R.id.indicator);
		mUnderlinePageIndicator.setViewPager(mViewPager);
		mUnderlinePageIndicator.setFades(false);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mUnderlinePageIndicator
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});
		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mViewPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mViewPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			BalanduinoActivity.appVersion = pInfo.versionName; // Read the app version
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");
		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			if (D)
				Log.d(TAG, "Request enable BT");
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else { // Otherwise, setup the chat session
			if (mChatService == null)
				setupBTService();
		}
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this); // Create SharedPreferences instance		
		String filterCoefficient = preferences.getString("filterCoefficient", null); // Read the stored value for filter coefficient
		if (filterCoefficient != null) {
			mSensorFusion.filter_coefficient = Float.parseFloat(filterCoefficient);
			mSensorFusion.tempFilter_coefficient = mSensorFusion.filter_coefficient;
		}
		// Read the previous back to spot value
		backToSpot = preferences.getBoolean("backToSpot", true); // Back to spot is true by default
		// Read the previous max angle
		maxAngle = preferences.getInt("maxAngle", 8); // Eight is the default value
		// Read the previous max turning value
		maxTurning = preferences.getInt("maxTurning", 20); // Twenty is the default value
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
		// unregister sensor listeners to prevent the activity from draining the
		// device's battery.
		mSensorFusion.unregisterListeners();

		// Store the value for FILTER_COEFFICIENT and max angle at shutdown
		Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
		edit.putString("filterCoefficient",Float.toString(mSensorFusion.filter_coefficient));
		edit.putBoolean("backToSpot", backToSpot);
		edit.putInt("maxAngle", maxAngle);
		edit.putInt("maxTurning", maxTurning);
		edit.commit();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");		
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		mSensorFusion.unregisterListeners();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
		// Unregister sensor listeners to prevent the activity from draining the device's battery.
		mSensorFusion.unregisterListeners();
		if(mChatService != null) { // Send stop command and stop sending graph data command
			if(mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
				mChatService.write((sendStop + imuStop).getBytes());
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");
		// Restore the sensor listeners when user resumes the application.
		mSensorFusion.initListeners();
	}

	private void setupBTService() {
		if (D)
			Log.d(TAG, "setupBTService()");
		// Initialize the BluetoothChatService to perform Bluetooth connections
		mChatService = new BluetoothChatService(this, new BluetoothHandler(this));
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		if (D)
			Log.d(TAG,"onTabSelected: " + tab.getPosition());		
		currentTabSelected = tab.getPosition();
		mUnderlinePageIndicator.setCurrentItem(currentTabSelected); // When the given tab is selected, switch to the corresponding page in the ViewPager
		CustomViewPager.setPagingEnabled(true);
		if (currentTabSelected == ViewPagerAdapter.GRAPH_FRAGMENT && mChatService != null) {
            mChatService.write(getKalman.getBytes());
            if (GraphFragment.mToggleButton != null) {
                if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                    if (GraphFragment.mToggleButton.isChecked())
                        mChatService.write(imuBegin.getBytes()); // Request data
                    else
                        mChatService.write(imuStop.getBytes()); // Stop sending data
                }
            }
		} else if (currentTabSelected == ViewPagerAdapter.INFO_FRAGMENT && mChatService != null) {
			if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
				mChatService.write(getInfo.getBytes()); // Update info
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		if(D)
			Log.d(TAG,"onTabUnselected: " + tab.getPosition());
		if((tab.getPosition() == ViewPagerAdapter.IMU_FRAGMENT || tab.getPosition() == ViewPagerAdapter.JOYSTICK_FRAGMENT) && mChatService != null) { // Send stop command if the user selects another tab
			if(mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
				mChatService.write(sendStop.getBytes());
		} else if(tab.getPosition() == ViewPagerAdapter.GRAPH_FRAGMENT && mChatService != null) {
			if(mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
				mChatService.write(imuStop.getBytes());
		}
        if(tab.getPosition() == ViewPagerAdapter.GRAPH_FRAGMENT) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); // Hide the keyboard
		    imm.hideSoftInputFromWindow(getWindow().getDecorView().getApplicationWindowToken(), 0);
		}
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(D)
			Log.e(TAG,"onPrepareOptionsMenu");
		super.onPrepareOptionsMenu(menu);
		MenuItem menuItem = menu.findItem(R.id.menu_connect);
		if (mChatService == null)
			menuItem.setIcon(R.drawable.device_access_bluetooth);
		else {
			if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
				menuItem.setIcon(R.drawable.device_access_bluetooth_connected);
			else
				menuItem.setIcon(R.drawable.device_access_bluetooth);
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(D)
			Log.e(TAG,"onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getSupportMenuInflater();	  	
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_connect:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.settings:
			// Open up the settings dialog
			SettingsDialogFragment dialogFragment = new SettingsDialogFragment();
			dialogFragment.show(getSupportFragmentManager(), null);
			return true;
		case android.R.id.home:
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://balanduino.net/"));
			startActivity(browserIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void showToast(String message, int duration) {
		if(mToast != null)
			mToast.cancel(); // Close the toast if it's already open
		mToast = Toast.makeText(getApplicationContext(),message,duration);
		mToast.show();
	}

	// The Handler class that gets information back from the BluetoothChatService
	static class BluetoothHandler extends Handler {
		private final WeakReference<BalanduinoActivity>mActivity;
		private final BalanduinoActivity mBalanduinoActivity;		
		private String mConnectedDeviceName; // Name of the connected device
		BluetoothHandler(BalanduinoActivity activity) {
			mActivity = new WeakReference<BalanduinoActivity>(activity);
			mBalanduinoActivity = mActivity.get();
        }
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				mBalanduinoActivity.supportInvalidateOptionsMenu();
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mBalanduinoActivity.showToast(mBalanduinoActivity.getString(R.string.connected_to) + " "+ mConnectedDeviceName, Toast.LENGTH_SHORT);
					if(mChatService == null)
						return;
					Handler myHandler = new Handler();
					myHandler.postDelayed(new Runnable(){
				        public void run() {
				        	mChatService.write((getPIDValues + getSettings + getInfo + getKalman).getBytes());
				        }
				    }, 1000); // Wait 1 second before sending the message
					if(GraphFragment.mToggleButton != null) {
						if(GraphFragment.mToggleButton.isChecked() && currentTabSelected == ViewPagerAdapter.GRAPH_FRAGMENT) {
							myHandler.postDelayed(new Runnable(){
						        public void run() {
						        	mChatService.write(imuBegin.getBytes()); // Request data
						        }
						    }, 1000); // Wait 1 second before sending the message
						} else {
							myHandler.postDelayed(new Runnable(){
								public void run() {
									mChatService.write(imuStop.getBytes()); // Stop sending data
								}
							}, 1000); // Wait 1 second before sending the message
						}
					}					
					break;
				case BluetoothChatService.STATE_CONNECTING:					
					break;
				}
				PIDFragment.updateButton();
				break;
			case MESSAGE_READ:
				if(newPIDValues) {
					newPIDValues = false;
					PIDFragment.updateView();
				}
				if(newSettings)
					newSettings = false;
				if(newInfo) {
					newInfo = false;
					InfoFragment.updateView();
				}
				if(newIMUValues) {
					newIMUValues = false;
					GraphFragment.updateIMUValues();
				}
                if(newKalmanValues) {
                    newKalmanValues = false;
                    GraphFragment.updateKalmanValues();
                }
				if(pairingWithWii) {
					pairingWithWii = false;
					mBalanduinoActivity.showToast("Now press 1 & 2 on the Wiimote or press sync if you are using a Wii U Pro Controller", Toast.LENGTH_LONG);
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// Save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				break;
			case MESSAGE_TOAST:				
				mBalanduinoActivity.supportInvalidateOptionsMenu();				
				PIDFragment.updateButton();
				mBalanduinoActivity.showToast(msg.getData().getString(TOAST), Toast.LENGTH_SHORT);
				break;
			case MESSAGE_RETRY:
				if(D)
					Log.e(TAG, "MESSAGE_RETRY");
				mBalanduinoActivity.connectDevice(null, true);
				break;
			}
		}
	}	

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect to
			if (resultCode == Activity.RESULT_OK)
				connectDevice(data,false);
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupBTService();
			} else {
				// User did not enable Bluetooth or an error occured
				if (D)
					Log.d(TAG, "BT not enabled");
				showToast(getString(R.string.bt_not_enabled_leaving), Toast.LENGTH_SHORT);				
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean retry) {
		if(retry) {
			if(btDevice != null) {
				mChatService.start(); // This will stop all the running threads
				mChatService.connect(btDevice, btSecure); // Attempt to connect to the device
			}
		} else { // It's a new connection
			mChatService.newConnection = true;
			mChatService.start(); // This will stop all the running threads
			String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS); // Get the device Bluetooth address
			btSecure = data.getExtras().getBoolean(DeviceListActivity.EXTRA_NEW_DEVICE); // If it's a new device we will pair with the device			
			btDevice = mBluetoothAdapter.getRemoteDevice(address); // Get the BluetoothDevice object
			mChatService.nRetries = 0; // Reset retry counter
			mChatService.connect(btDevice, btSecure); // Attempt to connect to the device
			showToast(getString(R.string.connecting), Toast.LENGTH_SHORT);
		}		
	}
}
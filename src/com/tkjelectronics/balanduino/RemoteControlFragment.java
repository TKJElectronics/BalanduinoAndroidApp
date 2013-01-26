package com.tkjelectronics.balanduino;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class RemoteControlFragment extends SherlockFragment {
	private Button mButton;
	public TextView mPitchView;
	public TextView mRollView;
	public TextView mCoefficient;
	private TableRow mTableRow;

	private BluetoothChatService mChatService;
	private SensorFusion mSensorFusion;

	private Handler mHandler = new Handler();
	private Runnable mRunnable;
	private int counter = 0;
	boolean buttonState;

	public RemoteControlFragment() {
		mChatService = BalanduinoActivity.mChatService;
		mSensorFusion = BalanduinoActivity.mSensorFusion;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.remotecontrol, container, false);
		
		mPitchView = (TextView) v.findViewById(R.id.textView1);
		mRollView = (TextView) v.findViewById(R.id.textView2);
		mCoefficient = (TextView) v.findViewById(R.id.textView3);
		mTableRow = (TableRow) v.findViewById(R.id.tableRowCoefficient);
		mButton = (Button) v.findViewById(R.id.button);
		
		mHandler.postDelayed(new Runnable() { // Hide the menu icon and tablerow if there is no build in gyroscope in the device
			@Override
			public void run() {
				if(SensorFusion.IMUOutputSelection == -1)
					mHandler.postDelayed(this, 100); // Run this again if it hasn't initialized the sensors yet
				if(SensorFusion.IMUOutputSelection != 2) { // Check if a gyro is supported
					mTableRow.setVisibility(View.GONE); // If not then hide the tablerow
					if(BalanduinoActivity.settings != null)
						BalanduinoActivity.settings.setVisible(false); // Hide the settings icon too
					else
						mHandler.postDelayed(this, 100);  // Wait 100ms before running the code again
				}				
			}
		}, 100); // Wait 100ms before running the code
		return v;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mRunnable = new Runnable() {
			@Override
			public void run() {
				if(mSensorFusion == null) {
					mSensorFusion = BalanduinoActivity.mSensorFusion;
					return;
				}
				mPitchView.setText(mSensorFusion.pitch);
				mRollView.setText(mSensorFusion.roll);
				mCoefficient.setText(mSensorFusion.coefficient);				

				counter++;
				if (counter > 2) { // Only send data every 150ms time
					counter = 0;						
					if (mChatService == null) {
						//Log.e("Fragment: ","mChatService == null");
						mChatService = BalanduinoActivity.mChatService; // Update the instance, as it's likely because Bluetooth wasn't enabled at startup
						return;
					}
					if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED && BalanduinoActivity.currentTabSelected == ViewPagerAdapter.IMU_FRAGMENT) {
						buttonState = mButton.isPressed();
						CustomViewPager.setPagingEnabled(!buttonState);
						if (buttonState) {
							String message = "M," + mSensorFusion.pitch + ',' + mSensorFusion.roll + ";";
							byte[] send = message.getBytes();
							mChatService.write(send, false);
							mButton.setText("Now sending data");
						} else {
							byte[] send = "S;".getBytes();
							mChatService.write(send, false);
							mButton.setText("Sending stop command");
						}
					} else {
						mButton.setText(R.string.button);
						if(BalanduinoActivity.currentTabSelected == ViewPagerAdapter.IMU_FRAGMENT)
							CustomViewPager.setPagingEnabled(true);
					}
				}
				mHandler.postDelayed(this, 50); // Update IMU data every 50ms
			}
		};
		mHandler.postDelayed(mRunnable, 50); // Update IMU data every 50ms
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(mRunnable);
	}
}
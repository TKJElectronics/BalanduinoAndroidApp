package com.tkjelectronics.balanduino;

import java.util.Timer;
import java.util.TimerTask;

import com.actionbarsherlock.app.SherlockFragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class RemoteControlFragment extends SherlockFragment {
	private Button mButton;
	//public TextView mAzimuthView;
	public TextView mPitchView;
	public TextView mRollView;
	public TextView mCoefficient;

	private BluetoothChatService mChatService = null;
	private SensorFusion mSensorFusion = null;

	private Handler mHandler;
	private Timer dataTimer = new Timer();
	private int counter = 0;
	boolean update;
	boolean buttonState;

	public RemoteControlFragment() {
		mChatService = BalanduinoActivity.mChatService;
		mSensorFusion = BalanduinoActivity.mSensorFusion;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.remotecontrol, container, false);

		//mAzimuthView = (TextView) v.findViewById(R.id.textView1);
		mPitchView = (TextView) v.findViewById(R.id.textView2);
		mRollView = (TextView) v.findViewById(R.id.textView3);
		mCoefficient = (TextView) v.findViewById(R.id.textView4);

		mButton = (Button) v.findViewById(R.id.button);

		mHandler = new Handler();
		dataTimer.schedule(new processDataTimer(), 0, 50); // Update IMU data every 50ms
		return v;
	}

	class processDataTimer extends TimerTask {
		public void run() {
			// Send data to the connected device
			mHandler.post(processDataTask);
		}
	}

	private Runnable processDataTask = new Runnable() {
		@Override
		public void run() {
			processData();
		}
	};

	public void processData() {
		if(!update)
			return;
		//mAzimuthView.setText(mSensorFusion.azimut);
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
			if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED && BalanduinoActivity.currentTabSelected == 0) {
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
				if(BalanduinoActivity.currentTabSelected == 0)
					CustomViewPager.setPagingEnabled(true);
			}
		}
	}
	@Override
	public void onStart() {
		super.onResume();			
		//Log.e("Fragment: ","onStart");
		update = true;
	}
	@Override
	public void onResume() {
		super.onResume();			
		//Log.e("Fragment: ","onResume");
		update = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		//Log.e("Fragment: ","onPause");
		update = false;		
	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		//Log.e("Fragment: ","onDestroyView");
		update = false;		
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		//Log.e("Fragment: ","onDestroy");
		update = false;
		dataTimer.cancel();
		dataTimer.purge();				
	}
	@Override
	public void onStop() {
		super.onStop();
		//Log.e("Fragment: ","onStop");
		update = false;		
	}
}
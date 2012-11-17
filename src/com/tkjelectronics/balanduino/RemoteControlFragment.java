package com.tkjelectronics.balanduino;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RemoteControlFragment extends Fragment {
	private Button mButton;
	public TextView mAzimuthView;
	public TextView mPitchView;
	public TextView mRollView;
	public TextView mCoefficient;

	private BluetoothChatService mChatService = null;
	private SensorFusion mSensorFusion = null;

	private Handler mHandler;
	private Timer sendDataTimer = new Timer();	
	int counter = 0;

	public RemoteControlFragment() {
		mChatService = BalanduinoActivity.mChatService;
		mSensorFusion = BalanduinoActivity.mSensorFusion;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Create a new TextView and set its text to the fragment's section
		// number argument value.

		View v = inflater
				.inflate(R.layout.remotecontrol, container, false);

		mAzimuthView = (TextView) v.findViewById(R.id.textView5);
		mPitchView = (TextView) v.findViewById(R.id.textView6);
		mRollView = (TextView) v.findViewById(R.id.textView7);
		mCoefficient = (TextView) v.findViewById(R.id.textView8);

		mButton = (Button) v.findViewById(R.id.button);

		mHandler = new Handler();
		sendDataTimer.schedule(new processIMUDataTimer(), 0, 10); // Update IMU
																	// data
																	// every
																	// 10ms
		return v;
	}

	class processIMUDataTimer extends TimerTask {
		public void run() {
			// Send data to the connected device
			mHandler.post(processIMUDataTask);
		}
	}
	private Runnable processIMUDataTask = new Runnable() {
		@Override
		public void run() {
			processIMUData();
		}
	};
	public void processIMUData() {
		mAzimuthView.setText(mSensorFusion.azimut);
		mPitchView.setText(mSensorFusion.pitch);
		mRollView.setText(mSensorFusion.roll);
		mCoefficient.setText(mSensorFusion.coefficient);

		counter++;
		if (counter > 4) { // Only send data every 50ms time
			counter = 0;
			if (mChatService == null) {
				Toast.makeText(getActivity(), "mChatService == null",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
				if (mButton.isPressed() && BalanduinoActivity.currentTabSelected == 0) {
					String message = mSensorFusion.pitch + ','
							+ mSensorFusion.roll + ";";
					byte[] send = message.getBytes();
					mChatService.write(send, false);
					mButton.setText("Now sending data");
				} else {
					byte[] send = "S;".getBytes();
					mChatService.write(send, false);
					mButton.setText("Sending stop command");
				}
			} else
				mButton.setText(R.string.button);
		}
	}
}
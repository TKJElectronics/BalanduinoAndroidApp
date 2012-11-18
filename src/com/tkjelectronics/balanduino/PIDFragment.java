package com.tkjelectronics.balanduino;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PIDFragment extends SherlockFragment {
	private Button mButton;
	public TextView mKpView;
	public TextView mKiView;
	public TextView mKdView;
	public TextView mTargetAngleView;

	private BluetoothChatService mChatService = null;	
	
	private Handler mHandler;
	private Timer updateViewTimer = new Timer();

	public PIDFragment() {
		mChatService = BalanduinoActivity.mChatService;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.pid, container, false);

		mKpView = (TextView) v.findViewById(R.id.textView5);
		mKiView = (TextView) v.findViewById(R.id.textView6);
		mKdView = (TextView) v.findViewById(R.id.textView7);
		mTargetAngleView = (TextView) v.findViewById(R.id.textView8);

		mButton = (Button) v.findViewById(R.id.button);
		mButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mChatService == null) {
					//Log.e("Fragment: ","mChatService == null");
					mChatService = BalanduinoActivity.mChatService; // Update the instance, as it's likely because Bluetooth wasn't enabled at startup
					return;
				}
				if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
					byte[] send = "G;".getBytes();
					mChatService.write(send, false);		
				} 			
			}
		});
		mHandler = new Handler();
		updateViewTimer.schedule(new updateViewTimerTask(), 0, 50); // Update IMU data every 50ms
		return v;
	}
	
	class updateViewTimerTask extends TimerTask {
		public void run() {
			// Send data to the connected device
			mHandler.post(updateViewTimerRunnable);
		}
	}

	private Runnable updateViewTimerRunnable = new Runnable() {
		@Override
		public void run() {
			updateView();
		}
	};

	public void updateView() {
		mKpView.setText(BalanduinoActivity.pValue);
		mKiView.setText(BalanduinoActivity.iValue);
		mKdView.setText(BalanduinoActivity.dValue);
		mTargetAngleView.setText(BalanduinoActivity.targetAngleValue);		
		
		if (mChatService == null) {
			//Log.e("Fragment: ","mChatService == null");
			mChatService = BalanduinoActivity.mChatService; // Update the instance, as it's likely because Bluetooth wasn't enabled at startup
			return;
		}
		if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
			mButton.setText(R.string.getPIDValues);			
		else
			mButton.setText(R.string.button);
	}
}
package com.tkjelectronics.balanduino;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PIDFragment extends SherlockFragment {
	private static final String TAG = "PIDFragment";
	private static final boolean D = BalanduinoActivity.D;

	Button mButton;
	TextView mKpView;
	TextView mKiView;
	TextView mKdView;
	TextView mTargetAngleView;

	EditText mEditKp;
	EditText mEditKi;
	EditText mEditKd;
	EditText mEditTargetAngle;

	private BluetoothChatService mChatService = null;

	private Handler mHandler;
	private Timer updateViewTimer = new Timer();

	String newKpValue;
	String newKiValue;
	String newKdValue;
	String newTargetAngleValue;
	String oldKpValue;
	String oldKiValue;
	String oldKdValue;
	String oldTargetAngleValue;

	int counter = 0;

	public PIDFragment() {
		mChatService = BalanduinoActivity.mChatService;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.pid, container, false);

		mKpView = (TextView) v.findViewById(R.id.textView1);
		mKiView = (TextView) v.findViewById(R.id.textView2);
		mKdView = (TextView) v.findViewById(R.id.textView3);
		mTargetAngleView = (TextView) v.findViewById(R.id.textView4);

		mEditKp = (EditText) v.findViewById(R.id.editText1);
		mEditKi = (EditText) v.findViewById(R.id.editText2);
		mEditKd = (EditText) v.findViewById(R.id.editText3);
		mEditTargetAngle = (EditText) v.findViewById(R.id.editText4);

		mButton = (Button) v.findViewById(R.id.button);
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mChatService == null) {
					if (D)
						Log.e(TAG, "mChatService == null");
					mChatService = BalanduinoActivity.mChatService; // Update the instance, as it's likely because Bluetooth wasn't enabled at startup
					return;
				}
				if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {					
					newKpValue = mEditKp.getText().toString();
					newKiValue = mEditKi.getText().toString();
					newKdValue = mEditKd.getText().toString();
					newTargetAngleValue = mEditTargetAngle.getText().toString();
					
					Handler myHandler = new Handler();
										
					if(newKpValue != null) {
						if (!newKpValue.equals(oldKpValue)) {		
							oldKpValue = newKpValue;						
							myHandler.post(new Runnable() {
								public void run() {
									byte[] send = ("P," + newKpValue + ";").getBytes();
									mChatService.write(send, false);
								}
							});
							counter = 25;
						}						
					}					
					if(newKiValue != null) {
						if (!newKiValue.equals(oldKiValue)) {
							oldKiValue = newKiValue;
							myHandler.postDelayed(new Runnable() {
								public void run() {
									byte[] send = ("I," + newKiValue + ";").getBytes();
									mChatService.write(send, false);
								}
							}, counter); // Wait before sending the message						
							counter += 25;
						}
					}					
					if(newKdValue != null) {
						if (!newKdValue.equals(oldKdValue)) {
							oldKdValue = newKdValue;
							myHandler.postDelayed(new Runnable() {
								public void run() {
									byte[] send = ("D," + newKdValue + ";").getBytes();
									mChatService.write(send, false);
								}
							}, counter); // Wait before sending the message						
							counter += 25;
						}
					}
					if(newTargetAngleValue != null) {
						if (!newTargetAngleValue.equals(oldTargetAngleValue)) {
							oldTargetAngleValue = newTargetAngleValue;
							myHandler.postDelayed(new Runnable() {
								public void run() {
									byte[] send = ("T," + newTargetAngleValue + ";").getBytes();
									mChatService.write(send, false);
								}
							}, counter); // Wait before sending the message						
							counter += 25;
						}
					}
								
					if(counter != 0) {
						myHandler.postDelayed(new Runnable() {
							public void run() {
								byte[] send = "G;".getBytes();
								mChatService.write(send, false);
							}
						}, counter); // Wait before sending the message
						if (D) 
							Log.i(TAG, "Kp Value: " + newKpValue + "," + newKiValue + "," + newKdValue + "," + newTargetAngleValue);
					}

					counter = 0; // Reset counter															
				}
			}
		});
		mHandler = new Handler();
		updateViewTimer.schedule(new updateViewTimerTask(), 0, 50); // Update view every 50ms
		return v;
	}

	class updateViewTimerTask extends TimerTask {
		public void run() {
			// Send data to the connected device
			mHandler.post(updateView);
		}
	}

	private Runnable updateView = new Runnable() {
		@Override
		public void run() {
			mKpView.setText(BalanduinoActivity.pValue);
			mKiView.setText(BalanduinoActivity.iValue);
			mKdView.setText(BalanduinoActivity.dValue);
			mTargetAngleView.setText(BalanduinoActivity.targetAngleValue);

			if (BalanduinoActivity.newPValue) {
				BalanduinoActivity.newPValue = false;
				mEditKp.setText(BalanduinoActivity.pValue);
			}
			if (BalanduinoActivity.newIValue) {
				BalanduinoActivity.newIValue = false;
				mEditKi.setText(BalanduinoActivity.iValue);
			}
			if (BalanduinoActivity.newDValue) {
				BalanduinoActivity.newDValue = false;
				mEditKd.setText(BalanduinoActivity.dValue);
			}
			if (BalanduinoActivity.newTargetAngleValue) {
				BalanduinoActivity.newTargetAngleValue = false;
				mEditTargetAngle.setText(BalanduinoActivity.targetAngleValue);
			}

			if (mChatService == null) {
				if (D)
					Log.e(TAG, "mChatService == null");
				mChatService = BalanduinoActivity.mChatService; // Update the instance, as it's likely because Bluetooth wasn't enabled at startup
				return;
			}
			if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
				mButton.setText(R.string.updateValues);
			else
				mButton.setText(R.string.button);
		}
	};		
}
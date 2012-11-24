package com.tkjelectronics.balanduino;

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

	static Button mButton;
	static TextView mKpView;
	static TextView mKiView;
	static TextView mKdView;
	static TextView mTargetAngleView;

	static EditText mEditKp;
	static EditText mEditKi;
	static EditText mEditKd;
	static EditText mEditTargetAngle;

	private static BluetoothChatService mChatService = null;

	String newKpValue;
	String newKiValue;
	String newKdValue;
	String newTargetAngleValue;
	String oldKpValue;
	String oldKiValue;
	String oldKdValue;
	String oldTargetAngleValue;

	Handler mHandler = new Handler();
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
										
					if(newKpValue != null) {
						if (!newKpValue.equals(oldKpValue)) {		
							oldKpValue = newKpValue;						
							mHandler.post(new Runnable() {
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
							mHandler.postDelayed(new Runnable() {
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
							mHandler.postDelayed(new Runnable() {
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
							mHandler.postDelayed(new Runnable() {
								public void run() {
									byte[] send = ("T," + newTargetAngleValue + ";").getBytes();
									mChatService.write(send, false);
								}
							}, counter); // Wait before sending the message						
							counter += 25;
						}
					}
					if(counter != 0) {
						mHandler.postDelayed(new Runnable() {
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
		updateView();
		updateButton();
		return v;
	}
	
	public static void updateView() {
		if (BalanduinoActivity.newPValue) {
			if(mKpView != null && mEditKp != null) {
				BalanduinoActivity.newPValue = false;
				mKpView.setText(BalanduinoActivity.pValue);
				mEditKp.setText(BalanduinoActivity.pValue);
			}
		}
		if (BalanduinoActivity.newIValue) {
			if(mKiView != null && mEditKi != null) {
				BalanduinoActivity.newIValue = false;
				mKiView.setText(BalanduinoActivity.iValue);
				mEditKi.setText(BalanduinoActivity.iValue);
			}
		}
		if (BalanduinoActivity.newDValue) {
			if(mKdView != null && mEditKd != null) {
				BalanduinoActivity.newDValue = false;
				mKdView.setText(BalanduinoActivity.dValue);
				mEditKd.setText(BalanduinoActivity.dValue);
			}
		}
		if (BalanduinoActivity.newTargetAngleValue) {
			if(mTargetAngleView != null && mEditTargetAngle != null) {
				BalanduinoActivity.newTargetAngleValue = false;
				mTargetAngleView.setText(BalanduinoActivity.targetAngleValue);
				mEditTargetAngle.setText(BalanduinoActivity.targetAngleValue);
			}
		}
	}
	
	public static void updateButton() {
		if (mChatService == null) {
			if (D)
				Log.e(TAG, "mChatService == null");
			mChatService = BalanduinoActivity.mChatService; // Update the instance, as it's likely because Bluetooth wasn't enabled at startup
			return;
		}
		if(mButton == null)
			return;
		if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
			mButton.setText(R.string.updateValues);
		else
			mButton.setText(R.string.button);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// When the user resumes the view, then set the values again
		if(mChatService != null) {
			if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
				mKpView.setText(BalanduinoActivity.pValue);
				mKiView.setText(BalanduinoActivity.iValue);
				mKdView.setText(BalanduinoActivity.dValue);
				mTargetAngleView.setText(BalanduinoActivity.targetAngleValue);
				mEditKp.setText(BalanduinoActivity.pValue);
				mEditKi.setText(BalanduinoActivity.iValue);
				mEditKd.setText(BalanduinoActivity.dValue);
				mEditTargetAngle.setText(BalanduinoActivity.targetAngleValue);
			}
		}
		updateButton();
	}
}
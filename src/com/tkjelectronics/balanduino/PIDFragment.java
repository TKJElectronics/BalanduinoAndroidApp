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
				if (BalanduinoActivity.mChatService == null) {
					if (D)
						Log.e(TAG, "mChatService == null");
					return;
				}
				if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {					
					newKpValue = mEditKp.getText().toString();
					newKiValue = mEditKi.getText().toString();
					newKdValue = mEditKd.getText().toString();
					newTargetAngleValue = mEditTargetAngle.getText().toString();
										
					if(newKpValue != null) {
						if (!newKpValue.equals(oldKpValue)) {		
							oldKpValue = newKpValue;						
							mHandler.post(new Runnable() {
								public void run() {
									BalanduinoActivity.mChatService.write((BalanduinoActivity.setPValue + newKpValue + ";").getBytes());
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
									BalanduinoActivity.mChatService.write((BalanduinoActivity.setIValue + newKiValue + ";").getBytes());
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
									BalanduinoActivity.mChatService.write((BalanduinoActivity.setDValue + newKdValue + ";").getBytes());
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
									BalanduinoActivity.mChatService.write((BalanduinoActivity.setTargetAngle + newTargetAngleValue + ";").getBytes());
								}
							}, counter); // Wait before sending the message						
							counter += 25;
						}
					}
					if(counter != 0) {
						mHandler.postDelayed(new Runnable() {
							public void run() {
								BalanduinoActivity.mChatService.write(BalanduinoActivity.getPIDValues.getBytes());
							}
						}, counter); // Wait before sending the message
						if (D) 
							Log.i(TAG, newKpValue + "," + newKiValue + "," + newKdValue + "," + newTargetAngleValue);
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
		if(mKpView != null && mEditKp != null) {
			mKpView.setText(BalanduinoActivity.pValue);
			if(!(mEditKp.getText().toString().equals(BalanduinoActivity.pValue)))
				mEditKp.setText(BalanduinoActivity.pValue);
		}
		if(mKiView != null && mEditKi != null) {
			mKiView.setText(BalanduinoActivity.iValue);
			if(!(mEditKi.getText().toString().equals(BalanduinoActivity.iValue)))
				mEditKi.setText(BalanduinoActivity.iValue);
		}
		if(mKdView != null && mEditKd != null) {
			mKdView.setText(BalanduinoActivity.dValue);
			if(!(mEditKd.getText().toString().equals(BalanduinoActivity.dValue)))
				mEditKd.setText(BalanduinoActivity.dValue);
		}		
		if(mTargetAngleView != null && mEditTargetAngle != null) {
			mTargetAngleView.setText(BalanduinoActivity.targetAngleValue);
			if(!(mEditTargetAngle.getText().toString().equals(BalanduinoActivity.targetAngleValue)))
				mEditTargetAngle.setText(BalanduinoActivity.targetAngleValue);
		}
	}
	
	public static void updateButton() {
		if (BalanduinoActivity.mChatService != null && mButton != null) {
			if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
				mButton.setText(R.string.updateValues);
			else
				mButton.setText(R.string.button);
		}		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// When the user resumes the view, then set the values again
		if(BalanduinoActivity.mChatService != null) {
			if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
				mKpView.setText(BalanduinoActivity.pValue);
				mKiView.setText(BalanduinoActivity.iValue);
				mKdView.setText(BalanduinoActivity.dValue);
				mTargetAngleView.setText(BalanduinoActivity.targetAngleValue);
				mEditKp.setText(BalanduinoActivity.pValue);
				mEditKi.setText(BalanduinoActivity.iValue);
				mEditKd.setText(BalanduinoActivity.dValue);
				mEditTargetAngle.setText(BalanduinoActivity.targetAngleValue);
			}
			updateButton();
		}		
	}
}
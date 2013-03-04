package com.tkjelectronics.balanduino;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class InfoFragment extends SherlockFragment {
	static TextView mAppVersion;
	static TextView mFirmwareVersion;
	static TextView mMcu;
	static TextView mBatterylevel;
	static TextView mRuntime;
	
	static Button mButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.info, container, false);
		mAppVersion = (TextView) v.findViewById(R.id.appVersion);
		mFirmwareVersion = (TextView) v.findViewById(R.id.firmwareVersion);
		mMcu = (TextView) v.findViewById(R.id.mcu);
		mBatterylevel = (TextView) v.findViewById(R.id.batterylevel);
		mRuntime = (TextView) v.findViewById(R.id.runtime);
		
		mButton = (Button) v.findViewById(R.id.button);
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (BalanduinoActivity.mChatService != null) {
					if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
						BalanduinoActivity.mChatService.write(BalanduinoActivity.getInfo.getBytes());
				}
			}
		});
		updateView();
		updateButton();
		return v;
	}
	
	public static void updateView() {
		if(mAppVersion != null && BalanduinoActivity.appVersion != null)
			mAppVersion.setText(BalanduinoActivity.appVersion);
		if(mFirmwareVersion != null && BalanduinoActivity.firmwareVersion != null)
			mFirmwareVersion.setText(BalanduinoActivity.firmwareVersion);
		if(mMcu != null && BalanduinoActivity.mcu != null)
			mMcu.setText("ATmega" + BalanduinoActivity.mcu);
		if(mBatterylevel != null && BalanduinoActivity.batteryLevel != null)
			mBatterylevel.setText(BalanduinoActivity.batteryLevel);
		if(mRuntime != null && BalanduinoActivity.runtime != 0) {	
			String minutes = Integer.toString((int)Math.floor(BalanduinoActivity.runtime));
			String seconds = Integer.toString((int)(BalanduinoActivity.runtime%1/(1.0/60.0)));
			mRuntime.setText(minutes + " min " + seconds + " sec");
		}
	}
	
	public static void updateButton() {
		if(BalanduinoActivity.mChatService != null && mButton != null) {
			if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
				mButton.setText(R.string.updateInfo);
			else
				mButton.setText(R.string.button);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// When the user resumes the view, then update the values
		updateView();
		updateButton();
	}
}
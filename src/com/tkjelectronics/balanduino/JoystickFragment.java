package com.tkjelectronics.balanduino;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class JoystickFragment extends SherlockFragment implements JoystickView.OnJoystickChangeListener {
	DecimalFormat d = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
	JoystickView mJoystick;
	TextView mText1;
	private Handler mHandler;
	private Timer dataTimer = new Timer();
	boolean update;
	
	double xValue;
	double yValue;
	boolean joystickReleased;
	
	private BluetoothChatService mChatService = null;
	
	public JoystickFragment() {
		mChatService = BalanduinoActivity.mChatService;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.joystick, container, false);
		mJoystick = (JoystickView)v.findViewById(R.id.joystick);
		initJoystick();
		mText1 = (TextView)v.findViewById(R.id.textView1);
		mText1.setText("x: 0 y: 0");
		
		mHandler = new Handler();
		dataTimer.schedule(new processDataTimer(), 0, 150); // Send data every 150ms
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
		if (mChatService == null) {
			//Log.e("Fragment: ","mChatService == null");
			mChatService = BalanduinoActivity.mChatService; // Update the instance, as it's likely because Bluetooth wasn't enabled at startup
			return;
		}
		if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED && BalanduinoActivity.currentTabSelected == 1) {
			if(joystickReleased) {
				byte[] send = "S;".getBytes();
				mChatService.write(send, false);
			} else {
				String message = "J," + d.format(xValue) + ',' + d.format(yValue) + ";";
				byte[] send = message.getBytes();
				mChatService.write(send, false);
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

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void initJoystick() { // It's only in portrait mode, so we don't worry if the user rotate the screen
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		int width;
		if(android.os.Build.VERSION.SDK_INT < 13)
			width = display.getWidth();
		else {
			Point size = new Point();
			display.getSize(size);
			width = size.x;
		}		
		mJoystick.setRadius(width/3.1,width/3.1/2);		
		mJoystick.setOnJoystickChangeListener(this);
	}
	@Override
	public void setOnTouchListener(double xValue, double yValue) {
		joystickReleased = false;
		CustomViewPager.setPagingEnabled(false);
		this.xValue = xValue;
		this.yValue = yValue;
		mText1.setText("x: " + d.format(xValue) + " y: " + d.format(yValue));
	}
	@Override
	public void setOnMovedListener(double xValue, double yValue) {
		joystickReleased = false;
		CustomViewPager.setPagingEnabled(false);
		this.xValue = xValue;
		this.yValue = yValue;
		mText1.setText("x: " + d.format(xValue) + " y: " + d.format(yValue));
	}
	@Override
	public void setOnReleaseListener(double xValue, double yValue) {
		joystickReleased = true;
		CustomViewPager.setPagingEnabled(true);
		this.xValue = xValue;
		this.yValue = yValue;
		mText1.setText("x: " + d.format(xValue) + " y: " + d.format(yValue));		
	}
}
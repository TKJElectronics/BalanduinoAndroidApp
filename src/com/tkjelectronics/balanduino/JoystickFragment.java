package com.tkjelectronics.balanduino;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.joystick, container, false);
		mJoystick = (JoystickView)v.findViewById(R.id.joystick);
		initJoystick();
		mText1 = (TextView)v.findViewById(R.id.textView1);
		mText1.setText("x: 0 y: 0");
		return v;
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
		CustomViewPager.setPagingEnabled(false);
		if(mText1.getText().equals("x: 0 y: 0"))
			mText1.setText("x: " + d.format(xValue) + " y: " + d.format(yValue));
	}
	@Override
	public void setOnMovedListener(double xValue, double yValue) {
		CustomViewPager.setPagingEnabled(false);
		mText1.setText("x: " + d.format(xValue) + " y: " + d.format(yValue));
	}
	@Override
	public void setOnReleaseListener(double xValue, double yValue) {
		CustomViewPager.setPagingEnabled(true);
		mText1.setText("x: " + d.format(xValue) + " y: " + d.format(yValue));
	}
}
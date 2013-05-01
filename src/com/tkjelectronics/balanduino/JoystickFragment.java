/*************************************************************************************
 * Copyright (C) 2012 Kristian Lauszus, TKJ Electronics. All rights reserved.
 *
 * This software may be distributed and modified under the terms of the GNU
 * General Public License version 2 (GPL2) as published by the Free Software
 * Foundation and appearing in the file GPL2.TXT included in the packaging of
 * this file. Please note that GPL2 Section 2[b] requires that all works based
 * on this software must also be made publicly available under the terms of
 * the GPL2 ("Copyleft").
 *
 * Contact information
 * -------------------
 *
 * Kristian Lauszus, TKJ Electronics
 * Web      :  http://www.tkjelectronics.com
 * e-mail   :  kristianl@tkjelectronics.com
 * 
 ************************************************************************************/

package com.tkjelectronics.balanduino;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class JoystickFragment extends SherlockFragment implements JoystickView.OnJoystickChangeListener {
	DecimalFormat d = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
	JoystickView mJoystick;
	TextView mText1;
	private Handler mHandler = new Handler();
	private Runnable mRunnable;
	
	double xValue;
	double yValue;
	boolean joystickReleased;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.joystick, container, false);
		
		mJoystick = (JoystickView)v.findViewById(R.id.joystick);
		mJoystick.setOnJoystickChangeListener(this);
		
		mText1 = (TextView)v.findViewById(R.id.textView1);
		mText1.setText(R.string.defaultJoystickValue);
		return v;
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
	
	@Override
	public void onStart() {
		super.onStart();
		mJoystick.invalidate();
	}
	@Override
	public void onResume() {
		super.onResume();
		mJoystick.invalidate();
		
		mRunnable = new Runnable() {
			@Override
			public void run() {
				mHandler.postDelayed(this, 150); // Send data every 150ms
				if (BalanduinoActivity.mChatService == null)
					return;
				if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED && BalanduinoActivity.currentTabSelected == ViewPagerAdapter.JOYSTICK_FRAGMENT) {
					if(joystickReleased)
						BalanduinoActivity.mChatService.write(BalanduinoActivity.sendStop.getBytes());
					else {
						String message = BalanduinoActivity.sendJoystickValues + d.format(xValue) + ',' + d.format(yValue) + ";";
						BalanduinoActivity.mChatService.write(message.getBytes());
					}
				}
			}
		};
		mHandler.postDelayed(mRunnable, 150); // Send data every 150ms
	}

	@Override
	public void onPause() {
		super.onPause();
		mJoystick.invalidate();
		CustomViewPager.setPagingEnabled(true);
		mHandler.removeCallbacks(mRunnable);
	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mJoystick.invalidate();
	}
	@Override
	public void onStop() {
		super.onStop();
		mJoystick.invalidate();
		CustomViewPager.setPagingEnabled(true);
	}	
}
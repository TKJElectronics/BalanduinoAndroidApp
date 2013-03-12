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

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
	public static final int IMU_FRAGMENT = 0;
	public static final int JOYSTICK_FRAGMENT = 1;
	public static final int GRAPH_FRAGMENT = 2;
	public static final int PID_FRAGMENT = 3;
	public static final int INFO_FRAGMENT = 4;

	public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public SherlockFragment getItem(int position) {
		switch (position) {
		case 0:
			SherlockFragment fragment0 = new ImuFragment();
			return fragment0;
		case 1:
			SherlockFragment fragment1 = new JoystickFragment();
			return fragment1;
		case 2:
			SherlockFragment fragment2 = new GraphFragment();
			return fragment2;
		case 3:
			SherlockFragment fragment3 = new PIDFragment();
			return fragment3;
		case 4:
			SherlockFragment fragment4 = new InfoFragment();
			return fragment4;
		default:
			return null;
		}
	}

	@Override
	public int getCount() {
		// Return number of tabs		
		return 5;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return "IMU";
		case 1:
			return "Joystick";
		case 2:
			return "Graph";
		case 3:
			return "PID Adjustment";
		case 4:
			return "Info";
		}
		return null;
	}
}
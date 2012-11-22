package com.tkjelectronics.balanduino;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

	public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			Fragment fragment0 = new RemoteControlFragment();
			return fragment0;
		case 1:
			Fragment fragment1 = new JoystickFragment();
			return fragment1;
		case 2:
			Fragment fragment2 = new PIDFragment();
			return fragment2;
		case 3:
			Fragment fragment3 = new VoiceRecognitionFragment();
			return fragment3;
		default:
			return null;
		}
	}

	@Override
	public int getCount() {
		// Show 4 pages.
		return 4;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return "IMU";
		case 1:
			return "Joystick";
		case 2:
			return "PID Adjustment";
		case 3:
			return "Voice control";
		}
		return null;
	}
}
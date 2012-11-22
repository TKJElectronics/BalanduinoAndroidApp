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
			Fragment fragment1 = new PIDFragment();
			return fragment1;
		case 2:
			Fragment fragment2 = new VoiceRecognitionFragment();
			return fragment2;
		default:
			return null;
		}
	}

	@Override
	public int getCount() {
		// Show 3 pages.
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return "Remote Control";
		case 1:
			return "PID Adjustment";
		case 2:
			return "Voice control";
		}
		return null;
	}
}
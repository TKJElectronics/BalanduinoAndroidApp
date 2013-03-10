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

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

public class GraphFragment extends SherlockFragment {
	private static final boolean D = BalanduinoActivity.D;
	
	private static GraphView graphView;
	private static GraphViewSeries accSeries;
	private static GraphViewSeries gyroSeries;
	private static GraphViewSeries kalmanSeries;
	private static double counter = 100d;
	public static ToggleButton mToggleButton;
	private static CheckBox mCheckBox1;
	private static CheckBox mCheckBox2;
	private static CheckBox mCheckBox3;
	private static double[][] buffer = new double[3][101]; // Used to store the 101 last readings
	
	public GraphFragment() {
		for (int i = 0; i < 3; i++)
			for (int i2 = 0; i2 < buffer[i].length; i2++)
				buffer[i][i2] = 180d;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.graph, container, false);
		
		GraphViewData[] data0 = new GraphViewData[101];
		GraphViewData[] data1 = new GraphViewData[101];
		GraphViewData[] data2 = new GraphViewData[101];
		
		for (int i = 0; i < 101; i++) { // Restore last data
			data0[i] = new GraphViewData(counter-100+i, buffer[0][i]);
			data1[i] = new GraphViewData(counter-100+i, buffer[1][i]);
			data2[i] = new GraphViewData(counter-100+i, buffer[2][i]);
		}
		accSeries = new GraphViewSeries("Accelerometer",new GraphViewSeriesStyle(Color.RED, 2), data0);
		gyroSeries = new GraphViewSeries("Gyro", new GraphViewSeriesStyle(Color.GREEN, 2), data1);
		kalmanSeries = new GraphViewSeries("Kalman", new GraphViewSeriesStyle(Color.BLUE, 2), data2);
		
		graphView = new LineGraphView(getActivity(), "");
		if(mCheckBox1 != null) {
			if(mCheckBox1.isChecked())
				graphView.addSeries(accSeries);			
		} else
			graphView.addSeries(accSeries);
		if(mCheckBox2 != null) {
			if(mCheckBox2.isChecked())
				graphView.addSeries(gyroSeries);
		} else
			graphView.addSeries(gyroSeries);
		if(mCheckBox3 != null) {
			if(mCheckBox3.isChecked())
				graphView.addSeries(kalmanSeries);
		} else
			graphView.addSeries(kalmanSeries);

		graphView.setManualYAxisBounds(360, 0);
		graphView.setViewPort(0, 100);
		graphView.setScrollable(true);
		graphView.setDiscableTouch(true);

		graphView.setShowLegend(true);
		graphView.setLegendAlign(LegendAlign.BOTTOM);
		graphView.scrollToEnd();

		LinearLayout layout = (LinearLayout) v.findViewById(R.id.graph);
		layout.addView(graphView);
		
		mCheckBox1 = (CheckBox) v.findViewById(R.id.checkBox1);
		mCheckBox1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(((CheckBox) v).isChecked())
					graphView.addSeries(accSeries);
				else
					graphView.removeSeries(accSeries);
				graphView.redrawAll(); // Redraw it in case the stop button is pressed
			}
		});
		mCheckBox2 = (CheckBox) v.findViewById(R.id.checkBox2);
		mCheckBox2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(((CheckBox) v).isChecked())
					graphView.addSeries(gyroSeries);
				else
					graphView.removeSeries(gyroSeries);
				graphView.redrawAll(); // Redraw it in case the stop button is pressed
			}
		});
		mCheckBox3 = (CheckBox) v.findViewById(R.id.checkBox3);
		mCheckBox3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(((CheckBox) v).isChecked())
					graphView.addSeries(kalmanSeries);
				else
					graphView.removeSeries(kalmanSeries);
				graphView.redrawAll(); // Redraw it in case the stop button is pressed
			}
		});

		mToggleButton = (ToggleButton) v.findViewById(R.id.button);
		mToggleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((ToggleButton) v).isChecked())
					mToggleButton.setText("Stop");
				else
					mToggleButton.setText("Start");
				
				if (BalanduinoActivity.mChatService != null) {
					if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED && BalanduinoActivity.currentTabSelected == ViewPagerAdapter.GRAPH_FRAGMENT) {
						if(((ToggleButton) v).isChecked())
							BalanduinoActivity.mChatService.write(BalanduinoActivity.imuBegin.getBytes()); // Request data
						else
							BalanduinoActivity.mChatService.write(BalanduinoActivity.imuStop.getBytes()); // Stop sending data
					}
				}
			}
		});
		if (BalanduinoActivity.mChatService != null) {
			if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED && BalanduinoActivity.currentTabSelected == ViewPagerAdapter.GRAPH_FRAGMENT) {
				if(mToggleButton.isChecked())
					BalanduinoActivity.mChatService.write(BalanduinoActivity.imuBegin.getBytes()); // Request data
				else
					BalanduinoActivity.mChatService.write(BalanduinoActivity.imuStop.getBytes()); // Stop sending data
			}
		}
		
		return v;
	}
	
	public static void updateValues() {
		if(mToggleButton == null)
			return;
		if (!(mToggleButton.isChecked()))
			return;
		
		for (int i = 0; i < 3; i++) { // We will save the 100 last values
			for (int i2 = 0; i2 < 100; i2++)
				buffer[i][i2] = buffer[i][i2+1];
		}
		try { // In some rare occasions the values can be corrupted
			buffer[0][100] = Double.parseDouble(BalanduinoActivity.accValue);
			buffer[1][100] = Double.parseDouble(BalanduinoActivity.gyroValue);
			buffer[2][100] = Double.parseDouble(BalanduinoActivity.kalmanValue);
		} catch (NumberFormatException e) {
			if(D)
				Log.e("RealTimeGraph", "error in input", e);
			return;
		}			
		
		boolean scroll;
		if(!mCheckBox1.isChecked() && !mCheckBox2.isChecked() && !mCheckBox3.isChecked())
			scroll = false;
		else
			scroll = true;
			
		counter++;
		accSeries.appendData(new GraphViewData(counter,buffer[0][100]), scroll);
		if(buffer[1][100] <= 360 && buffer[1][100] >= 0) // Don't draw it if it would be larger than y-axis boundaries
			gyroSeries.appendData(new GraphViewData(counter,buffer[1][100]), scroll);
		kalmanSeries.appendData(new GraphViewData(counter,buffer[2][100]), scroll);
		
		if(!scroll)
			graphView.redrawAll();
	}

	@Override
	public void onResume() {
		super.onResume();
		if(mToggleButton.isChecked())
			mToggleButton.setText("Stop");
		else
			mToggleButton.setText("Start");
		
		if (BalanduinoActivity.mChatService != null) {
			if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED && BalanduinoActivity.currentTabSelected == ViewPagerAdapter.GRAPH_FRAGMENT) {
				if(mToggleButton.isChecked()) 
					BalanduinoActivity.mChatService.write(BalanduinoActivity.imuBegin.getBytes()); // Request data
				else
					BalanduinoActivity.mChatService.write(BalanduinoActivity.imuStop.getBytes()); // Stop sending data
			}
		}
	}
}
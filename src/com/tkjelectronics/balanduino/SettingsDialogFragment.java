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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsDialogFragment extends DialogFragment {
	Button mRestoreButton;
	Button mPairButton;
	int maxAngle;
	int maxTurning;
	boolean backToSpot;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.settings_dialog, null);

		final TextView coefficientValue = (TextView) view.findViewById(R.id.coefficientValue);
		coefficientValue.setText(BalanduinoActivity.mSensorFusion.d.format(BalanduinoActivity.mSensorFusion.filter_coefficient));
		final SeekBar mSeekbarCoefficient = (SeekBar) view.findViewById(R.id.coefficientSeekBar);
		mSeekbarCoefficient.setProgress((int) (BalanduinoActivity.mSensorFusion.filter_coefficient * mSeekbarCoefficient.getMax()));
		mSeekbarCoefficient.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
				BalanduinoActivity.mSensorFusion.tempFilter_coefficient = ((float) progress) / mSeekbarCoefficient.getMax();
				coefficientValue.setText(BalanduinoActivity.mSensorFusion.d.format(BalanduinoActivity.mSensorFusion.tempFilter_coefficient));
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		if(SensorFusion.IMUOutputSelection != 2) { // Check if a gyro is supported if not hide seekbar and text
			view.findViewById(R.id.seekText).setVisibility(View.GONE);
			view.findViewById(R.id.coefficientLayout).setVisibility(View.GONE);
			mSeekbarCoefficient.setVisibility(View.GONE);
		}

		final TextView angleValue = (TextView) view.findViewById(R.id.angleValue);
		maxAngle = BalanduinoActivity.maxAngle;
		angleValue.setText(Integer.toString(maxAngle));
		final SeekBar mSeekbarAngle = (SeekBar) view.findViewById(R.id.angleSeekBar);
		mSeekbarAngle.setProgress(maxAngle);
		mSeekbarAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
				maxAngle = progress+1; // The seekbar doesn't allow to set the mimimum value, so we will add 1
				angleValue.setText(Integer.toString(maxAngle));
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		final TextView turningValue = (TextView) view.findViewById(R.id.turningValue);
		maxTurning = BalanduinoActivity.maxTurning;
		turningValue.setText(Integer.toString(maxTurning));
		final SeekBar mSeekbarTurning = (SeekBar) view.findViewById(R.id.turningSeekBar);
		mSeekbarTurning.setProgress(maxTurning);
		mSeekbarTurning.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
				maxTurning = progress+1; // The seekbar doesn't allow to set the mimimum value, so we will add 1
				turningValue.setText(Integer.toString(maxTurning));
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.checkBox);
		backToSpot = BalanduinoActivity.backToSpot;
		mCheckBox.setChecked(backToSpot);
		mCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				backToSpot = ((CheckBox) v).isChecked();
			}
		});

		mRestoreButton = (Button) view.findViewById(R.id.restore);
		mRestoreButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (BalanduinoActivity.mChatService != null) {
					if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
						BalanduinoActivity.mChatService.write(BalanduinoActivity.restoreDefaultValues.getBytes());
						Toast.makeText(getActivity(),"Default values have been restored", Toast.LENGTH_SHORT).show();
						dismiss();
					}
				}
			}
		});
		mPairButton = (Button) view.findViewById(R.id.pairButton);
		mPairButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (BalanduinoActivity.mChatService != null) {
					if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
						BalanduinoActivity.mChatService.write(BalanduinoActivity.sendPairWithWii.getBytes());
						dismiss();
					}
				}
			}
		});

		if (BalanduinoActivity.mChatService != null) {
			if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
				mRestoreButton.setText(R.string.restoreButtonText);
				mPairButton.setText(R.string.wiiButtonText);
			} else {
				mRestoreButton.setText(R.string.button);
				mPairButton.setText(R.string.button);
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Set title
		builder.setTitle(R.string.dialog_title)
				// Add the buttons
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User clicked OK button
						BalanduinoActivity.mSensorFusion.filter_coefficient = BalanduinoActivity.mSensorFusion.tempFilter_coefficient;
						BalanduinoActivity.maxAngle = maxAngle;
						BalanduinoActivity.maxTurning = maxTurning;
						BalanduinoActivity.backToSpot = backToSpot;
						if (BalanduinoActivity.mChatService != null) {
							if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
								int val = backToSpot? 1 : 0;
								BalanduinoActivity.mChatService.write((BalanduinoActivity.setMaxAngle + Integer.toString(maxAngle) + ";" + BalanduinoActivity.setMaxTurning + Integer.toString(maxTurning) + ";" + BalanduinoActivity.setBackToSpot + Integer.toString(val) + ";" + BalanduinoActivity.getSettings).getBytes());
							}
						}
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
						BalanduinoActivity.mSensorFusion.tempFilter_coefficient = BalanduinoActivity.mSensorFusion.filter_coefficient;
					}
				})
				// Set custom view
				.setView(view);
				// Create the AlertDialog
		return builder.create();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// User pressed back, home or pressed outside the dialog
		BalanduinoActivity.mSensorFusion.tempFilter_coefficient = BalanduinoActivity.mSensorFusion.filter_coefficient;
	}
}
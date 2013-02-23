package com.tkjelectronics.balanduino;

import com.actionbarsherlock.app.SherlockDialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsDialogFragment extends SherlockDialogFragment {
	Button mRestoreButton;
	Button mPairButton;

	public SettingsDialogFragment() {
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {		
		View view = getSherlockActivity().getLayoutInflater().inflate(R.layout.settings_dialog, null);
		
		final TextView seekValue = (TextView) view.findViewById(R.id.alertText);
		seekValue.setText(BalanduinoActivity.mSensorFusion.d.format(BalanduinoActivity.mSensorFusion.filter_coefficient));
		final SeekBar mSeekbar = (SeekBar) view.findViewById(R.id.seek);
		mSeekbar.setProgress((int) (BalanduinoActivity.mSensorFusion.filter_coefficient * mSeekbar.getMax()));
		mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
				BalanduinoActivity.mSensorFusion.tempFilter_coefficient = ((float) progress) / mSeekbar.getMax();
				seekValue.setText(BalanduinoActivity.mSensorFusion.d.format(BalanduinoActivity.mSensorFusion.tempFilter_coefficient));
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
				
		if(SensorFusion.IMUOutputSelection != 2) { // Check if a gyro is supported if not hide seekbar and text
			view.findViewById(R.id.seekText).setVisibility(View.GONE);
			view.findViewById(R.id.coefficientLayout).setVisibility(View.GONE);
			mSeekbar.setVisibility(View.GONE);
		}
		
		mRestoreButton = (Button) view.findViewById(R.id.restore);
		mRestoreButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (BalanduinoActivity.mChatService != null) {
					if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
						BalanduinoActivity.mChatService.write("R;".getBytes(), false);
				}
			}
		});
		mPairButton = (Button) view.findViewById(R.id.pairButton);
		mPairButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (BalanduinoActivity.mChatService != null) {
					if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
						BalanduinoActivity.mChatService.write("W;".getBytes(), false);
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
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
		// Set title
		builder.setTitle(R.string.dialog_title)
				// Add the buttons
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User clicked OK button
						BalanduinoActivity.mSensorFusion.filter_coefficient = BalanduinoActivity.mSensorFusion.tempFilter_coefficient;
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
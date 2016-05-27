/*************************************************************************************
 * Copyright (C) 2012-2014 Kristian Lauszus, TKJ Electronics. All rights reserved.
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class SettingsDialogFragment extends SherlockDialogFragment {
    private int maxAngle, maxTurning;
    private boolean backToSpot;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getSherlockActivity().getLayoutInflater().inflate(R.layout.settings_dialog, null);

        final TextView coefficientValue = (TextView) view.findViewById(R.id.coefficientValue);
        coefficientValue.setText(BalanduinoActivity.mSensorFusion.d.format(BalanduinoActivity.mSensorFusion.filter_coefficient));
        final SeekBar mSeekBarCoefficient = (SeekBar) view.findViewById(R.id.coefficientSeekBar);
        mSeekBarCoefficient.setProgress((int) (BalanduinoActivity.mSensorFusion.filter_coefficient * mSeekBarCoefficient.getMax()));
        mSeekBarCoefficient.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                BalanduinoActivity.mSensorFusion.tempFilter_coefficient = ((float) progress) / mSeekBarCoefficient.getMax();
                coefficientValue.setText(BalanduinoActivity.mSensorFusion.d.format(BalanduinoActivity.mSensorFusion.tempFilter_coefficient));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        if (SensorFusion.IMUOutputSelection != 2) { // Check if a gyro is supported if not hide SeekBar and text
            view.findViewById(R.id.seekText).setVisibility(View.GONE);
            view.findViewById(R.id.coefficientLayout).setVisibility(View.GONE);
            mSeekBarCoefficient.setVisibility(View.GONE);
        }

        final TextView angleValue = (TextView) view.findViewById(R.id.angleValue);
        maxAngle = BalanduinoActivity.maxAngle;
        angleValue.setText(Integer.toString(maxAngle));
        final SeekBar mSeekBarAngle = (SeekBar) view.findViewById(R.id.angleSeekBar);
        mSeekBarAngle.setProgress(maxAngle);
        mSeekBarAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                maxAngle = progress + 1; // The SeekBar doesn't allow to set the minimum value, so we will add 1
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
        final SeekBar mSeekBarTurning = (SeekBar) view.findViewById(R.id.turningSeekBar);
        mSeekBarTurning.setProgress(maxTurning);
        mSeekBarTurning.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                maxTurning = progress + 1; // The SeekBar doesn't allow to set the minimum value, so we will add 1
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

        if (Upload.isUsbHostAvailable()) {
            Button mUploadButton = (Button) view.findViewById(R.id.uploadButton);
            mUploadButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Upload.uploadFirmware()) // Check if a new upload was started
                        dismiss();
                }
            });
        } else
            view.findViewById(R.id.uploadFirmware).setVisibility(View.GONE);

        Button mRestoreButton = (Button) view.findViewById(R.id.restoreButton);
        mRestoreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BalanduinoActivity.mChatService != null &&
                        BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                    BalanduinoActivity.mChatService.write(BalanduinoActivity.restoreDefaultValues);
                    Toast.makeText(getSherlockActivity(), "Default values have been restored", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });

        Button mPairButtonWii = (Button) view.findViewById(R.id.pairButtonWii);
        mPairButtonWii.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BalanduinoActivity.mChatService != null &&
                        BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                    BalanduinoActivity.mChatService.write(BalanduinoActivity.sendPairWithWii);
                    dismiss();
                }
            }
        });

        Button mPairButtonPS4 = (Button) view.findViewById(R.id.pairButtonPS4);
        mPairButtonPS4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BalanduinoActivity.mChatService != null &&
                        BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                    BalanduinoActivity.mChatService.write(BalanduinoActivity.sendPairWithPS4);
                    dismiss();
                }
            }
        });

        if (BalanduinoActivity.mChatService != null) {
            if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                mRestoreButton.setText(R.string.restoreButtonText);
                mPairButtonWii.setText(R.string.wiiButtonText);
                mPairButtonPS4.setText(R.string.ps4ButtonText);
            } else {
                mRestoreButton.setText(R.string.button);
                mPairButtonWii.setText(R.string.button);
                mPairButtonPS4.setText(R.string.button);
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
                        BalanduinoActivity.maxAngle = maxAngle;
                        BalanduinoActivity.maxTurning = maxTurning;
                        BalanduinoActivity.backToSpot = backToSpot;
                        if (BalanduinoActivity.mChatService != null &&
                                BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                            int val = backToSpot ? 1 : 0;
                            BalanduinoActivity.mChatService.write(BalanduinoActivity.setMaxAngle + maxAngle + ";"
                                    + BalanduinoActivity.setMaxTurning + maxTurning + ";"
                                    + BalanduinoActivity.setBackToSpot + val + ";" + BalanduinoActivity.getSettings);
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
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

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PIDFragment extends SherlockFragment {
	private static final String TAG = "PIDFragment";
	private static final boolean D = BalanduinoActivity.D;

	static Button mButton;
    static TextView mKpView, mKiView, mKdView, mTargetAngleView;
	static SeekBar mKpSeekBar, mKiSeekBar, mKdSeekBar, mTargetAngleSeekBar;
    static TextView mKpSeekBarValue, mKiSeekBarValue, mKdSeekBarValue, mTargetAngleSeekBarValue;
    static RadioButton radio1, radio2;

    float newKpValue, newKiValue, newKdValue, newTargetAngleValue;
    float oldKpValue, oldKiValue, oldKdValue, oldTargetAngleValue;

	Handler mHandler = new Handler();
	int counter = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.pid, container, false);

		mKpView = (TextView) v.findViewById(R.id.textView1);
		mKiView = (TextView) v.findViewById(R.id.textView2);
		mKdView = (TextView) v.findViewById(R.id.textView3);
		mTargetAngleView = (TextView) v.findViewById(R.id.textView4);

        mKpSeekBar = (SeekBar) v.findViewById(R.id.KpSeekBar);
        mKpSeekBar.setMax(2000); // 0-20
        mKpSeekBar.setProgress(mKpSeekBar.getMax()/2);
        mKpSeekBarValue = (TextView) v.findViewById(R.id.KpValue);
        mKpSeekBarValue.setText(String.format("%.2f", (float)mKpSeekBar.getMax()/200.0f));

        mKpSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                newKpValue = (float)progress/100.0f; // Since the SeekBar can only handle integers, so this is needed
                mKpSeekBarValue.setText(String.format("%.2f", newKpValue)); // Two decimal places
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mKiSeekBar = (SeekBar) v.findViewById(R.id.KiSeekBar);
        mKiSeekBar.setMax(2000); // 0-20
        mKiSeekBar.setProgress(mKiSeekBar.getMax()/2);
        mKiSeekBarValue = (TextView) v.findViewById(R.id.KiValue);
        mKiSeekBarValue.setText(String.format("%.2f", (float)mKiSeekBar.getMax()/200.0f));

        mKiSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                newKiValue = (float)progress/100.0f; // Since the SeekBar can only handle integers, so this is needed
                mKiSeekBarValue.setText(String.format("%.2f", newKiValue)); // Two decimal places
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mKdSeekBar = (SeekBar) v.findViewById(R.id.KdSeekBar);
        mKdSeekBar.setMax(2000); // 0-20
        mKdSeekBar.setProgress(mKdSeekBar.getMax()/2);
        mKdSeekBarValue = (TextView) v.findViewById(R.id.KdValue);
        mKdSeekBarValue.setText(String.format("%.2f", (float)mKdSeekBar.getMax()/200.0f));

        mKdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                newKdValue = (float)progress/100.0f; // Since the SeekBar can only handle integers, so this is needed
                mKdSeekBarValue.setText(String.format("%.2f", newKdValue)); // Two decimal places
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mTargetAngleSeekBar = (SeekBar) v.findViewById(R.id.TargetAngleSeekBar);
        mTargetAngleSeekBar.setMax(600); // 150-210
        mTargetAngleSeekBar.setProgress(mTargetAngleSeekBar.getMax()/2);
        mTargetAngleSeekBarValue = (TextView) v.findViewById(R.id.TargetAngleValue);
        mTargetAngleSeekBarValue.setText(String.format("%.2f", (float)mTargetAngleSeekBar.getMax()/20.0f+150.0f));

        mTargetAngleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                newTargetAngleValue = (float)progress/10.0f+150.0f; // It's not possible to set the minimum value either, so we will add a offset
                mTargetAngleSeekBarValue.setText(String.format("%.2f", newTargetAngleValue)); // Two decimal places
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        radio1 = (RadioButton) v.findViewById(R.id.radio1);
        radio1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateView();
            }
        });
        radio2 = (RadioButton) v.findViewById(R.id.radio2);
        radio2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateView();
            }
        });

		mButton = (Button) v.findViewById(R.id.button);
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (BalanduinoActivity.mChatService == null) {
					if (D)
						Log.e(TAG, "mChatService == null");
					return;
				}
				if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                    if (newKpValue != oldKpValue) {
                        oldKpValue = newKpValue;
                        mHandler.post(new Runnable() {
                            public void run() {
                                char radioButton = '0';
                                if (radio1.isChecked())
                                    radioButton = '0';
                                else if (radio2.isChecked())
                                    radioButton = '1';
                                BalanduinoActivity.mChatService.write(BalanduinoActivity.setPValue + radioButton + "," + newKpValue + ";");
                            }
                        });
                        counter = 25;
                    }
                    if (newKiValue != oldKiValue) {
                        oldKiValue = newKiValue;
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                char radioButton = '0';
                                if (radio1.isChecked())
                                    radioButton = '0';
                                else if (radio2.isChecked())
                                    radioButton = '1';
                                BalanduinoActivity.mChatService.write(BalanduinoActivity.setIValue + radioButton + "," + newKiValue + ";");
                            }
                        }, counter); // Wait before sending the message
                        counter += 25;
                    }
                    if (newKdValue != oldKdValue) {
                        oldKdValue = newKdValue;
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                char radioButton = '0';
                                if (radio1.isChecked())
                                    radioButton = '0';
                                else if (radio2.isChecked())
                                    radioButton = '1';
                                BalanduinoActivity.mChatService.write(BalanduinoActivity.setDValue + radioButton + "," + newKdValue + ";");
                            }
                        }, counter); // Wait before sending the message
                        counter += 25;
                    }
                    if (newTargetAngleValue != oldTargetAngleValue) {
                        oldTargetAngleValue = newTargetAngleValue;
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                BalanduinoActivity.mChatService.write(BalanduinoActivity.setTargetAngle + newTargetAngleValue + ";");
                            }
                        }, counter); // Wait before sending the message
                        counter += 25;
                    }
					if (counter != 0) {
						mHandler.postDelayed(new Runnable() {
							public void run() {
                                String getValues = BalanduinoActivity.getPIDValues;
                                if (radio1.isChecked())
                                    getValues = BalanduinoActivity.getPIDValues;
                                else if (radio2.isChecked())
                                    getValues = BalanduinoActivity.getEncoderValues;
								BalanduinoActivity.mChatService.write(getValues);
							}
						}, counter); // Wait before sending the message
						if (D)
							Log.i(TAG, newKpValue + "," + newKiValue + "," + newKdValue + "," + newTargetAngleValue);
					}
					counter = 0; // Reset counter
				}
			}
		});
		updateView();
		updateButton();
		return v;
	}

	public static void updateView() {
		if (mKpView != null && mKpSeekBar != null && mKpSeekBarValue != null) {
            String Kp = "";
            if (radio1.isChecked() && !BalanduinoActivity.pValue.isEmpty())
                Kp = BalanduinoActivity.pValue;
            else if (radio2.isChecked() && !BalanduinoActivity.encoderPValue.isEmpty())
                Kp = BalanduinoActivity.encoderPValue;
            mKpView.setText(Kp);
            if (!Kp.equals("")) {
                float value = Float.parseFloat(Kp);
                mKpSeekBarValue.setText(String.format("%.2f", value)); // Two decimal places
                mKpSeekBar.setProgress((int)(value*100.0f));
            }
		}
        if (mKiView != null && mKiSeekBar != null && mKiSeekBarValue != null) {
            String Ki = "";
            if (radio1.isChecked() && !BalanduinoActivity.iValue.isEmpty())
                Ki = BalanduinoActivity.iValue;
            else if (radio2.isChecked() && !BalanduinoActivity.encoderIValue.isEmpty())
                Ki = BalanduinoActivity.encoderIValue;
            mKiView.setText(Ki);
            if (!Ki.equals("")) {
                float value = Float.parseFloat(Ki);
                mKiSeekBarValue.setText(String.format("%.2f", value)); // Two decimal places
                mKiSeekBar.setProgress((int)(value*100.0f));
            }
        }
        if (mKdView != null && mKdSeekBar != null && mKdSeekBarValue != null) {
            String Kd = "";
            if (radio1.isChecked() && !BalanduinoActivity.dValue.isEmpty())
                Kd = BalanduinoActivity.dValue;
            else if (radio2.isChecked() && !BalanduinoActivity.encoderDValue.isEmpty())
                Kd = BalanduinoActivity.encoderDValue;
            mKdView.setText(Kd);
            if (!Kd.equals("")) {
                float value = Float.parseFloat(Kd);
                mKdSeekBarValue.setText(String.format("%.2f", value)); // Two decimal places
                mKdSeekBar.setProgress((int)(value*100.0f));
            }
        }
        if (mTargetAngleView != null && mTargetAngleSeekBar != null && mTargetAngleSeekBarValue != null && !BalanduinoActivity.targetAngleValue.isEmpty()) {
            mTargetAngleView.setText(BalanduinoActivity.targetAngleValue);
            mTargetAngleSeekBarValue.setText(String.format("%.2f", Float.parseFloat(BalanduinoActivity.targetAngleValue))); // Two decimal places
            mTargetAngleSeekBar.setProgress((int)((Float.parseFloat(BalanduinoActivity.targetAngleValue)-150.0f)*10.0f));
        }
	}

	public static void updateButton() {
		if (BalanduinoActivity.mChatService != null && mButton != null) {
			if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
				mButton.setText(R.string.updateValues);
			else
				mButton.setText(R.string.button);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// When the user resumes the view, then set the values again
		if (BalanduinoActivity.mChatService != null) {
			if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
                updateView();
			updateButton();
		}
	}
}
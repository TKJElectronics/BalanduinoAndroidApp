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
 * The fusion algorythme is from this guide: http://www.thousand-thoughts.com/2012/03/android-sensor-fusion-tutorial/
 * By Paul Lawitzki
 * The Bluetooth communication is based on the BluetoothChat sample included in the Android SDK, but with some improvements
 * 
 ************************************************************************************/

/************************************************************************************
 * Copyright (c) 2012 Paul Lawitzki
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 ************************************************************************************/

package com.tkjelectronics.balanduino;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.tkjelectronics.balanduino.R;

public class BalanduinoActivity extends SherlockActivity implements
		SensorEventListener, RadioGroup.OnCheckedChangeListener {
	// For debugging
	private static final String TAG = "Balanduino";
	public static final boolean D = true;

	// Stores information about all the different sensors
	private SensorManager mSensorManager = null;

	// angular speeds from gyro
	private float[] gyro = new float[3];
	// rotation matrix from gyro data
	private float[] gyroMatrix = new float[9];
	// orientation angles from gyro matrix
	private float[] gyroOrientation = new float[3];
	// magnetic field vector
	private float[] magnet = new float[3];
	// accelerometer vector
	private float[] accel = new float[3];
	// orientation angles from accel and magnet
	private float[] accMagOrientation = new float[3];
	// final orientation angles from sensor fusion
	private float[] fusedOrientation = new float[3];
	// accelerometer and magnetometer based rotation matrix
	private float[] rotationMatrix = new float[9];

	public static final float EPSILON = 0.000000001f;
	private static final float NS2S = 1.0f / 1000000000.0f;
	private float timestamp;
	private boolean initState = true;

	public static final int TIME_CONSTANT = 30;
	public static float filter_coefficient = 0.98f;
	private static float tempFilter_coefficient = filter_coefficient;
	private Timer fuseTimer = new Timer();
	private Timer IMUTimer = new Timer();

	// The following members are only for displaying the sensor output.
	public Handler mHandler;
	private RadioGroup mRadioGroup;
	private TextView mAzimuthView;
	private TextView mPitchView;
	private TextView mRollView;
	private TextView mCoefficient;
	private int radioSelection;
	// DecimalFormat d = new DecimalFormat("#.##");
	DecimalFormat d = (DecimalFormat) NumberFormat
			.getNumberInstance(Locale.ENGLISH);

	private Button mButton;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	// public static final int MESSAGE_READ = 2;
	// public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	// private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	protected static final Activity BalanduinoActivity = null;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(),
					"Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		gyroOrientation[0] = 0.0f;
		gyroOrientation[1] = 0.0f;
		gyroOrientation[2] = 0.0f;

		// initialise gyroMatrix with identity matrix
		gyroMatrix[0] = 1.0f;
		gyroMatrix[1] = 0.0f;
		gyroMatrix[2] = 0.0f;
		gyroMatrix[3] = 0.0f;
		gyroMatrix[4] = 1.0f;
		gyroMatrix[5] = 0.0f;
		gyroMatrix[6] = 0.0f;
		gyroMatrix[7] = 0.0f;
		gyroMatrix[8] = 1.0f;

		// get sensorManager and initialise sensor listeners
		mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		initListeners();

		// wait for one second until gyroscope and magnetometer/accelerometer
		// data is initialised then scedule the complementary filter task
		fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
				1000, TIME_CONSTANT);

		IMUTimer.schedule(new sendIMUDataTimer(), 0, 100); // Send IMU data
															// every 100ms

		// GUI stuff
		mHandler = new Handler();
		radioSelection = 0;
		d.setRoundingMode(RoundingMode.HALF_UP);
		d.setMaximumFractionDigits(3);
		d.setMinimumFractionDigits(3);
		mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		mAzimuthView = (TextView) findViewById(R.id.textView5);
		mPitchView = (TextView) findViewById(R.id.textView6);
		mRollView = (TextView) findViewById(R.id.textView7);
		mCoefficient = (TextView) findViewById(R.id.textView8);
		mRadioGroup.setOnCheckedChangeListener(this);

		mButton = (Button) findViewById(R.id.button1);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");
		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			if (D)
				Log.d(TAG, "Request enable BT");
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupBTService();
		}
		// Read the stored value for FILTER_COEFFICIENT
		String filterCoefficient = PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						"filterCoefficient", null);
		if (filterCoefficient != null) {
			filter_coefficient = Float.parseFloat(filterCoefficient);
			tempFilter_coefficient = filter_coefficient;
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
		// unregister sensor listeners to prevent the activity from draining the
		// device's battery.
		unregisterListeners();

		// Store the value for FILTER_COEFFICIENT at shutdown
		Editor edit = PreferenceManager.getDefaultSharedPreferences(this)
				.edit();
		edit.putString("filterCoefficient", Float.toString(filter_coefficient));
		edit.commit();
	}

	@Override
	public void onDestroy() {
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		unregisterListeners();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
		// unregister sensor listeners to prevent the activity from draining the
		// device's battery.
		unregisterListeners();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");
		// restore the sensor listeners when user resumes the application.
		initListeners();
	}

	private void setupBTService() {
		if (D)
			Log.d(TAG, "setupBTService()");
		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandlerBluetooth);
	}

	// This function registers sensor listeners for the accelerometer,
	// magnetometer and gyroscope.
	public void initListeners() {
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
			mSensorManager.registerListener(this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_FASTEST);
		else
			Log.i(TAG, "Accelerometer not supported");
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null)
			mSensorManager.registerListener(this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
					SensorManager.SENSOR_DELAY_FASTEST);
		else
			Log.i(TAG, "Gyroscope not supported");
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null)
			mSensorManager
					.registerListener(this, mSensorManager
							.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
							SensorManager.SENSOR_DELAY_FASTEST);
		else
			Log.i(TAG, "Magnetic Field sensor not supported");
	}

	private void unregisterListeners() {
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			// copy new accelerometer data into accel array and calculate
			// orientation
			System.arraycopy(event.values, 0, accel, 0, 3);
			calculateAccMagOrientation();
			break;

		case Sensor.TYPE_GYROSCOPE:
			// process gyro data
			gyroFunction(event);
			break;

		case Sensor.TYPE_MAGNETIC_FIELD:
			// copy new magnetometer data into magnet array
			System.arraycopy(event.values, 0, magnet, 0, 3);
			break;
		}
	}

	// calculates orientation angles from accelerometer and magnetometer output
	public void calculateAccMagOrientation() {
		if (SensorManager
				.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
			SensorManager.getOrientation(rotationMatrix, accMagOrientation);
		}
	}

	// This function is borrowed from the Android reference
	// at
	// http://developer.android.com/reference/android/hardware/SensorEvent.html#values
	// It calculates a rotation vector from the gyroscope angular speed values.
	private void getRotationVectorFromGyro(float[] gyroValues,
			float[] deltaRotationVector, float timeFactor) {
		float[] normValues = new float[3];

		// Calculate the angular speed of the sample
		float omegaMagnitude = FloatMath
				.sqrt(gyroValues[0] * gyroValues[0] + gyroValues[1]
						* gyroValues[1] + gyroValues[2] * gyroValues[2]);

		// Normalize the rotation vector if it's big enough to get the axis
		if (omegaMagnitude > EPSILON) {
			normValues[0] = gyroValues[0] / omegaMagnitude;
			normValues[1] = gyroValues[1] / omegaMagnitude;
			normValues[2] = gyroValues[2] / omegaMagnitude;
		}

		// Integrate around this axis with the angular speed by the timestep
		// in order to get a delta rotation from this sample over the timestep
		// We will convert this axis-angle representation of the delta rotation
		// into a quaternion before turning it into the rotation matrix.
		float thetaOverTwo = omegaMagnitude * timeFactor;
		float sinThetaOverTwo = FloatMath.sin(thetaOverTwo);
		float cosThetaOverTwo = FloatMath.cos(thetaOverTwo);
		deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
		deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
		deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
		deltaRotationVector[3] = cosThetaOverTwo;
	}

	// This function performs the integration of the gyroscope data.
	// It writes the gyroscope based orientation into gyroOrientation.
	public void gyroFunction(SensorEvent event) {
		// don't start until first accelerometer/magnetometer orientation has
		// been acquired
		if (accMagOrientation == null)
			return;

		// initialisation of the gyroscope based rotation matrix
		if (initState) {
			float[] initMatrix = new float[9];
			initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
			float[] test = new float[3];
			SensorManager.getOrientation(initMatrix, test);
			gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
			initState = false;
		}

		// copy the new gyro values into the gyro array
		// convert the raw gyro data into a rotation vector
		float[] deltaVector = new float[4];
		if (timestamp != 0) {
			final float dT = (event.timestamp - timestamp) * NS2S;
			System.arraycopy(event.values, 0, gyro, 0, 3);
			getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
		}

		// measurement done, save current time for next interval
		timestamp = event.timestamp;

		// convert rotation vector into rotation matrix
		float[] deltaMatrix = new float[9];
		SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

		// apply the new rotation interval on the gyroscope based rotation
		// matrix
		gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

		// get the gyroscope based orientation from the rotation matrix
		SensorManager.getOrientation(gyroMatrix, gyroOrientation);
	}

	private float[] getRotationMatrixFromOrientation(float[] o) {
		float[] xM = new float[9];
		float[] yM = new float[9];
		float[] zM = new float[9];

		float sinX = FloatMath.sin(o[1]);
		float cosX = FloatMath.cos(o[1]);
		float sinY = FloatMath.sin(o[2]);
		float cosY = FloatMath.cos(o[2]);
		float sinZ = FloatMath.sin(o[0]);
		float cosZ = FloatMath.cos(o[0]);

		// rotation about x-axis (pitch)
		xM[0] = 1.0f;
		xM[1] = 0.0f;
		xM[2] = 0.0f;
		xM[3] = 0.0f;
		xM[4] = cosX;
		xM[5] = sinX;
		xM[6] = 0.0f;
		xM[7] = -sinX;
		xM[8] = cosX;

		// rotation about y-axis (roll)
		yM[0] = cosY;
		yM[1] = 0.0f;
		yM[2] = sinY;
		yM[3] = 0.0f;
		yM[4] = 1.0f;
		yM[5] = 0.0f;
		yM[6] = -sinY;
		yM[7] = 0.0f;
		yM[8] = cosY;

		// rotation about z-axis (azimuth)
		zM[0] = cosZ;
		zM[1] = sinZ;
		zM[2] = 0.0f;
		zM[3] = -sinZ;
		zM[4] = cosZ;
		zM[5] = 0.0f;
		zM[6] = 0.0f;
		zM[7] = 0.0f;
		zM[8] = 1.0f;

		// rotation order is y, x, z (roll, pitch, azimuth)
		float[] resultMatrix = matrixMultiplication(xM, yM);
		resultMatrix = matrixMultiplication(zM, resultMatrix);
		return resultMatrix;
	}

	private float[] matrixMultiplication(float[] A, float[] B) {
		float[] result = new float[9];

		result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
		result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
		result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

		result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
		result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
		result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

		result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
		result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
		result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

		return result;
	}

	class calculateFusedOrientationTask extends TimerTask {
		public void run() {
			float oneMinusCoeff = 1.0f - filter_coefficient;

			/*
			 * Fix for 179° <--> -179° transition problem: Check whether one of
			 * the two orientation angles (gyro or accMag) is negative while the
			 * other one is positive. If so, add 360° (2 * math.PI) to the
			 * negative value, perform the sensor fusion, and remove the 360°
			 * from the result if it is greater than 180°. This stabilizes the
			 * output in positive-to-negative-transition cases.
			 */

			// azimuth
			if (gyroOrientation[0] < -0.5 * Math.PI
					&& accMagOrientation[0] > 0.0) {
				fusedOrientation[0] = (float) (filter_coefficient
						* (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff
						* accMagOrientation[0]);
				fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI
						: 0;
			} else if (accMagOrientation[0] < -0.5 * Math.PI
					&& gyroOrientation[0] > 0.0) {
				fusedOrientation[0] = (float) (filter_coefficient
						* gyroOrientation[0] + oneMinusCoeff
						* (accMagOrientation[0] + 2.0 * Math.PI));
				fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI
						: 0;
			} else {
				fusedOrientation[0] = filter_coefficient * gyroOrientation[0]
						+ oneMinusCoeff * accMagOrientation[0];
			}

			// pitch
			if (gyroOrientation[1] < -0.5 * Math.PI
					&& accMagOrientation[1] > 0.0) {
				fusedOrientation[1] = (float) (filter_coefficient
						* (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff
						* accMagOrientation[1]);
				fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI
						: 0;
			} else if (accMagOrientation[1] < -0.5 * Math.PI
					&& gyroOrientation[1] > 0.0) {
				fusedOrientation[1] = (float) (filter_coefficient
						* gyroOrientation[1] + oneMinusCoeff
						* (accMagOrientation[1] + 2.0 * Math.PI));
				fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI
						: 0;
			} else {
				fusedOrientation[1] = filter_coefficient * gyroOrientation[1]
						+ oneMinusCoeff * accMagOrientation[1];
			}

			// roll
			if (gyroOrientation[2] < -0.5 * Math.PI
					&& accMagOrientation[2] > 0.0) {
				fusedOrientation[2] = (float) (filter_coefficient
						* (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff
						* accMagOrientation[2]);
				fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI
						: 0;
			} else if (accMagOrientation[2] < -0.5 * Math.PI
					&& gyroOrientation[2] > 0.0) {
				fusedOrientation[2] = (float) (filter_coefficient
						* gyroOrientation[2] + oneMinusCoeff
						* (accMagOrientation[2] + 2.0 * Math.PI));
				fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI
						: 0;
			} else {
				fusedOrientation[2] = filter_coefficient * gyroOrientation[2]
						+ oneMinusCoeff * accMagOrientation[2];
			}

			// overwrite gyro matrix and orientation with fused orientation
			// to comensate gyro drift
			gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
			System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);

			// update sensor output in GUI
			mHandler.post(updateOreintationDisplayTask);
		}
	}

	class sendIMUDataTimer extends TimerTask {
		public void run() {
			// Send data to the connected device
			mHandler.post(sendIMUDataTask);
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radio0:
			radioSelection = 0;
			break;
		case R.id.radio1:
			radioSelection = 1;
			break;
		case R.id.radio2:
			radioSelection = 2;
			break;
		}
	}

	public void updateOreintationDisplay() {
		switch (radioSelection) {
		case 0:
			mAzimuthView
					.setText(d.format(accMagOrientation[0] * 180 / Math.PI));
			mPitchView.setText(d.format(accMagOrientation[1] * 180 / Math.PI));
			mRollView.setText(d.format(accMagOrientation[2] * 180 / Math.PI));
			break;
		case 1:
			mAzimuthView.setText(d.format(gyroOrientation[0] * 180 / Math.PI));
			mPitchView.setText(d.format(gyroOrientation[1] * 180 / Math.PI));
			mRollView.setText(d.format(gyroOrientation[2] * 180 / Math.PI));
			break;
		case 2:
			mAzimuthView.setText(d.format(fusedOrientation[0] * 180 / Math.PI));
			mPitchView.setText(d.format(fusedOrientation[1] * 180 / Math.PI));
			mRollView.setText(d.format(fusedOrientation[2] * 180 / Math.PI));
			break;
		}
		mCoefficient.setText(d.format(tempFilter_coefficient));
	}

	private Runnable updateOreintationDisplayTask = new Runnable() {
		@Override
		public void run() {
			updateOreintationDisplay();
		}
	};

	public void sendIMUData() {
		if (mChatService == null)
			return;
		if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
			if (mButton.isPressed()) {
				/*
				 * String message = d.format(fusedOrientation[0] * 180/Math.PI)
				 * + ';' + d.format(fusedOrientation[1] * 180/Math.PI) + ';' +
				 * d.format(fusedOrientation[2] * 180/Math.PI) + "\r\n";
				 */
				String message = d.format(fusedOrientation[1] * 180 / Math.PI)
						+ ',' + d.format(fusedOrientation[2] * 180 / Math.PI)
						+ ";";
				byte[] send = message.getBytes();
				mChatService.write(send, false);
				mButton.setText("Now sending data");
			} else {
				byte[] send = "S;".getBytes();
				mChatService.write(send, false);
				mButton.setText("Sending stop command");
			}

		} else
			mButton.setText(R.string.button);
	}

	private Runnable sendIMUDataTask = new Runnable() {
		@Override
		public void run() {
			sendIMUData();
		}
	};
	
	@TargetApi(11)
	static class VersionHelper {
		static void refreshActionBarMenu(Activity activity) {
			activity.invalidateOptionsMenu();
		}
	}

	public void updateActionBar() {
		VersionHelper.refreshActionBarMenu(this);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		MenuItem menuItemMapView = menu.findItem(R.id.menu_insecure_connect);
		if (mChatService == null)
			menuItemMapView.setIcon(R.drawable.device_access_bluetooth);
		else {
			if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
				menuItemMapView
						.setIcon(R.drawable.device_access_bluetooth_connected);
			else
				menuItemMapView.setIcon(R.drawable.device_access_bluetooth);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.menu_insecure_connect:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent,
					REQUEST_CONNECT_DEVICE_INSECURE);
			return true;
		case R.id.settings:
			// This is used to add a custom layout to an AlertDialog
			final View setCoefficient = LayoutInflater.from(this).inflate(R.layout.dialog, 
					(ViewGroup)findViewById(R.id.layout_dialog));			
			final TextView value = (TextView)setCoefficient.findViewById(R.id.alertText);			
			final SeekBar mSeekbar = (SeekBar)setCoefficient.findViewById(R.id.seek);
			mSeekbar.setProgress((int) (filter_coefficient * 100));
			mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onProgressChanged(SeekBar seekBar, int progress,boolean fromTouch) {
					if (D)
						Log.d(TAG, Integer.toString(progress));
					tempFilter_coefficient = ((float) progress) / 100;
					value.setText(d.format(tempFilter_coefficient));
					}
				public void onStartTrackingTouch(SeekBar seekBar) {					
				}
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_title)
					// Set title
					// Add the buttons
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									filter_coefficient = tempFilter_coefficient;
									if (D)
										Log.d(TAG,
												"Filter Coefficient was set to: "
														+ filter_coefficient);
									// User clicked OK button
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									tempFilter_coefficient = filter_coefficient;
									// User cancelled the dialog
								}
							})
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									tempFilter_coefficient = filter_coefficient;
									// User pressed back, home etc.
								}
							})
					// Set custom view with Seekbar
					.setView(setCoefficient)
					// Create and show the AlertDialog
					.create().show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// The Handler that gets information back from the BluetoothChatService
	@SuppressLint("HandlerLeak")
	private final Handler mHandlerBluetooth = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			updateActionBar();
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					Toast.makeText(
							getApplicationContext(),
							getString(R.string.connected_to) + " "
									+ mConnectedDeviceName, Toast.LENGTH_SHORT)
							.show();
					// mTitle.setText();
					// mTitle.append(mConnectedDeviceName);
					// mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					Toast.makeText(getApplicationContext(),
							R.string.connecting, Toast.LENGTH_SHORT).show();
					break;
				/*
				 * case BluetoothChatService.STATE_NONE:
				 * Toast.makeText(getApplicationContext
				 * (),R.string.title_not_connected,Toast.LENGTH_LONG).show();
				 * break;
				 */
				}
				break;
			/*
			 * case MESSAGE_READ: byte[] readBuf = (byte[]) msg.obj; //
			 * construct a string from the valid bytes in the buffer String
			 * readMessage = new String(readBuf, 0, msg.arg1); if(D) Log.i(TAG,
			 * "Received string: " + readMessage);
			 * //Toast.makeText(getApplicationContext
			 * (),readMessage,Toast.LENGTH_SHORT).show(); break;
			 */
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				// Toast.makeText(getApplicationContext(),"Connected to: " +
				// mConnectedDeviceName,Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				unregisterListeners();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		/*
		 * case REQUEST_CONNECT_DEVICE_SECURE: // When DeviceListActivity
		 * returns with a device to connect if (resultCode ==
		 * Activity.RESULT_OK) { connectDevice(data, true); } break;
		 */
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupBTService();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(getApplicationContext(),
						R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT)
						.show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}
}
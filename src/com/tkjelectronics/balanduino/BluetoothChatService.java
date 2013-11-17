/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tkjelectronics.balanduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread for connecting with a device,
 * and a thread for performing data transmissions when connected.
 */
public class BluetoothChatService {
    // Debugging
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = BalanduinoActivity.D;

    // RFCOMM/SPP UUID
    private static final UUID UUID_RFCOMM_GENERIC = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0; // we're doing nothing
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2; // now connected to a remote device

    boolean stopReading; // This is used to stop it from reading on the inputStream
    public boolean newConnection; // Prevent it from calling connectionFailed() if it trying to start a new connection

    private static final int MAXRETRIES = 100; // I know this might seem way too high! But it seems to work pretty well
    public int nRetries = 0;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothChatService(Handler handler, BluetoothAdapter mBluetoothAdapter) {
        mAdapter = mBluetoothAdapter;
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D)
            Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BalanduinoActivity.MESSAGE_STATE_CHANGE, state,
                -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        if (D)
            Log.d(TAG, "start");

        stopReading = true;

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        //setState(STATE_NONE);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        if (D)
            Log.d(TAG, "connect to: " + device);

        stopReading = true;

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device, final String socketType) {
        if (D)
            Log.d(TAG, "connected, Socket Type: " + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler
                .obtainMessage(BalanduinoActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BalanduinoActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D)
            Log.d(TAG, "stop");
        stopReading = true;
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mState == STATE_CONNECTED)
            disconnectSuccess();
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    public void write(String string) {
        write(string.getBytes());
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        Message msg;
        if (nRetries < MAXRETRIES) { // There is a bug in the Android core, so we need to connect twice for it to work all everytime
            nRetries++;
            // Send a retry message back to the Activity
            msg = mHandler.obtainMessage(BalanduinoActivity.MESSAGE_RETRY);
        } else {
            // Send a failure message back to the Activity
            msg = mHandler.obtainMessage(BalanduinoActivity.MESSAGE_DISCONNECTED);
            Bundle bundle = new Bundle();
            bundle.putString(BalanduinoActivity.TOAST, "Unable to connect to device");
            msg.setData(bundle);
        }
        if (!newConnection) {
            mHandler.sendMessage(msg); // Send message
            BluetoothChatService.this.start(); // Start the service over to restart listening mode
        }
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() { // Send a failure message back to the Activity
        setState(STATE_NONE);
        Message msg = mHandler.obtainMessage(BalanduinoActivity.MESSAGE_DISCONNECTED);
        Bundle bundle = new Bundle();
        bundle.putString(BalanduinoActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }

    private void disconnectSuccess() {
        // Send a success message back to the Activity
        Message msg = mHandler.obtainMessage(BalanduinoActivity.MESSAGE_DISCONNECTED);
        Bundle bundle = new Bundle();
        bundle.putString(BalanduinoActivity.TOAST, "Disconnected successfully");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device
                            .createRfcommSocketToServiceRecord(UUID_RFCOMM_GENERIC);
                } else {
                    tmp = device
                            .createInsecureRfcommSocketToServiceRecord(UUID_RFCOMM_GENERIC);
                }
            } catch (IOException e) {
                if (D)
                    Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            if (D)
                Log.i(TAG, "BEGIN mConnectThread SocketType: " + mSocketType);
            //setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            newConnection = false;

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    if (D)
                        Log.e(TAG, "unable to close() " + mSocketType
                                + " socket during connection failure", e2);
                }
                if (!newConnection)
                    connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                if (D)
                    Log.e(TAG, "close() of connect " + mSocketType
                            + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device. It handles all
     * incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            if (D)
                Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                if (D)
                    Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            stopReading = false;
        }

        public void run() {
            if (D)
                Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (!stopReading) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    String readMessage = new String(buffer, 0, bytes);
                    String[] splitMessage = readMessage.split(",");
                    if (D) {
                        Log.i(TAG, "Received string: " + readMessage);
                        for (int i = 0; i < splitMessage.length; i++)
                            Log.i(TAG, "splitMessage[" + i + "]: " + splitMessage[i]);
                    }
                    if (splitMessage[0].trim().equals(BalanduinoActivity.responsePIDValues) && splitMessage.length == BalanduinoActivity.responsePIDValuesLength) {
                        BalanduinoActivity.pValue = splitMessage[1].trim();
                        BalanduinoActivity.iValue = splitMessage[2].trim();
                        BalanduinoActivity.dValue = splitMessage[3].trim();
                        BalanduinoActivity.targetAngleValue = splitMessage[4].trim();
                        BalanduinoActivity.newPIDValues = true;

                        // Send message back to the UI Activity
                        mHandler.obtainMessage(BalanduinoActivity.MESSAGE_READ).sendToTarget();
                    } else if (splitMessage[0].trim().equals(BalanduinoActivity.responseSettings) && splitMessage.length == BalanduinoActivity.responseSettingsLength) {
                        BalanduinoActivity.backToSpot = splitMessage[1].trim().equals("1");
                        BalanduinoActivity.maxAngle = Integer.parseInt(splitMessage[2].trim());
                        BalanduinoActivity.maxTurning = Integer.parseInt(splitMessage[3].trim());
                    } else if (splitMessage[0].trim().equals(BalanduinoActivity.responseInfo) && splitMessage.length == BalanduinoActivity.responseInfoLength) {
                        BalanduinoActivity.firmwareVersion = splitMessage[1].trim();
                        BalanduinoActivity.eepromVersion = splitMessage[2].trim();
                        BalanduinoActivity.mcu = splitMessage[3].trim();
                        BalanduinoActivity.newInfo = true;

                        // Send message back to the UI Activity
                        mHandler.obtainMessage(BalanduinoActivity.MESSAGE_READ).sendToTarget();
                    } else if (splitMessage[0].trim().equals(BalanduinoActivity.responseStatus) && splitMessage.length == BalanduinoActivity.responseStatusLength) {
                        BalanduinoActivity.batteryLevel = splitMessage[1].trim();
                        BalanduinoActivity.runtime = Double.parseDouble(splitMessage[2].trim());
                        BalanduinoActivity.newStatus = true;

                        // Send message back to the UI Activity
                        mHandler.obtainMessage(BalanduinoActivity.MESSAGE_READ).sendToTarget();
                    } else if (splitMessage[0].trim().equals(BalanduinoActivity.responseKalmanValues) && splitMessage.length == BalanduinoActivity.responseKalmanValuesLength) {
                        BalanduinoActivity.Qangle = splitMessage[1].trim();
                        BalanduinoActivity.Qbias = splitMessage[2].trim();
                        BalanduinoActivity.Rmeasure = splitMessage[3].trim();
                        BalanduinoActivity.newKalmanValues = true;

                        // Send message back to the UI Activity
                        mHandler.obtainMessage(BalanduinoActivity.MESSAGE_READ).sendToTarget();
                    } else if (splitMessage[0].trim().equals(BalanduinoActivity.responseIMU) && splitMessage.length == BalanduinoActivity.responseIMULength) {
                        BalanduinoActivity.accValue = splitMessage[1].trim();
                        BalanduinoActivity.gyroValue = splitMessage[2].trim();
                        BalanduinoActivity.kalmanValue = splitMessage[3].trim();
                        BalanduinoActivity.newIMUValues = true;

                        // Send message back to the UI Activity
                        mHandler.obtainMessage(BalanduinoActivity.MESSAGE_READ).sendToTarget();
                    } else if (splitMessage[0].trim().equals(BalanduinoActivity.responsePairWii) && splitMessage.length == BalanduinoActivity.responsePairWiiLength) {
                        BalanduinoActivity.pairingWithWii = true;

                        // Send message back to the UI Activity
                        mHandler.obtainMessage(BalanduinoActivity.MESSAGE_READ).sendToTarget();
                    }
                } catch (IOException e) {
                    if (D)
                        Log.e(TAG, "disconnected", e);
                    if (!stopReading) {
                        cancel();
                        connectionLost();
                    }
                    return;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                if (D)
                    Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            if (mmInStream != null) {
                try {
                    mmInStream.close();
                } catch (Exception ignored) {
                }
            }
            if (mmOutStream != null) {
                try {
                    mmOutStream.close();
                } catch (Exception ignored) {
                }
            }
            if (mmSocket != null) {
                try {
                    mmSocket.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
package com.tkjelectronics.balanduino;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Map;

import com.physicaloid.lib.Boards;
import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.Physicaloid.UploadCallBack;
import com.physicaloid.lib.programmer.avr.UploadErrors;

public class Upload {
    private static final String TAG = "Upload";
    private static final boolean D = BalanduinoActivity.D;
    private static final String ACTION_USB_PERMISSION = "com.tkjelectronics.balanduino.USB_PERMISSION";
    private final static String usbError = "Error opening USB host communication";
    public final static String flavor = "Usb";

    static Physicaloid mPhysicaloid;
    static boolean uploading = false;

    public static void close() {
        if (mPhysicaloid != null) {
            try {
                mPhysicaloid.close();
            } catch (RuntimeException e) {
                if (D)
                    Log.e(TAG, e.toString());
            }
        }
    }

    public static void uploadFirmware() {
        if (uploading)
            return;

        UsbManager mUsbManager = (UsbManager) BalanduinoActivity.activity.getSystemService(BalanduinoActivity.context.USB_SERVICE);
        Map<String, UsbDevice> map = mUsbManager.getDeviceList();
        if (D)
            Log.i(TAG, "UsbDevices: " + map);

        boolean deviceFound = false;
        for (Map.Entry<String, UsbDevice> entry : map.entrySet()) {
            UsbDevice mUsbDevice = entry.getValue();
            if (mUsbDevice.getVendorId() == 0x0403 && mUsbDevice.getProductId() == 0x6015) { // Check if the robot is connected
                deviceFound = true;
                if (mUsbManager.hasPermission(mUsbDevice)) {
                    if (D)
                        Log.i(TAG, "Already has permission");
                    upload();
                } else {
                    if (D)
                        Log.i(TAG, "Requesting permission");
                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(BalanduinoActivity.context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    BalanduinoActivity.activity.registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
                    mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
                }
            }
        }
        if (!deviceFound)
            BalanduinoActivity.showToast("Please connect the Balanduino to the USB Host port", Toast.LENGTH_SHORT);
    }

    private static final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (D)
                            Log.i(TAG, "Permission allowed");
                        upload();
                    }
                    else {
                        if (D)
                            Log.e(TAG, "Permission denied");
                    }
                }
            }
        }
    };

    private static void upload() {
        if (mPhysicaloid == null)
            mPhysicaloid = new Physicaloid(BalanduinoActivity.activity);

        try {
            mPhysicaloid.upload(Boards.BALANDUINO, BalanduinoActivity.context.getResources().getAssets().open("Balanduino.hex"), mUploadCallback);
        } catch (RuntimeException e) {
            if (D)
                Log.e(TAG, e.toString());
            BalanduinoActivity.showToast(usbError, Toast.LENGTH_SHORT);
        } catch (IOException e) {
            if (D)
                Log.e(TAG, e.toString());
            BalanduinoActivity.showToast(usbError, Toast.LENGTH_SHORT);
        }
    }

    static UploadCallBack mUploadCallback = new UploadCallBack() {
        @Override
        public void onUploading(int value) {
            uploading = true;
            if (D)
                Log.i(TAG, "Uploading: " + value);
        }

        @Override
        public void onPreUpload() {
            uploading = true;
            BalanduinoActivity.activity.runOnUiThread(new Runnable() {
                public void run() {
                    BalanduinoActivity.showToast("Uploading...", Toast.LENGTH_SHORT);
                }
            });
            if (D)
                Log.i(TAG, "Upload start");
        }

        @Override
        public void onPostUpload(boolean success) {
            uploading = false;
            if (success) {
                BalanduinoActivity.activity.runOnUiThread(new Runnable() {
                    public void run() {
                        BalanduinoActivity.showToast("Uploading was successful", Toast.LENGTH_SHORT);
                    }
                });
            } else {
                BalanduinoActivity.activity.runOnUiThread(new Runnable() {
                    public void run() {
                        BalanduinoActivity.showToast("Uploading failed", Toast.LENGTH_SHORT);
                    }
                });
            }
        }

        @Override
        public void onError(UploadErrors err) {
            uploading = false;
            BalanduinoActivity.activity.runOnUiThread(new Runnable() {
                public void run() {
                    BalanduinoActivity.showToast("Uploading error", Toast.LENGTH_SHORT);
                }
            });
            if (D)
                Log.e(TAG, "Error: " + err.toString());
        }
    };
}
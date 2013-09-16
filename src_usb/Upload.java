package com.tkjelectronics.balanduino;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import com.physicaloid.lib.Boards;
import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.Physicaloid.UploadCallBack;
import com.physicaloid.lib.programmer.avr.UploadErrors;

public class Upload {
    private static final String TAG = "Upload";
    private static final boolean D = BalanduinoActivity.D;

    static Physicaloid mPhysicaloid;
    public final static String flavor = "Usb";
    final static String usbError = "Error opening USB host communication";
    static boolean uploading = false;


    public static void UploadFirmware() {
        if (uploading)
            return;

        if (mPhysicaloid == null)
            mPhysicaloid = new Physicaloid(BalanduinoActivity.activity);

        try {
            mPhysicaloid.upload(Boards.BALANDUINO, BalanduinoActivity.context.getResources().getAssets().open("Blink.balanduino.hex"), mUploadCallback);
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
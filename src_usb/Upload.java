package com.tkjelectronics.balanduino;

import android.util.Log;

import java.io.IOException;

import com.physicaloid.lib.Boards;
import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.Physicaloid.UploadCallBack;
import com.physicaloid.lib.programmer.avr.UploadErrors;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;

public class Upload {
    // Debugging
    private static final String TAG = "Upload";
    private static final boolean D = BalanduinoActivity.D;

    public final static String flavor = "Usb";

    final static Physicaloid mPhysicaloid = new Physicaloid(BalanduinoActivity.activity);

    public static void UploadFirmware() {
    	try {
            //mPhysicaloid.upload(Boards.ARDUINO_UNO, BalanduinoActivity.context.getResources().getAssets().open("Blink.uno.hex"), mUploadCallback);
            mPhysicaloid.upload(Boards.ARDUINO_UNO, BalanduinoActivity.context.getResources().getAssets().open("Blink.cpp.hex"), mUploadCallback);
        } catch (RuntimeException e) {
            if (D)
                Log.e(TAG, e.toString());
        } catch (IOException e) {
            if (D)
                Log.e(TAG, e.toString());
        }
    }

    static UploadCallBack mUploadCallback = new UploadCallBack() {
        @Override
        public void onUploading(int value) {
            if (D)
                Log.i(TAG, "Uploading: " + value);
        }

        @Override
        public void onPreUpload() {
            if (D)
                Log.i(TAG, "Upload start");
        }

        @Override
        public void onPostUpload(boolean success) {
            if (D) {
                if (success)
                    Log.i(TAG, "Upload successful");
                else
                    Log.i(TAG, "Upload fail");
            }
        }

        @Override
        public void onError(UploadErrors err) {
            if (D)
                Log.e(TAG, "Error: " + err.toString());
        }
    };
}
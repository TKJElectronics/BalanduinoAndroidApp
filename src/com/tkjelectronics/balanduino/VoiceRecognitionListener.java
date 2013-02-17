package com.tkjelectronics.balanduino;

import java.util.ArrayList;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

class VoiceRecognitionListener implements RecognitionListener {
	private static final String TAG = "VoiceRecognitionListener";
	private boolean D = BalanduinoActivity.D;
	
	private String waiting = "Waiting for result...";
	
	public void onResults(Bundle data) {
		if(D)
			Log.d(TAG, "onResults " + data);
		
		ArrayList<String> matches = data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);		
		for(int i=0;i<matches.size();i++) {
			if(D)
				Log.d(TAG, matches.get(i));
			if(matches.get(i).contains("sto")) {
				VoiceRecognitionFragment.mText.setText("Stop");
				sendCommand("S;");
				break;
			}
			else if(matches.get(i).contains("for")) {
				VoiceRecognitionFragment.mText.setText("Forward");
				sendCommand("J,0,0.2;"); // Send data to the robot 
				break;
			}
			else if(matches.get(i).contains("bac")) {
				VoiceRecognitionFragment.mText.setText("Backward");
				sendCommand("J,0,-0.2;"); // Send data to the robot
				break;
			}
			else if(matches.get(i).contains("lef")) {
				VoiceRecognitionFragment.mText.setText("Left");
				sendCommand("J,-0.3,0;"); // Send data to the robot
				break;
			}
			else if(matches.get(i).contains("rig")) {
				VoiceRecognitionFragment.mText.setText("Right");
				sendCommand("J,0.3,0;"); // Send data to the robot
				break;
			}
		}
		if(VoiceRecognitionFragment.mText.getText().equals(waiting))
			VoiceRecognitionFragment.mText.setText("No match!");
		
		if(BalanduinoActivity.toggleButtonState) // Start it again if the button is still checked
			BalanduinoActivity.startSpeechRecognizer();
	}
	private void sendCommand(String message) {
		if (BalanduinoActivity.mChatService == null)
			return;
		if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED && BalanduinoActivity.currentTabSelected == ViewPagerAdapter.VOICERECOGNITION_FRAGMENT) {
			byte[] send = message.getBytes();
			BalanduinoActivity.mChatService.write(send, false);
		}
	}
	public void onReadyForSpeech(Bundle params) {
		VoiceRecognitionFragment.mText.setText("Awaiting command");
	}
	public void onBeginningOfSpeech() {
		VoiceRecognitionFragment.mText.setText("Sounding good!");
	}	
	public void onEndOfSpeech() {
		VoiceRecognitionFragment.mText.setText(waiting);
	}
	public void onError(int error) {
		if(D)
			Log.e(TAG, "error " + error);
		sendCommand("S;");
		BalanduinoActivity.restartSpeechRecognizer();
		VoiceRecognitionFragment.mText.setText("error " + error);
	}
	public void onBufferReceived(byte[] buffer) {
	}
	public void onEvent(int eventType, Bundle params) {
	}
	public void onPartialResults(Bundle partialResults) {
	}
	public void onRmsChanged(float rmsdB) {
	}
}
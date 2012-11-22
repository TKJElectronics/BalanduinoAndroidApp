package com.tkjelectronics.balanduino;

import java.util.ArrayList;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

class VoiceRecognitionListener implements RecognitionListener {
	private static final String TAG = "VoiceRecognitionListener";
	private boolean D = BalanduinoActivity.D;
	
	public void onResults(Bundle data) {
		if(D)
			Log.d(TAG, "onResults " + data);
		
		ArrayList<String> matches = data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);		
		for(int i=0;i<matches.size();i++) {
			if(D)
				Log.d(TAG, matches.get(i));
			if(matches.get(i).contains("sto")) {
				VoiceRecognitionFragment.mText.setText("Stop");
			}
			else if(matches.get(i).contains("for")) {
				VoiceRecognitionFragment.mText.setText("Forward");
			}
			else if(matches.get(i).contains("bac")) {
				VoiceRecognitionFragment.mText.setText("Backward");
			}
			else if(matches.get(i).contains("lef")) {
				VoiceRecognitionFragment.mText.setText("Left");
			}
			else if(matches.get(i).contains("rig")) {
				VoiceRecognitionFragment.mText.setText("Right");
			}
		}
		if(VoiceRecognitionFragment.mText.getText().equals("Waiting for result..."))
			VoiceRecognitionFragment.mText.setText("No match!");
		
		if(BalanduinoActivity.toggleButtonState) // Start it again if the button is still checked
			BalanduinoActivity.startSpeechRecognizer();
	}
	
	public void onBeginningOfSpeech() {
		//Log.d(TAG, "onBeginningOfSpeech");
		VoiceRecognitionFragment.mText.setText("Sounding good!");
	}
	public void onBufferReceived(byte[] buffer) {
		//Log.d(TAG, "onBufferReceived");
	}
	public void onEndOfSpeech() {
		//Log.d(TAG, "onEndofSpeech");
		VoiceRecognitionFragment.mText.setText("Waiting for result...");
	}
	public void onError(int error) {
		if(D)
			Log.e(TAG, "error " + error);
		BalanduinoActivity.restartSpeechRecognizer();
		VoiceRecognitionFragment.mText.setText("error " + error);
	}
	public void onEvent(int eventType, Bundle params) {
		//Log.d(TAG, "onEvent " + eventType);
	}
	public void onPartialResults(Bundle partialResults) {
		//Log.d(TAG, "onPartialResults");
	}
	public void onReadyForSpeech(Bundle params) {
		//Log.d(TAG, "onReadyForSpeech");
	}		
	public void onRmsChanged(float rmsdB) {
		//Log.d(TAG, "onRmsChanged");
	}
}
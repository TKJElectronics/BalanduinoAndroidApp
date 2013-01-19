package com.tkjelectronics.balanduino;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;

public class VoiceRecognitionFragment extends SherlockFragment {
	public static TextView mText;
	ToggleButton mButton;
	signalListener mCallback;	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.voicerecognition, container, false);

		mText = (TextView) v.findViewById(R.id.textView1);		
		mButton = (ToggleButton) v.findViewById(R.id.toggleButton);
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Is the toggle on?
			    boolean state = ((ToggleButton)v).isChecked();
			    mCallback.toggleButtonChanged(state);		
			}
		});
		return v;
	}
	
	public interface signalListener {
	    void toggleButtonChanged(boolean toggleButton);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	    // This makes sure that the container activity has implemented
	    // the callback interface. If not, it throws an exception
		try {
			mCallback = (signalListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement signalListener");
		}
	}
	@Override
	public void onPause() {
		mCallback.toggleButtonChanged(false);
		mButton.setChecked(false);
		super.onPause();
	}
}
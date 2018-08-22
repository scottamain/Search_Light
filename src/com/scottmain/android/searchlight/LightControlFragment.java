package com.scottmain.android.searchlight;

import android.app.Activity;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;

public class LightControlFragment extends Fragment {
	private final static String EXTRA_MODE = "mode";
	private final static String EXTRA_ON = "on";
	int mCurrentMode;
	boolean mOn;
	TransitionDrawable mDrawable;
	ImageButton mBulb;
	LightSwitch mLightswitch;

	// Empty constructor required
	public LightControlFragment() {}

    /**
     * Create a new instance, providing the current light mode as an argument.
     */
    static LightControlFragment newInstance(int mode, boolean on) {
    	LightControlFragment fragment = new LightControlFragment();

        Bundle args = new Bundle();
        args.putInt(EXTRA_MODE, mode);
        args.putBoolean(EXTRA_ON, on);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        
    	mCurrentMode = getArguments().getInt(EXTRA_MODE);
    	mOn = getArguments().getBoolean(EXTRA_ON);
 
        switch(mCurrentMode) {
        case R.id.mode_blackout:
        	return inflater.inflate(R.layout.black, container, false);
        case R.id.mode_viewfinder:
        	return inflater.inflate(R.layout.viewfinder, container, false);
        case R.id.mode_lightswitch:
        	return inflater.inflate(R.layout.lightswitch, container, false);
        case R.id.mode_lightbulb:
        default:
        	return inflater.inflate(R.layout.bulb, container, false);
        }
    }
    
    
    
	@Override
	public void onResume() {

    	// to fade the settings button
    	ImageButton settingsButton = (ImageButton) getActivity().findViewById(R.id.button_settings);
    	
        switch(mCurrentMode) {
        case R.id.mode_lightswitch:
	    	mLightswitch = (LightSwitch) getActivity().findViewById(R.id.button_lightswitch);
	    	mLightswitch.setChecked(false);
	    	if (mOn) mLightswitch.setChecked(true);
	    	mLightswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton button, boolean isChecked) {
					mListener.onLightControlClick(isChecked);
				}
	    	});
        	settingsButton.setAlpha(255);
	    	break;
        case R.id.mode_blackout:
        	Button image = (Button) getActivity().findViewById(R.id.button_black);
        	Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        	image.startAnimation(fadeOut);
        	settingsButton.setAlpha(90);
        	break;
        case R.id.mode_viewfinder:
        case R.id.mode_lightbulb:
        	mBulb = (ImageButton) getActivity().findViewById(R.id.button_bulb);
            mDrawable = (TransitionDrawable) mBulb.getDrawable();
            mDrawable.setCrossFadeEnabled(true);
            if (mOn) mDrawable.startTransition(0);
        	settingsButton.setAlpha(255);

            break;
        }

    	PreviewSurface surface = (PreviewSurface) getActivity().findViewById(R.id.surface);
        if (mCurrentMode == R.id.mode_viewfinder) {
        	surface.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else {
        	surface.setLayoutParams(new LayoutParams(1, 1));
        }
		
		super.onResume();
	}

	@Override
	public void onStop() {
		stopLightControl();
		super.onStop();
	}
	
	@Override
	public void onDestroyView() {
		stopLightControl();
		super.onDestroyView();
	}

	private void stopLightControl() {
        switch(mCurrentMode) {
        case R.id.mode_lightbulb:
        case R.id.mode_viewfinder:
			// kill any ongoing transition so it's not still finishing when we resume
			mDrawable.resetTransition();
			break;
        }
	}

	public void toggleLightControl(boolean on) {
    	if (on) {
    	    // Update UI
    	    switch (mCurrentMode) {
    	    case R.id.mode_lightbulb:
    	    case R.id.mode_viewfinder:
        	    mDrawable.startTransition(200);
        	    break;
    	    case R.id.mode_lightswitch:
            	mLightswitch.setChecked(true);
        	    break;
    	    }
    	} else {
    	    // Update UI
    	    switch (mCurrentMode) {
    	    case R.id.mode_lightbulb:
    	    case R.id.mode_viewfinder:
    	        mDrawable.reverseTransition(300);
        	    break;
    	    case R.id.mode_lightswitch:
            	mLightswitch.setChecked(false);
        	    break;
        	    
    	    }
    	}
    }

	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks. */
    public interface LightControlListener {
        public void onLightControlClick(boolean on);
    }
    
    // Use this instance of the interface to deliver action events
    LightControlListener mListener;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the LightControlListener so we can send events to the host
            mListener = (LightControlListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement LightControlListener");
        }
    }
}

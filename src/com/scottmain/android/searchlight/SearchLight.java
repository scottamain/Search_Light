/*
Copyright 2010 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.scottmain.android.searchlight;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout.LayoutParams;

public class SearchLight extends FragmentActivity implements PreviewSurface.Callback, 
		ModeDialogFragment.ModeDialogListener, LightControlFragment.LightControlListener  {
	//private final static String TAG = "SearchLight";
	private final static String MODE_TYPE = "mode_type";
	
	PreviewSurface mSurface;
	boolean on = false;
	boolean paused = false;
	boolean skipAnimate = false;
	boolean mSystemUiVisible = true;
	boolean mCameraReady = false; // to make sure we don't turn on light when preview surface resizes
	int mCurrentMode;
	
	
	FragmentManager mFragmentManager;
	LightControlFragment mCurrentFragment;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        mSurface = (PreviewSurface) findViewById(R.id.surface);
        mSurface.setCallback(this);
        
        int mode; // viewing mode
        
        // When user selects mode from menu, there's a mode type
        mode = getIntent().getIntExtra(MODE_TYPE, 0);
        // When launched clean, there's no mode in the intent, so check preference
        if (mode == 0) {
            SharedPreferences modePreferences = getPreferences(Context.MODE_PRIVATE);
            mode = modePreferences.getInt(MODE_TYPE, 0);
            
            // Rewrite the intent to carry the desired mode
            Intent intent = getIntent();
	        intent.putExtra(MODE_TYPE, mode);
            setIntent(intent);
        }
        
        switch(mode) {
        case R.id.mode_blackout:
        	mCurrentMode = R.id.mode_blackout;
        	break;
        case R.id.mode_viewfinder:
        	mCurrentMode = R.id.mode_viewfinder;
        	break;
        case R.id.mode_lightswitch:
        	mCurrentMode = R.id.mode_lightswitch;
        	break;
        case R.id.mode_lightbulb:
        default:
            mCurrentMode = R.id.mode_lightbulb;
        	break;
        }
        
        // Set up layout with initial controller fragment
        mFragmentManager = getSupportFragmentManager();
        switchControlFragment(mCurrentMode);
    }
    


    /** Implementation of LightControlFragment callback. Primarily for LightSwitch mode */
	@Override
	public void onLightControlClick(boolean on) {
		if (on) turnOn();
		else turnOff();
	}

    /** Click event for all light controllers */
    public void toggleLight(View v) {
    	if (on) {
    		turnOff();
    	} else {
    		turnOn();
    	}
    }
    
    private void turnOn() {
    	if (!on) {
    	    on = true;
    	    mSurface.lightOn();
    	    mCurrentFragment.toggleLightControl(on);
    	}
    }
    
    private void turnOff() {
    	if (on) {
	        on = false;
    	    mSurface.lightOff();
    	    mCurrentFragment.toggleLightControl(on);
    	}
    }

	@Override
	protected void onPause() {
		super.onPause();
		turnOff();
		mSurface.releaseCamera();
		paused = true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (paused) {
			mSurface.initCamera();
		}
		mCameraReady = false;
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Save the current mode so it's not lost when process stops
		SharedPreferences modePreferences = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = modePreferences.edit();
		editor.putInt(MODE_TYPE, mCurrentMode);
		editor.commit();
		finish(); // I give up, the camera surface doesn't come back on resume, so just kill it all
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
    	skipAnimate = false;
        
		if (hasFocus && paused) {
			mSurface.startPreview();
			paused = false;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case R.id.dialog_camera_na:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dialog_camera_na)
			       .setCancelable(false)
			       .setNeutralButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                SearchLight.this.finish();
			           }
			       });
			return builder.create();
		default:
			return super.onCreateDialog(id);
		}
	}

	/** In case a device has a MENU button, show the mode dialog when it's pressed */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    showModeDialog(findViewById(R.id.button_settings));
	    return true;
	}
	

	/** Implement the ModeDialogFragment's callback interface method */
	@Override
	public void onModeClick(int mode) {
		if (mCurrentMode == mode) return;

		mCurrentMode = mode;
		skipAnimate = true;
	    if (mode != -1) {
	    	switchControlFragment(mode);
	    }
	}
	
	private void switchControlFragment(int mode) {
    	// update activity state w/ new mode
        Intent intent = getIntent();
        intent.putExtra(MODE_TYPE, mode);
        setIntent(intent);
        
        // switch fragments
        LightControlFragment newFragment = LightControlFragment.newInstance(mode, on);
        mFragmentManager.beginTransaction().replace(R.id.controller_fragment, newFragment).commit();
        mCurrentFragment = newFragment;
        if (mode == R.id.mode_viewfinder) {
        	mSurface.setIsViewfinder();
        }
	}
	
	/** Call this to show the dialog with different light modes */
	public void showModeDialog(View v) {
		int currentMode = getIntent().getIntExtra(MODE_TYPE, R.id.mode_lightbulb);
	    DialogFragment newFragment = ModeDialogFragment.newInstance(currentMode);
	    newFragment.show(getSupportFragmentManager(), "mode_dialog");
	}

	public void cameraReady() {
		if (!mCameraReady) {
			mCameraReady = true;
			turnOn();
		}
	}
	
	public void cameraNotAvailable() {
		showDialog(R.id.dialog_camera_na);
	}
	
	
	public static class HackPreviewSurfaceFragment extends Fragment {
		public void HackPreviewSurfaceFragment() {}

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
	        
	    	return inflater.inflate(R.layout.hack_previewsurface, container, false);
	    }
	}
	
}
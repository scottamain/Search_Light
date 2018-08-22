package com.scottmain.android.searchlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ModeDialogFragment extends DialogFragment {
	private final static String EXTRA_CURRENT_MODE = "current_mode";

    /**
     * Create a new instance, providing the current light mode as an argument.
     */
    static ModeDialogFragment newInstance(int currentMode) {
    	ModeDialogFragment fragment = new ModeDialogFragment();

        Bundle args = new Bundle();
        args.putInt(EXTRA_CURRENT_MODE, currentMode);
        fragment.setArguments(args);

        return fragment;
    }
    
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int currentMode = getArguments().getInt(EXTRA_CURRENT_MODE);
		
		// This is why the string-array and integer-array order MUST match exactly
		// translate the 'which' index position to the mode ID
		switch (currentMode){
		case R.id.mode_lightbulb:
			currentMode = 0;
			break;
		case R.id.mode_lightswitch:
			currentMode = 1;
			break;
		case R.id.mode_viewfinder:
			currentMode = 2;
			break;
		case R.id.mode_blackout:
			currentMode = 3;
			break;
		}
    	
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setSingleChoiceItems(R.array.modes, currentMode,
                new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						// This is why the string-array and integer-array order MUST match exactly
						// translate the 'which' index position to the mode ID
						switch (which){
						case 0:
							which = R.id.mode_lightbulb;
							break;
						case 1:
							which = R.id.mode_lightswitch;
							break;
						case 2:
							which = R.id.mode_viewfinder;
							break;
						case 3:
							which = R.id.mode_blackout;
							break;
						}
						dismiss();
						
						// pass which item was clicked back to the activity
						mListener.onModeClick(which);
					}
				});
        return builder.create();
    }
    

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks. */
    public interface ModeDialogListener {
        public void onModeClick(int which);
    }
    
    // Use this instance of the interface to deliver action events
    ModeDialogListener mListener;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ModeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ModeDialogListener");
        }
    }
}

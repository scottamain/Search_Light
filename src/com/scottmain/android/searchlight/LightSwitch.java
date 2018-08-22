/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.scottmain.android.searchlight;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.CompoundButton;




// TODO: MOVE LOTS OF STUFF OFF MAIN THREAD



/**
 * A Switch is a two-state toggle switch widget that can select between two
 * options. The user may drag the "thumb" back and forth to choose the selected option,
 * or simply tap to toggle as if it were a checkbox. The {@link #setText(CharSequence) text}
 * property controls the text displayed in the label for the switch, whereas the
 * {@link #setTextOff(CharSequence) off} and {@link #setTextOn(CharSequence) on} text
 * controls the text on the thumb. Similarly, the
 * {@link #setTextAppearance(android.content.Context, int) textAppearance} and the related
 * setTypeface() methods control the typeface and style of label text, whereas the
 * {@link #setSwitchTextAppearance(android.content.Context, int) switchTextAppearance} and
 * the related seSwitchTypeface() methods control that of the thumb.
 */
public class LightSwitch extends CompoundButton {
    private static final int TOUCH_MODE_IDLE = 0;
    private static final int TOUCH_MODE_DOWN = 1;
    private static final int TOUCH_MODE_DRAGGING = 2;

    private Drawable mThumbDrawable;
    private Drawable mTrackDrawable;
    private int mThumbTextPadding;
    private int mSwitchMinWidth;
    private int mSwitchPadding;
    private CharSequence mTextOn;
    private CharSequence mTextOff;

    private int mTouchMode;
    private int mTouchSlop;
    private float mTouchX;
    private float mTouchY;

    private float mThumbPosition;
    private int mSwitchWidth;
    private int mSwitchHeight;
    private int mThumbHeight; // Does not include padding

    private int mSwitchLeft;
    private int mSwitchTop;
    private int mSwitchRight;
    private int mSwitchBottom;

    private TextPaint mTextPaint;
    private ColorStateList mTextColors;
    private Layout mOnLayout;
    private Layout mOffLayout;

    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };

    /**
     * Construct a new Switch with default styling.
     *
     * @param context The Context that will determine this widget's theming.
     */
    public LightSwitch(Context context) {
        this(context, null);
    }

    /**
     * Construct a new Switch with default styling, overriding specific style
     * attributes as requested.
     *
     * @param context The Context that will determine this widget's theming.
     * @param attrs Specification of attributes that should deviate from default styling.
     */
    public LightSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.switchStyle);
    }

    /**
     * Construct a new Switch with a default style determined by the given theme attribute,
     * overriding specific style attributes as requested.
     *
     * @param context The Context that will determine this widget's theming.
     * @param attrs Specification of attributes that should deviate from the default styling.
     * @param defStyle An attribute ID within the active theme containing a reference to the
     *                 default style for this widget. e.g. android.R.attr.switchStyle.
     */
    public LightSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        Resources res = getResources();
        mTextPaint.density = res.getDisplayMetrics().density;

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.LightSwitch, defStyle, 0);

        mThumbDrawable = a.getDrawable(R.styleable.LightSwitch_thumb);
        mTrackDrawable = a.getDrawable(R.styleable.LightSwitch_track);
        mTextOn = a.getText(R.styleable.LightSwitch_textOn);
        mTextOff = a.getText(R.styleable.LightSwitch_textOff);
        mThumbTextPadding = a.getDimensionPixelSize(R.styleable.LightSwitch_thumbTextPadding, 0);
        mSwitchMinWidth = a.getDimensionPixelSize(R.styleable.LightSwitch_switchMinWidth, 0);
        mSwitchPadding = a.getDimensionPixelSize(R.styleable.LightSwitch_switchPadding, 0);
        mThumbHeight = a.getDimensionPixelSize(R.styleable.LightSwitch_thumbMinHeight, 0);

        int appearance = a.getResourceId(R.styleable.LightSwitch_switchTextAppearance, 0);
        if (appearance != 0) {
            setSwitchTextAppearance(context, appearance);
        }
        a.recycle();

        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();

        // Refresh display with current params
        refreshDrawableState();
        setChecked(isChecked());
    }

    /**
     * Sets the switch text color, size, style, hint color, and highlight color
     * from the specified TextAppearance resource.
     *
     * @attr ref android.R.styleable#Switch_switchTextAppearance
     */
    public void setSwitchTextAppearance(Context context, int resid) {
        
        TypedArray appearance =
                context.obtainStyledAttributes(resid, R.styleable.LightSwitch_TextAppearance);

        ColorStateList colors;

        colors = appearance.getColorStateList(R.styleable.LightSwitch_TextAppearance_textColor);
        if (colors != null) {
            mTextColors = colors;
        } else {
            // If no color set in TextAppearance, default to the view's textColor
            mTextColors = getTextColors();
        }
        int textSize = appearance.getDimensionPixelSize(R.styleable.LightSwitch_TextAppearance_textSize, 0);
        int textColor = appearance.getColor(R.styleable.LightSwitch_TextAppearance_textColor, 0);

        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
        requestLayout();

        appearance.recycle();
    }


    @Override
    public boolean performClick() {
        /* When clicked, DO NOT toggle the state */
        return true;
    }

    /**
     * @return true if (x, y) is within the target area of the switch thumb
     */
    private boolean hitThumb(float x, float y) {
        final int thumbTop = (int) (mThumbPosition - mTouchSlop);
        final int thumbLeft = mSwitchLeft - mTouchSlop;
        final int thumbRight = mSwitchRight + mTouchSlop;
        final int thumbBottom = (int) (mThumbPosition + mThumbHeight + mTouchSlop);
        return x > thumbLeft && x < thumbRight && y > thumbTop && y < thumbBottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        final float x = ev.getX();
        final float y = ev.getY();
        
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if (isEnabled() && hitThumb(x, y)) {
                    mTouchMode = TOUCH_MODE_DOWN;
                    mTouchX = x;
                    mTouchY = y;
                } else {
                	return true; // If it wasn't a hit on the thumb, return true
                	             // so the button does not go into "pressed" state
                	             // which changes the thumb color even when not touched
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                switch (mTouchMode) {
                    case TOUCH_MODE_IDLE:
                        // Didn't target the thumb, treat normally.
                        break;

                    case TOUCH_MODE_DOWN: {
                        if (Math.abs(x - mTouchX) > mTouchSlop ||
                                Math.abs(y - mTouchY) > mTouchSlop) {
                            mTouchMode = TOUCH_MODE_DRAGGING;
                            getParent().requestDisallowInterceptTouchEvent(true);
                            mTouchX = x;
                            mTouchY = y;
                            return true;
                        }
                        break;
                    }

                    case TOUCH_MODE_DRAGGING: {
                        final float dy = y - mTouchY;
                        float newPos = Math.max(0,
                                Math.min(mThumbPosition + dy, getThumbScrollRange()));
                        if (newPos != mThumbPosition) {
                            mThumbPosition = newPos;
                            mTouchY = y;
                            invalidate();
                        }
                        return true;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mTouchMode == TOUCH_MODE_DRAGGING) {
                    stopDrag(ev);
                    return true;
                }
                mTouchMode = TOUCH_MODE_IDLE;
                break;
            }
        }

        return super.onTouchEvent(ev);
    }

    private void cancelSuperTouch(MotionEvent ev) {
        MotionEvent cancel = MotionEvent.obtain(ev);
        cancel.setAction(MotionEvent.ACTION_CANCEL);
        super.onTouchEvent(cancel);
        cancel.recycle();
    }

    /**
     * Called from onTouchEvent to end a drag operation.
     *
     * @param ev Event that triggered the end of drag mode - ACTION_UP or ACTION_CANCEL
     */
    private void stopDrag(MotionEvent ev) {
        mTouchMode = TOUCH_MODE_IDLE;
        // Up and not canceled, also checks the switch has not been disabled during the drag
        boolean commitChange = ev.getAction() == MotionEvent.ACTION_UP && isEnabled();

        cancelSuperTouch(ev);

        if (commitChange) {
        	setChecked(isThumbPositionCheckedState());
        } else {
        	setChecked(isChecked());
        }
    }

    private boolean isThumbPositionCheckedState() {
    	if (isChecked()) {
    		return mThumbPosition <= getThumbScrollRange() * 2/3;
    	} else {
            return mThumbPosition <= getThumbScrollRange() * 1/3;
    	}
    }

    private void setThumbPosition(boolean checked) {
        mThumbPosition = checked ? 0 : getThumbScrollRange();
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        setThumbPosition(isChecked());
        invalidate();
    }



    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOnLayout == null) {
            mOnLayout = makeLayout(mTextOn);
        }
        if (mOffLayout == null) {
            mOffLayout = makeLayout(mTextOff);
        }

        final int maxTextWidth = Math.max(mOnLayout.getWidth(), mOffLayout.getWidth());
        final int switchWidth = mSwitchMinWidth;
        final int switchHeight = switchWidth * 3;

        mSwitchWidth = switchWidth;
        mSwitchHeight = switchHeight;
        
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int measuredHeight = getMeasuredHeight();
        if (measuredHeight < switchHeight) {
            setMeasuredDimension(getMeasuredWidth(), switchHeight);
        }
    }

    private Layout makeLayout(CharSequence text) {

        return new StaticLayout(text, mTextPaint,
                (int) Math.ceil(Layout.getDesiredWidth(text, mTextPaint)),
                Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        setThumbPosition(isChecked());

        int switchRight;
        int switchLeft;
	    switchRight = getWidth();
	    switchLeft = getPaddingLeft();

        int switchTop = 0;
        int switchBottom = 0;
        switch (getGravity() & Gravity.VERTICAL_GRAVITY_MASK) {
            default:
            case Gravity.TOP:
                switchTop = getPaddingTop();
                switchBottom = switchTop + mSwitchHeight;
                break;

            case Gravity.CENTER_VERTICAL:
                switchTop = (getPaddingTop() + getHeight() - getPaddingBottom()) / 2 -
                        mSwitchHeight / 2;
                switchBottom = switchTop + mSwitchHeight;
                break;

            case Gravity.BOTTOM:
                switchBottom = getHeight() - getPaddingBottom();
                switchTop = switchBottom - mSwitchHeight;
                break;
        }

        mSwitchLeft = switchLeft;
        mSwitchTop = switchTop;
        mSwitchBottom = switchBottom;
        mSwitchRight = switchRight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mTrackDrawable.setBounds(mSwitchLeft, mSwitchTop, mSwitchRight, mSwitchBottom);
        mTrackDrawable.draw(canvas);

        canvas.save();

        canvas.clipRect(mSwitchLeft, mSwitchTop, mSwitchRight, mSwitchBottom);

        int thumbTop = mSwitchTop + (int) (mThumbPosition + 0.5f);

        mThumbDrawable.setBounds(mSwitchLeft + mSwitchPadding, thumbTop + mSwitchPadding,
        		mSwitchRight - mSwitchPadding, (thumbTop + mThumbHeight) - mSwitchPadding);
        mThumbDrawable.draw(canvas);

        // mTextColors should not be null, but just in case
        if (mTextColors != null) {
            mTextPaint.setColor(mTextColors.getColorForState(getDrawableState(),
                    mTextColors.getDefaultColor()));
        }
        mTextPaint.drawableState = getDrawableState();

        // Draw the button text
        Layout switchText = isThumbPositionCheckedState() ? mOnLayout : mOffLayout;
        if (switchText != null) {
            canvas.translate((mSwitchLeft + mSwitchRight) / 2 - switchText.getWidth() / 2,
                    (thumbTop + (thumbTop + mThumbHeight)) / 2 - switchText.getHeight() / 2);
            switchText.draw(canvas);
        }

        canvas.restore();
    }

    @Override
    public int getCompoundPaddingLeft() {
        int padding = super.getCompoundPaddingLeft() + mSwitchWidth;
        if (!TextUtils.isEmpty(getText())) {
            padding += mSwitchPadding;
        }
        return padding;
    }

    @Override
    public int getCompoundPaddingRight() {
        int padding = super.getCompoundPaddingRight() + mSwitchWidth;
        if (!TextUtils.isEmpty(getText())) {
            padding += mSwitchPadding;
        }
        return padding;
    }

    private int getThumbScrollRange() {
        if (mTrackDrawable == null) {
            return 0;
        }
        return mSwitchHeight - mThumbHeight;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        int[] myDrawableState = getDrawableState();

        // Set the state of the Drawable
        // Drawable may be null when checked state is set from XML, from super constructor
        if (mThumbDrawable != null) mThumbDrawable.setState(myDrawableState);
        if (mTrackDrawable != null) mTrackDrawable.setState(myDrawableState);

        invalidate();
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mThumbDrawable || who == mTrackDrawable;
    }
}

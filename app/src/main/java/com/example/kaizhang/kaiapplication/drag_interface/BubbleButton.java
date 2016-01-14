package com.example.kaizhang.kaiapplication.drag_interface;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Button;

import com.example.kaizhang.kaiapplication.utils.CheckLongPressHelper;
import com.example.kaizhang.kaiapplication.utils.Utilities;

/**
 * Created by kai zhang on 2016/1/8.
 */
public class BubbleButton extends Button {
    public enum BelongTo {FunctionList, Hotseat, None}

    private BelongTo belongTo = BelongTo.None;
    private CheckLongPressHelper mLongPressHelper;
    private float mSlop;

    public BubbleButton(Context context) {
        this(context, null);
    }

    public BubbleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLongPressHelper = new CheckLongPressHelper(this);
    }

    public BelongTo getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(BelongTo belongTo) {
        this.belongTo = belongTo;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        mLongPressHelper.cancelLongPress();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // So that the pressed outline is visible immediately on setStayPressed(),
                // we pre-create it on ACTION_DOWN (it takes a small but perceptible amount of time
                // to create it)

                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_UP:
//                mLongPressHelper.checkForClick();
            case MotionEvent.ACTION_CANCEL:
                // If we've touched down and up on an item, and it's still not "pressed", then
                // destroy the pressed outline
                mLongPressHelper.cancelLongPress();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!Utilities.pointInView(this, event.getX(), event.getY(), mSlop)) {
                    mLongPressHelper.cancelLongPress();
                }
                break;
        }
        return result;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public boolean hasPerformedLongPress() {
        return mLongPressHelper.hasPerformedLongPress();
    }
}

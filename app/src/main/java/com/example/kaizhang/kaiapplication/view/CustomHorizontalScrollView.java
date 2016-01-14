package com.example.kaizhang.kaiapplication.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * Created by kai zhang on 2015/12/29.
 */
public class CustomHorizontalScrollView extends HorizontalScrollView {

    private ScrollViewListener scrollViewListener = null;
    private Runnable scrollerTask;
    private int initialPosition;

    private int newCheck = 50;

    public CustomHorizontalScrollView(Context context) {
        this(context, null);
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }


    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    private void initViews() {
        scrollerTask = new Runnable() {
            public void run() {
                int newPosition = getScrollX();
                if (Math.abs(initialPosition - newPosition) <= 20) {//has stopped, use 20 to make judgement faster
                    if (scrollViewListener != null) {
                        scrollViewListener.onScrollStopped(newPosition);
                    }
                } else {
                    initialPosition = getScrollX();
                    CustomHorizontalScrollView.this.postDelayed(scrollerTask, newCheck);
                }
            }
        };
    }

    private void startScrollerTask() {
        initialPosition = getScrollX();
        CustomHorizontalScrollView.this.postDelayed(scrollerTask, newCheck);
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            startScrollerTask();
        }
        return super.onTouchEvent(ev);
    }

    public interface ScrollViewListener {
        void onScrollChanged(View scrollView, int x, int y, int oldx, int oldy);

        void onScrollStopped(int stopPosition);
    }
}

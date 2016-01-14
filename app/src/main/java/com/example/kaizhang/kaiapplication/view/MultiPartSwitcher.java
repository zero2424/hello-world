package com.example.kaizhang.kaiapplication.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by kai zhang on 2015/12/23.
 */
public class MultiPartSwitcher extends LinearLayout implements View.OnTouchListener {
    private GestureDetector mGesture = null;
    private Context context;
    private long duration = 200L;
    private final static String BLANKTAG = "blankTag";
    private int childCount;
    private final static int SHOWNUM = 3;
    private View cloneView;
    // represents the first position of all children view except clone view
    private ArrayList<Integer> grids;
    private int fullSize;
    private int layoutId;
    private boolean isCyclic = true;
    private SwitherListener switherListener;

    public enum Direction {PREVIOUS, NEXT}

    public MultiPartSwitcher(Context context) {
        this(context, null);
    }

    public MultiPartSwitcher(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiPartSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    public void setSwitherListener(SwitherListener switherListener) {
        this.switherListener = switherListener;
    }

    private void initView() {
        setOnTouchListener(this);
        mGesture = new GestureDetector(context, new MyOnGestureListener());
    }

    public void setItemLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    public View addOne() {
        View view = LayoutInflater.from(context).inflate(layoutId, this, false);
        addView(view);
        return view;
    }

    public View getOne(int index) {
        return getChildAt(index);
    }

    public void enableScroll(boolean enable) {
        setOnTouchListener(enable ? this : null);
    }

    public void setIsCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;
    }

    private void initChildren() {
        if (getChildCount() < SHOWNUM) {
            if (getChildCount() == 1) {
                View blankView = LayoutInflater.from(context).inflate(layoutId, this, false);
                blankView.setTag(BLANKTAG);
                addView(blankView);
            }
            for (int i = 0; i < SHOWNUM - getChildCount(); i++) {
                View blankView = LayoutInflater.from(context).inflate(layoutId, this, false);
                blankView.setTag(BLANKTAG);
                addView(blankView, 0);
            }
        }
        childCount = getChildCount();
        grids = new ArrayList<Integer>();
        fullSize = getOrientation() == HORIZONTAL ? getWidth() : getHeight();
        for (int i = 0; i < childCount; i++) {
            grids.add(fullSize * i / SHOWNUM);
            View childView = getChildAt(i);
            LayoutParams params = (LayoutParams) childView.getLayoutParams();
            params.weight = 0;
            if (getOrientation() == HORIZONTAL) {
                params.width = fullSize / SHOWNUM;
            } else {
                params.height = fullSize / SHOWNUM;
            }
            childView.setLayoutParams(params);
        }
        if (childCount == SHOWNUM) {
            cloneView = LayoutInflater.from(context).inflate(layoutId, this, false);
            addView(cloneView, getChildAt(0).getLayoutParams());
        }
        grids.trimToSize();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (childCount == 0 && getChildCount() != 0) {
            initChildren();
        }
    }

    private void moveView(final View view, float from, float to) {
        ValueAnimator anim = ValueAnimator.ofFloat(from, to);
        final int orientation = getOrientation();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float val = (Float) valueAnimator.getAnimatedValue();
                if (orientation == LinearLayout.HORIZONTAL) {
                    view.setTranslationX(val);
                } else {
                    view.setTranslationY(val);
                }
            }
        });
        anim.setDuration(duration);
        anim.start();
    }

    public long getDuration() {
        return duration;
    }

    private int getNonBlankChildCount() {
        int number = 0;
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (!BLANKTAG.equals(childView.getTag())) {
                number++;
            }
        }
        return number;
    }

    private View getNonBlankFirstChildView() {
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (!BLANKTAG.equals(childView.getTag()))
                return childView;
        }
        return getChildAt(0);
    }

    private View getNonBlankLastChildView() {
        for (int i = childCount - 1; i >= 0; i--) {
            View childView = getChildAt(i);
            if (!BLANKTAG.equals(childView.getTag()))
                return childView;
        }
        return getChildAt(childCount - 1);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGesture.onTouchEvent(event);
    }

    class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        /**
         * When SHOWNUM = 3
         * View structure like "1 2 3 clone" or "1 2 3 4 ...", only 1 2 3 are in the bounds
         * 1 or/and 3 may be blank view, doesn't matter
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float velocity = getOrientation() == HORIZONTAL ? velocityX : velocityY;
            float velocity2 = getOrientation() == HORIZONTAL ? velocityY : velocityX;
            if (Math.abs(velocity) < Math.abs(velocity2)) {
                return super.onFling(e1, e2, velocityX, velocityY);
            }
            int oneGrid = grids.get(1);
            if (!isCyclic) {
                if (getNonBlankChildCount() < SHOWNUM) {
                    if (velocity > 0 && getNonBlankLastChildView().getX() == fullSize - oneGrid) {
                        return super.onFling(e1, e2, velocityX, velocityY);
                    } else if (velocity < 0 && getNonBlankFirstChildView().getX() == 0) {
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                } else {
                    if (velocity > 0 && getChildAt(0).getX() == 0) {
                        return super.onFling(e1, e2, velocityX, velocityY);
                    } else if (velocity < 0 && getChildAt(childCount - 1).getX() == fullSize - oneGrid) {
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                }
            }
            int viewWaitToShowIndex = -1;
            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                int childPosition = getOrientation() == HORIZONTAL ? (int) childView.getX() : (int) childView.getY();
                int showPosition = grids.indexOf(childPosition);
                if (showPosition < 0 || showPosition >= SHOWNUM) {
                    continue;
                }
                float translation = getOrientation() == HORIZONTAL ? childView.getTranslationX() : childView.getTranslationY();
                /** more than {@link SHOWNUM} child views in list, then need to find viewWaitToShow */
                if (cloneView == null) {
                    if (velocity > 0 && showPosition == 0) {
                        if (i - 1 < 0) {
                            viewWaitToShowIndex = childCount - 1;
                        } else {
                            viewWaitToShowIndex = i - 1;
                        }
                    } else if (velocity < 0 && showPosition == SHOWNUM - 1) {
                        if (i + 1 >= childCount) {
                            viewWaitToShowIndex = 0;
                        } else {
                            viewWaitToShowIndex = i + 1;
                        }
                    }
                }
                if (velocity < 0) {
                    if (showPosition == 0 && cloneView != null) {/**only {@link SHOWNUM} children*/
                        if (switherListener != null) {
                            switherListener.cloneView(cloneView, childView);
                        }
                        moveView(cloneView, -fullSize, -fullSize - oneGrid);
                        moveView(childView, fullSize - grids.get(i), fullSize - oneGrid - grids.get(i));
                    } else {
                        moveView(childView, translation, translation - oneGrid);
                    }
                    if (switherListener != null) {
                        switherListener.onStartScrollView(childView, showPosition, Direction.PREVIOUS);
                    }
                } else if (velocity > 0) {
                    if (showPosition == SHOWNUM - 1 && cloneView != null) {/**only {@link SHOWNUM} children*/
                        if (switherListener != null) {
                            switherListener.cloneView(cloneView, childView);
                        }
                        moveView(cloneView, -oneGrid, 0);
                        moveView(childView, -oneGrid - grids.get(i), -grids.get(i));
                    } else {
                        moveView(childView, translation, translation + oneGrid);
                    }
                    if (switherListener != null) {
                        switherListener.onStartScrollView(childView, showPosition, Direction.NEXT);
                    }
                }
            }
            View viewWaitToShow = getChildAt(viewWaitToShowIndex);
            if (viewWaitToShow != null) {
                if (velocity < 0) {
                    moveView(viewWaitToShow, fullSize - grids.get(viewWaitToShowIndex), fullSize - oneGrid - grids.get(viewWaitToShowIndex));
                    if (switherListener != null) {
                        switherListener.onStartScrollView(viewWaitToShow, SHOWNUM, Direction.PREVIOUS);
                    }
                } else if (velocity > 0) {
                    moveView(viewWaitToShow, -oneGrid - grids.get(viewWaitToShowIndex), -grids.get(viewWaitToShowIndex));
                    if (switherListener != null) {
                        switherListener.onStartScrollView(viewWaitToShow, -1, Direction.NEXT);
                    }
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    public interface SwitherListener {
        void cloneView(View cloneView, View originalView);

        void onStartScrollView(View viewToScroll, int curPosition, Direction direction);
    }
}

package com.example.kaizhang.kaiapplication.layout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.kaizhang.kaiapplication.R;
import com.example.kaizhang.kaiapplication.controller.DragController;
import com.example.kaizhang.kaiapplication.utils.Utilities;
import com.example.kaizhang.kaiapplication.view.DragView;

/**
 * Created by kai zhang on 2016/1/5.
 */
public class DragLayer extends FrameLayout {
    private DragController mDragController;
    private int[] mTmpXY = new int[2];
    private Context mContext;
    public int width, height;
    public int standardMargin;
    public int functionSize;
    public int startH;

    public static final int ANIMATION_END_DISAPPEAR = 0;
    public static final int ANIMATION_END_FADE_OUT = 1;
    public static final int ANIMATION_END_REMAIN_VISIBLE = 2;

    // Variables relating to animation of views after drop
    private ValueAnimator mDropAnim = null;
    private ValueAnimator mFadeOutAnim = null;
    private TimeInterpolator mCubicEaseOutInterpolator = new DecelerateInterpolator(1.5f);
    private DragView mDropView = null;
    private int mAnchorViewInitialScrollX = 0;
    private View mAnchorView = null;

    public DragLayer(Context context) {
        this(context, null);
    }

    public DragLayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setDragController(DragController dragController) {
        this.mDragController = dragController;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        standardMargin = (int) Utilities.getDp(mContext, 10);
        width = mContext.getResources().getDisplayMetrics().widthPixels;
        height = mContext.getResources().getDisplayMetrics().heightPixels;
        functionSize = (width - standardMargin * 8) / 4;
        startH = height - Utilities.getStatusBarHeight(mContext) - (2 * functionSize + 3 * standardMargin);
    }

    /**
     * Determine the rect of the descendant in this DragLayer's coordinates
     *
     * @param descendant The descendant whose coordinates we want to find.
     * @param r          The rect into which to place the results.
     * @return The factor by which this descendant is scaled relative to this DragLayer.
     */
    public float getDescendantRectRelativeToSelf(View descendant, Rect r) {
        mTmpXY[0] = 0;
        mTmpXY[1] = 0;
        float scale = getDescendantCoordRelativeToSelf(descendant, mTmpXY);

        r.set(mTmpXY[0], mTmpXY[1],
                (int) (mTmpXY[0] + scale * descendant.getMeasuredWidth()),
                (int) (mTmpXY[1] + scale * descendant.getMeasuredHeight()));
        return scale;
    }

    public float getLocationInDragLayer(View child, int[] loc) {
        loc[0] = 0;
        loc[1] = 0;
        return getDescendantCoordRelativeToSelf(child, loc);
    }

    public float getDescendantCoordRelativeToSelf(View descendant, int[] coord) {
        return getDescendantCoordRelativeToSelf(descendant, coord, false);
    }

    /**
     * Given a coordinate relative to the descendant, find the coordinate in this DragLayer's
     * coordinates.
     *
     * @param descendant        The descendant to which the passed coordinate is relative.
     * @param coord             The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the root descendant:
     *                          sometimes this is relevant as in a child's coordinates within the root descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     * this scale factor is assumed to be equal in X and Y, and so if at any point this
     * assumption fails, we will need to return a pair of scale factors.
     */
    public float getDescendantCoordRelativeToSelf(View descendant, int[] coord,
                                                  boolean includeRootScroll) {
        return Utilities.getDescendantCoordRelativeToParent(descendant, this,
                coord, includeRootScroll);
    }

    /**
     * Inverse of {@link #getDescendantCoordRelativeToSelf(View, int[])}.
     */
    public float mapCoordInSelfToDescendent(View descendant, int[] coord) {
        return Utilities.mapCoordInSelfToDescendent(descendant, this, coord);
    }

    public void getViewRectRelativeToSelf(View v, Rect r) {
        int[] loc = new int[2];
        getLocationInWindow(loc);
        int x = loc[0];
        int y = loc[1];

        v.getLocationInWindow(loc);
        int vX = loc[0];
        int vY = loc[1];

        int left = vX - x;
        int top = vY - y;
        r.set(left, top, left + v.getMeasuredWidth(), top + v.getMeasuredHeight());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragController.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragController.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDragController.onTouchEvent(event);
    }

    public void animateViewIntoPosition(DragView dragView, final View child) {
        animateViewIntoPosition(dragView, child, null, null);
    }

    public void animateViewIntoPosition(DragView dragView, final int[] pos, float alpha,
                                        float scaleX, float scaleY, int animationEndStyle, Runnable onFinishRunnable,
                                        int duration) {
        Rect r = new Rect();
        getViewRectRelativeToSelf(dragView, r);
        final int fromX = r.left;
        final int fromY = r.top;

        animateViewIntoPosition(dragView, fromX, fromY, pos[0], pos[1], alpha, 1, 1, scaleX, scaleY,
                onFinishRunnable, animationEndStyle, duration, null);
    }

    public void animateViewIntoPosition(DragView dragView, final View child,
                                        final Runnable onFinishAnimationRunnable, View anchorView) {
        animateViewIntoPosition(dragView, child, -1, onFinishAnimationRunnable, anchorView);
    }

    public void animateViewIntoPosition(DragView dragView, final View child, final View targetChild, int duration,
                                        final Runnable onFinishAnimationRunnable, View anchorView) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
        Rect r = new Rect();
        getViewRectRelativeToSelf(dragView, r);

        int coord[] = new int[2];
        float childScale = child.getScaleX();
        coord[0] = lp.leftMargin + (int) (child.getMeasuredWidth() * (1 - childScale) / 2);
        coord[1] = lp.topMargin + (int) (child.getMeasuredHeight() * (1 - childScale) / 2);

        // Since the child hasn't necessarily been laid out, we force the lp to be updated with
        // the correct coordinates (above) and use these to determine the final location
        float scale = getDescendantCoordRelativeToSelf((View) child.getParent(), coord);
        // We need to account for the scale of the child itself, as the above only accounts for
        // for the scale in parents.
        scale *= childScale;
        int toX = coord[0];
        int toY = coord[1];
        float toScale = scale;
        if (child instanceof TextView) {
            TextView tv = (TextView) child;
            // Account for the source scale of the icon (ie. from AllApps to Workspace, in which
            // the workspace may have smaller icon bounds).
            toScale = scale / dragView.getIntrinsicIconScaleFactor();

            // The child may be scaled (always about the center of the view) so to account for it,
            // we have to offset the position by the scaled size.  Once we do that, we can center
            // the drag view about the scaled child view.
            toY += Math.round(toScale * tv.getPaddingTop());
            toY -= dragView.getMeasuredHeight() * (1 - toScale) / 2;
            if (dragView.getDragVisualizeOffset() != null) {
                toY -= Math.round(toScale * dragView.getDragVisualizeOffset().y);
            }

            toX -= (dragView.getMeasuredWidth() - Math.round(scale * child.getMeasuredWidth())) / 2;
        }

        int fromX = r.left;
        int fromY = r.top;
        targetChild.setVisibility(INVISIBLE);
        Runnable onCompleteRunnable = new Runnable() {
            public void run() {
                targetChild.setVisibility(VISIBLE);
                if (onFinishAnimationRunnable != null) {
                    onFinishAnimationRunnable.run();
                }
            }
        };
        fromX -= ((FrameLayout.LayoutParams) child.getLayoutParams()).leftMargin;
        fromY -= ((FrameLayout.LayoutParams) child.getLayoutParams()).topMargin;
        toX -= ((FrameLayout.LayoutParams) child.getLayoutParams()).leftMargin;
        toY -= ((FrameLayout.LayoutParams) child.getLayoutParams()).topMargin;

        toX += ((FrameLayout.LayoutParams) targetChild.getLayoutParams()).leftMargin - ((FrameLayout.LayoutParams) child.getLayoutParams()).leftMargin;
        toY += ((FrameLayout.LayoutParams) targetChild.getLayoutParams()).topMargin - ((FrameLayout.LayoutParams) child.getLayoutParams()).topMargin;
        if (child.getParent().getParent() instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) child.getParent().getParent();
            int scrollY = scrollView.getScrollY();
            fromY += scrollY;
            toY += scrollY;
            if (targetChild != child) {
                toY += scrollY;
            }
        }
        if (targetChild != child && targetChild.getParent().getParent() instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) targetChild.getParent().getParent();
            int scrollY = scrollView.getScrollY();
            toY -= scrollY;
        }
        animateViewIntoPosition(dragView, fromX, fromY, toX, toY, 1, 1, 1, toScale, toScale,
                onCompleteRunnable, ANIMATION_END_DISAPPEAR, duration, anchorView);
    }

    public void animateViewIntoPosition(DragView dragView, final View child, int duration,
                                        final Runnable onFinishAnimationRunnable, View anchorView) {
        animateViewIntoPosition(dragView, child, child, duration, onFinishAnimationRunnable, anchorView);
    }

    public void animateViewIntoPosition(final DragView view, final int fromX, final int fromY,
                                        final int toX, final int toY, float finalAlpha, float initScaleX, float initScaleY,
                                        float finalScaleX, float finalScaleY, Runnable onCompleteRunnable,
                                        int animationEndStyle, int duration, View anchorView) {
        Rect from = new Rect(fromX, fromY, fromX +
                view.getMeasuredWidth(), fromY + view.getMeasuredHeight());
        Rect to = new Rect(toX, toY, toX + view.getMeasuredWidth(), toY + view.getMeasuredHeight());
        animateView(view, from, to, finalAlpha, initScaleX, initScaleY, finalScaleX, finalScaleY, duration,
                null, null, onCompleteRunnable, animationEndStyle, anchorView);
    }

    /**
     * This method animates a view at the end of a drag and drop animation.
     *
     * @param view               The view to be animated. This view is drawn directly into DragLayer, and so
     *                           doesn't need to be a child of DragLayer.
     * @param from               The initial location of the view. Only the left and top parameters are used.
     * @param to                 The final location of the view. Only the left and top parameters are used. This
     *                           location doesn't account for scaling, and so should be centered about the desired
     *                           final location (including scaling).
     * @param finalAlpha         The final alpha of the view, in case we want it to fade as it animates.
     *                           finalScale The final scale of the view. The view is scaled about its center.
     * @param duration           The duration of the animation.
     * @param motionInterpolator The interpolator to use for the location of the view.
     * @param alphaInterpolator  The interpolator to use for the alpha of the view.
     * @param onCompleteRunnable Optional runnable to run on animation completion.
     *                           fadeOut Whether or not to fade out the view once the animation completes. If true,
     *                           the runnable will execute after the view is faded out.
     * @param anchorView         If not null, this represents the view which the animated view stays
     *                           anchored to in case scrolling is currently taking place. Note: currently this is
     *                           only used for the X dimension for the case of the workspace.
     */
    public void animateView(final DragView view, final Rect from, final Rect to,
                            final float finalAlpha, final float initScaleX, final float initScaleY,
                            final float finalScaleX, final float finalScaleY, int duration,
                            final Interpolator motionInterpolator, final Interpolator alphaInterpolator,
                            final Runnable onCompleteRunnable, final int animationEndStyle, View anchorView) {

        // Calculate the duration of the animation based on the object's distance
        final float dist = (float) Math.sqrt(Math.pow(to.left - from.left, 2) +
                Math.pow(to.top - from.top, 2));
        final Resources res = getResources();
        final float maxDist = (float) res.getInteger(R.integer.config_dropAnimMaxDist);

        // If duration < 0, this is a cue to compute the duration based on the distance
        if (duration < 0) {
            duration = res.getInteger(R.integer.config_dropAnimMaxDuration);
            if (dist < maxDist) {
                duration *= mCubicEaseOutInterpolator.getInterpolation(dist / maxDist);
            }
            duration = Math.max(duration, res.getInteger(R.integer.config_dropAnimMinDuration));
        }

        // Fall back to cubic ease out interpolator for the animation if none is specified
        TimeInterpolator interpolator = null;
        if (alphaInterpolator == null || motionInterpolator == null) {
            interpolator = mCubicEaseOutInterpolator;
        }

        // Animate the view
        final float initAlpha = view.getAlpha();
        final float dropViewScale = view.getScaleX();
        ValueAnimator.AnimatorUpdateListener updateCb = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float percent = (Float) animation.getAnimatedValue();
                final int width = view.getMeasuredWidth();
                final int height = view.getMeasuredHeight();

                float alphaPercent = alphaInterpolator == null ? percent :
                        alphaInterpolator.getInterpolation(percent);
                float motionPercent = motionInterpolator == null ? percent :
                        motionInterpolator.getInterpolation(percent);

                float initialScaleX = initScaleX * dropViewScale;
                float initialScaleY = initScaleY * dropViewScale;
                float scaleX = finalScaleX * percent + initialScaleX * (1 - percent);
                float scaleY = finalScaleY * percent + initialScaleY * (1 - percent);
                float alpha = finalAlpha * alphaPercent + initAlpha * (1 - alphaPercent);

                float fromLeft = from.left + (initialScaleX - 1f) * width / 2;
                float fromTop = from.top + (initialScaleY - 1f) * height / 2;

                int x = (int) (fromLeft + Math.round(((to.left - fromLeft) * motionPercent)));
                int y = (int) (fromTop + Math.round(((to.top - fromTop) * motionPercent)));

                int anchorAdjust = mAnchorView == null ? 0 : (int) (mAnchorView.getScaleX() *
                        (mAnchorViewInitialScrollX - mAnchorView.getScrollX()));

                int xPos = x - mDropView.getScrollX() + anchorAdjust;
                int yPos = y - mDropView.getScrollY();

                mDropView.setTranslationX(xPos);
                mDropView.setTranslationY(yPos);
                mDropView.setScaleX(scaleX);
                mDropView.setScaleY(scaleY);
                mDropView.setAlpha(alpha);
            }
        };
        animateView(view, updateCb, duration, interpolator, onCompleteRunnable, animationEndStyle,
                anchorView);
    }

    public void animateView(final DragView view, ValueAnimator.AnimatorUpdateListener updateCb, int duration,
                            TimeInterpolator interpolator, final Runnable onCompleteRunnable,
                            final int animationEndStyle, View anchorView) {
        // Clean up the previous animations
        if (mDropAnim != null) mDropAnim.cancel();
        if (mFadeOutAnim != null) mFadeOutAnim.cancel();

        // Show the drop view if it was previously hidden
        mDropView = view;
        mDropView.cancelAnimation();
        mDropView.resetLayoutParams();

        // Set the anchor view if the page is scrolling
        if (anchorView != null) {
            mAnchorViewInitialScrollX = anchorView.getScrollX();
        }
        mAnchorView = anchorView;

        // Create and start the animation
        mDropAnim = new ValueAnimator();
        mDropAnim.setInterpolator(interpolator);
        mDropAnim.setDuration(duration);
        mDropAnim.setFloatValues(0f, 1f);
        mDropAnim.addUpdateListener(updateCb);
        mDropAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }
                switch (animationEndStyle) {
                    case ANIMATION_END_DISAPPEAR:
                        clearAnimatedView();
                        break;
                    case ANIMATION_END_FADE_OUT:
                        fadeOutDragView();
                        break;
                    case ANIMATION_END_REMAIN_VISIBLE:
                        break;
                }
            }
        });
        mDropAnim.start();
    }

    public void clearAnimatedView() {
        if (mDropAnim != null) {
            mDropAnim.cancel();
        }
        if (mDropView != null) {
            mDragController.onDeferredEndDrag(mDropView);
        }
        mDropView = null;
        invalidate();
    }

    public View getAnimatedView() {
        return mDropView;
    }

    private void fadeOutDragView() {
        mFadeOutAnim = new ValueAnimator();
        mFadeOutAnim.setDuration(150);
        mFadeOutAnim.setFloatValues(0f, 1f);
        mFadeOutAnim.removeAllUpdateListeners();
        mFadeOutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                final float percent = (Float) animation.getAnimatedValue();

                float alpha = 1 - percent;
                mDropView.setAlpha(alpha);
            }
        });
        mFadeOutAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (mDropView != null) {
                    mDragController.onDeferredEndDrag(mDropView);
                }
                mDropView = null;
                invalidate();
            }
        });
        mFadeOutAnim.start();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) child.getLayoutParams();
            if (flp instanceof LayoutParams) {
                final LayoutParams lp = (LayoutParams) flp;
                if (lp.customPosition) {
                    child.layout(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height);
                }
            }
        }
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        public int x, y;
        public boolean customPosition = false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams lp) {
            super(lp);
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getWidth() {
            return width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getY() {
            return y;
        }
    }
}

/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.example.kaizhang.kaiapplication.controller;

import android.content.ComponentName;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.example.kaizhang.kaiapplication.R;
import com.example.kaizhang.kaiapplication.drag_interface.DragSource;
import com.example.kaizhang.kaiapplication.drag_interface.DropTarget;
import com.example.kaizhang.kaiapplication.layout.DragLayer;
import com.example.kaizhang.kaiapplication.utils.Utilities;
import com.example.kaizhang.kaiapplication.view.DragView;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Class for initiating a drag within a view or across multiple views.
 */
public class DragController {
    private static final String TAG = "Launcher.DragController";
    private DragLayer mDragLayer;
    /**
     * Indicates the drag is a move.
     */
    public static int DRAG_ACTION_MOVE = 0;

    /**
     * Indicates the drag is a copy.
     */
    public static int DRAG_ACTION_COPY = 1;
    private Handler mHandler;
    private static final float MAX_FLING_DEGREES = 35f;
    // temporaries to avoid gc thrash
    private Rect mRectTemp = new Rect();
    private final int[] mCoordinatesTemp = new int[2];
    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;
    /**
     * Whether or not we're dragging.
     */
    private boolean mDragging;

    /**
     * X coordinate of the down event.
     */
    private int mMotionDownX;

    /**
     * Y coordinate of the down event.
     */
    private int mMotionDownY;

    private DropTarget.DragObject mDragObject;

    /**
     * Who can receive drop events
     */
    private ArrayList<DropTarget> mDropTargets = new ArrayList<DropTarget>();
    private ArrayList<DragListener> mListeners = new ArrayList<DragListener>();
    private DropTarget mFlingToDeleteDropTarget;

    /**
     * The window token used as the parent for the DragView.
     */
    private IBinder mWindowToken;

    private View mMoveTarget;

    private DropTarget mLastDropTarget;

    private InputMethodManager mInputMethodManager;

    private int mLastTouch[] = new int[2];
    private long mLastTouchUpTime = -1;
    private int mDistanceSinceScroll = 0;

    private int mTmpPoint[] = new int[2];
    private Rect mDragLayerRect = new Rect();

    protected int mFlingToDeleteThresholdVelocity;
    private VelocityTracker mVelocityTracker;


    /**
     * Interface to receive notifications when a drag starts or stops
     */
    public interface DragListener {
        /**
         * A drag has begun
         *
         * @param source     An object representing where the drag originated
         * @param info       The data associated with the object that is being dragged
         * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE}
         *                   or {@link DragController#DRAG_ACTION_COPY}
         */
        void onDragStart(DragSource source, Object info, int dragAction);

        /**
         * The drag has ended
         */
        void onDragEnd();
    }

    /**
     * Used to create a new DragLayer from XML.
     */
    public DragController(DragLayer dragLayer) {
        Resources r = dragLayer.getResources();
        mDragLayer = dragLayer;
        mHandler = new Handler();
        mVelocityTracker = VelocityTracker.obtain();

        float density = r.getDisplayMetrics().density;
        mFlingToDeleteThresholdVelocity =
                (int) (r.getInteger(R.integer.config_flingToDeleteMinVelocity) * density);
    }

    public boolean dragging() {
        return mDragging;
    }


    public void startDrag(View v, DragSource dragSource, Object dragInfo) {
        mDragObject = new DropTarget.DragObject();
        mDragObject.dragComplete = false;
        mDragObject.dragSource = dragSource;
        mDragObject.dragInfo = dragInfo;
        mDragLayer.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        int scrollY = 0;
        if (v.getParent().getParent() instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) v.getParent().getParent();
            scrollY = scrollView.getScrollY();
        }
        int[] loc = mCoordinatesTemp;
        mDragLayer.getLocationInDragLayer(v, loc);
        Bitmap bitmap = Utilities.getBitmapFromView(v);
        mDragObject.dragView = new DragView(mDragLayer, bitmap, loc[0] + v.getWidth() / 2, loc[1] + v.getHeight() / 2, 0, 0, bitmap.getWidth(), bitmap.getHeight(), 1);
        mDragObject.dragView.setTag(System.currentTimeMillis());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(v.getLayoutParams());
        FrameLayout.LayoutParams originalLayoutParams = (FrameLayout.LayoutParams) v.getLayoutParams();
        layoutParams.setMargins(originalLayoutParams.leftMargin, originalLayoutParams.topMargin - scrollY, originalLayoutParams.rightMargin, originalLayoutParams.bottomMargin);
        v.setVisibility(View.INVISIBLE);
        mDragLayer.addView(mDragObject.dragView, layoutParams);
        mDragging = true;
        Log.i("zk", "startDrag");
    }

    /**
     * Draw the view into a bitmap.
     */
    Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        float alpha = v.getAlpha();
        v.setAlpha(1.0f);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setAlpha(alpha);
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    /**
     * Call this from a drag source view like this:
     * <p/>
     * <pre>
     *  @Override
     *  public boolean dispatchKeyEvent(KeyEvent event) {
     *      return mDragController.dispatchKeyEvent(this, event)
     *              || super.dispatchKeyEvent(event);
     * </pre>
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging;
    }

    public boolean isDragging() {
        return mDragging;
    }

    /**
     * Stop dragging without dropping.
     */
    public void cancelDrag() {
        if (mDragging) {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
            mDragObject.deferDragViewCleanupPostAnimation = false;
            mDragObject.cancelled = true;
            mDragObject.dragComplete = true;
            mDragObject.dragSource.onDropCompleted(null, mDragObject, false, false);
        }
        endDrag();
    }

    public void onAppsRemoved(final ArrayList<String> packageNames, HashSet<ComponentName> cns) {
        // Cancel the current drag if we are removing an app that we are dragging
        if (mDragObject != null) {
            Object rawDragInfo = mDragObject.dragInfo;
        }
    }

    private void endDrag() {
        if (mDragging) {
            mDragging = false;
            boolean isDeferred = false;
            if (mDragObject.dragView != null) {
                isDeferred = mDragObject.deferDragViewCleanupPostAnimation;
                if (!isDeferred) {
                    mDragObject.dragView.remove();
                }
                mDragObject.dragView = null;
            }

            // Only end the drag if we are not deferred
            if (!isDeferred) {
                for (DragListener listener : mListeners) {
                    listener.onDragEnd();
                }
            }
        }

        releaseVelocityTracker();
    }

    /**
     * This only gets called as a result of drag view cleanup being deferred in endDrag();
     */
    public void onDeferredEndDrag(DragView dragView) {
        dragView.remove();

        if (mDragObject.deferDragViewCleanupPostAnimation) {
            // If we skipped calling onDragEnd() before, do it now
            for (DragListener listener : mListeners) {
                listener.onDragEnd();
            }
        }
    }

    public void onDeferredEndFling(DropTarget.DragObject d) {
        d.dragSource.onFlingToDeleteCompleted();
    }

    /**
     * Clamps the position to the drag layer bounds.
     */
    private int[] getClampedDragLayerPos(float x, float y) {
        mDragLayer.getLocalVisibleRect(mDragLayerRect);
        mTmpPoint[0] = (int) Math.max(mDragLayerRect.left, Math.min(x, mDragLayerRect.right - 1));
        mTmpPoint[1] = (int) Math.max(mDragLayerRect.top, Math.min(y, mDragLayerRect.bottom - 1));
        return mTmpPoint;
    }

    long getLastGestureUpTime() {
        if (mDragging) {
            return System.currentTimeMillis();
        } else {
            return mLastTouchUpTime;
        }
    }

    void resetLastGestureUpTime() {
        mLastTouchUpTime = -1;
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean debug = false;

        // Update the velocity tracker
        acquireVelocityTrackerAndAddMovement(ev);

        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                Log.i("zk", "onInterceptTouchEvent move");
                break;
            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                Log.i("zk", "onInterceptTouchEvent down");
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                mLastDropTarget = null;
                break;
            case MotionEvent.ACTION_UP:
                Log.i("zk", "onInterceptTouchEvent up");
                mLastTouchUpTime = System.currentTimeMillis();
                if (mDragging) {
                    drop(dragLayerX, dragLayerY);
                } else {
                    //click

                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("zk", "onInterceptTouchEvent cancel");
                cancelDrag();
                break;
        }

        return mDragging;
    }

    /**
     * Sets the view that should handle move events.
     */
    void setMoveTarget(View view) {
        mMoveTarget = view;
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return mMoveTarget != null && mMoveTarget.dispatchUnhandledMove(focused, direction);
    }

    private void handleMoveEvent(int x, int y) {
        mDragObject.dragView.move(x, y);

        // Drop on someone?
        final int[] coordinates = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget(x, y, coordinates);
        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];
        checkTouchMove(dropTarget);

        // Check if we are hovering over the scroll areas
        mDistanceSinceScroll +=
                Math.sqrt(Math.pow(mLastTouch[0] - x, 2) + Math.pow(mLastTouch[1] - y, 2));
        mLastTouch[0] = x;
        mLastTouch[1] = y;
    }

    public void forceTouchMove() {
        int[] dummyCoordinates = mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget(mLastTouch[0], mLastTouch[1], dummyCoordinates);
        mDragObject.x = dummyCoordinates[0];
        mDragObject.y = dummyCoordinates[1];
        checkTouchMove(dropTarget);
    }

    private void checkTouchMove(DropTarget dropTarget) {
        if (dropTarget != null) {
            if (mLastDropTarget != dropTarget) {
                if (mLastDropTarget != null) {
                    mLastDropTarget.onDragExit(mDragObject);
                }
                dropTarget.onDragEnter(mDragObject);
            }
            dropTarget.onDragOver(mDragObject);
        } else {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
        }
        mLastDropTarget = dropTarget;
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging) {
            return false;
        }

        // Update the velocity tracker
        acquireVelocityTrackerAndAddMovement(ev);

        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.i("zk", "onTouchEvent down");
                // Remember where the motion event started
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                handleMoveEvent(dragLayerX, dragLayerY);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("zk", "onTouchEvent move");
                handleMoveEvent(dragLayerX, dragLayerY);
                break;
            case MotionEvent.ACTION_UP:
                Log.i("zk", "onTouchEvent up");
                // Ensure that we've processed a move event at the current pointer location.
                handleMoveEvent(dragLayerX, dragLayerY);

                if (mDragging) {
                    drop(dragLayerX, dragLayerY);
                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("zk", "onTouchEvent cancel");
                cancelDrag();
                break;
        }

        return true;
    }

    /**
     * Determines whether the user flung the current item to delete it.
     *
     * @return the vector at which the item was flung, or null if no fling was detected.
     */
    private PointF isFlingingToDelete(DragSource source) {
        if (mFlingToDeleteDropTarget == null) return null;
        if (!source.supportsFlingToDelete()) return null;

        ViewConfiguration config = ViewConfiguration.get(mDragLayer.getContext());
        mVelocityTracker.computeCurrentVelocity(1000, config.getScaledMaximumFlingVelocity());

        if (mVelocityTracker.getYVelocity() < mFlingToDeleteThresholdVelocity) {
            // Do a quick dot product test to ensure that we are flinging upwards
            PointF vel = new PointF(mVelocityTracker.getXVelocity(),
                    mVelocityTracker.getYVelocity());
            PointF upVec = new PointF(0f, -1f);
            float theta = (float) Math.acos(((vel.x * upVec.x) + (vel.y * upVec.y)) /
                    (vel.length() * upVec.length()));
            if (theta <= Math.toRadians(MAX_FLING_DEGREES)) {
                return vel;
            }
        }
        return null;
    }

    private void dropOnFlingToDeleteTarget(float x, float y, PointF vel) {
        final int[] coordinates = mCoordinatesTemp;

        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];

        // Clean up dragging on the target if it's not the current fling delete target otherwise,
        // start dragging to it.
        if (mLastDropTarget != null && mFlingToDeleteDropTarget != mLastDropTarget) {
            mLastDropTarget.onDragExit(mDragObject);
        }

        // Drop onto the fling-to-delete target
        boolean accepted = false;
        mFlingToDeleteDropTarget.onDragEnter(mDragObject);
        // We must set dragComplete to true _only_ after we "enter" the fling-to-delete target for
        // "drop"
        mDragObject.dragComplete = true;
        mFlingToDeleteDropTarget.onDragExit(mDragObject);
        if (mFlingToDeleteDropTarget.acceptDrop(mDragObject)) {
            mFlingToDeleteDropTarget.onFlingToDelete(mDragObject, mDragObject.x, mDragObject.y,
                    vel);
            accepted = true;
        }
        mDragObject.dragSource.onDropCompleted((View) mFlingToDeleteDropTarget, mDragObject, true,
                accepted);
    }

    private void drop(float x, float y) {
        final int[] coordinates = mCoordinatesTemp;
        final DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);

        mDragObject.x = coordinates[0];
        mDragObject.y = coordinates[1];
        boolean accepted = false;
        if (dropTarget != null) {
            mDragObject.dragComplete = true;
            dropTarget.onDragExit(mDragObject);
            if (dropTarget.acceptDrop(mDragObject)) {
                dropTarget.onDrop(mDragObject);
                accepted = true;
            }
        }
        mDragObject.dragSource.onDropCompleted((View) dropTarget, mDragObject, false, accepted);
    }

    public void quickMove(View v, DragSource dragSource, DropTarget dropTarget, Object dragInfo) {
        mDragObject = new DropTarget.DragObject();
        mDragObject.dragSource = dragSource;
        mDragObject.dragInfo = dragInfo;
        int scrollY = 0;
        if (v.getParent().getParent() instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) v.getParent().getParent();
            scrollY = scrollView.getScrollY();
        }
        int[] loc = mCoordinatesTemp;
        mDragLayer.getLocationInDragLayer(v, loc);
        Bitmap bitmap = Utilities.getBitmapFromView(v);
        mDragObject.dragView = new DragView(mDragLayer, bitmap, loc[0] + v.getWidth() / 2, loc[1] + v.getHeight() / 2, 0, 0, bitmap.getWidth(), bitmap.getHeight(), 1);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(v.getLayoutParams());
        FrameLayout.LayoutParams originalLayoutParams = (FrameLayout.LayoutParams) v.getLayoutParams();
        layoutParams.setMargins(originalLayoutParams.leftMargin, originalLayoutParams.topMargin - scrollY, originalLayoutParams.rightMargin, originalLayoutParams.bottomMargin);
        v.setVisibility(View.INVISIBLE);
        mDragLayer.addView(mDragObject.dragView, layoutParams);
        boolean accepted = false;
        if (dropTarget.acceptDrop(mDragObject)) {
            dropTarget.onDrop(mDragObject);
            accepted = true;
        }
        dragSource.onDropCompleted((View) dropTarget, mDragObject, false, accepted);
    }

    private DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        final Rect r = mRectTemp;

        final ArrayList<DropTarget> dropTargets = mDropTargets;
        final int count = dropTargets.size();
        for (int i = count - 1; i >= 0; i--) {
            DropTarget target = dropTargets.get(i);
            if (!target.isDropEnabled())
                continue;

            target.getHitRectRelativeToDragLayer(r);

            mDragObject.x = x;
            mDragObject.y = y;
            if (r.contains(x, y)) {

                dropCoordinates[0] = x;
                dropCoordinates[1] = y;
                mDragLayer.mapCoordInSelfToDescendent((View) target, dropCoordinates);

                return target;
            }
        }
        return null;
    }

    public void setWindowToken(IBinder token) {
        mWindowToken = token;
    }

    /**
     * Sets the drag listner which will be notified when a drag starts or ends.
     */
    public void addDragListener(DragListener l) {
        if (!mListeners.contains(l)) {
            mListeners.add(l);
        }
    }

    /**
     * Remove a previously installed drag listener.
     */
    public void removeDragListener(DragListener l) {
        mListeners.remove(l);
    }

    /**
     * Add a DropTarget to the list of potential places to receive drop events.
     */
    public void addDropTarget(DropTarget target) {
        if (!mDropTargets.contains(target)) {
            mDropTargets.add(target);
        }
    }

    /**
     * Don't send drop events to <em>target</em> any more.
     */
    public void removeDropTarget(DropTarget target) {
        mDropTargets.remove(target);
    }

    /**
     * Sets the current fling-to-delete drop target.
     */
    public void setFlingToDeleteDropTarget(DropTarget target) {
        mFlingToDeleteDropTarget = target;
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    DragView getDragView() {
        return mDragObject.dragView;
    }

}

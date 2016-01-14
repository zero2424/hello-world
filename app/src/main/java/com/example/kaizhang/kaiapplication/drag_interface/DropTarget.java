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

package com.example.kaizhang.kaiapplication.drag_interface;

import android.graphics.PointF;
import android.graphics.Rect;

import com.example.kaizhang.kaiapplication.view.DragView;

/**
 * Interface defining an object that can receive a drag.
 */
public interface DropTarget {

    public static final String TAG = "DropTarget";

    class DragObject {
        public int x = -1;
        public int y = -1;

        /**
         * X offset from the upper-left corner of the cell to where we touched.
         */
        public int xOffset = -1;

        /**
         * Y offset from the upper-left corner of the cell to where we touched.
         */
        public int yOffset = -1;

        /**
         * This indicates whether a drag is in final stages, either drop or cancel. It
         * differentiates onDragExit, since this is called when the drag is ending, above
         * the current drag target, or when the drag moves off the current drag object.
         */
        public boolean dragComplete = false;

        /**
         * The view that moves around while you drag.
         */
        public DragView dragView = null;

        /**
         * The data associated with the object being dragged
         */
        public Object dragInfo = null;

        /**
         * Where the drag originated
         */
        public DragSource dragSource = null;

        /**
         * Post drag animation runnable
         */
        public Runnable postAnimationRunnable = null;

        /**
         * Indicates that the drag operation was cancelled
         */
        public boolean cancelled = false;

        /**
         * Defers removing the DragView from the DragLayer until after the drop animation.
         */
        public boolean deferDragViewCleanupPostAnimation = true;

        public DragObject() {
        }
    }

    /**
     * Used to temporarily disable certain drop targets
     *
     * @return boolean specifying whether this drop target is currently enabled
     */
    boolean isDropEnabled();

    /**
     * Handle an object being dropped on the DropTarget
     */
    void onDrop(DragObject dragObject);

    void onClick(DragObject dragObject);

    void onDragEnter(DragObject dragObject);

    void onDragOver(DragObject dragObject);

    void onDragExit(DragObject dragObject);

    /**
     * Handle an object being dropped as a result of flinging to delete and will be called in place
     * of onDrop().  (This is only called on objects that are set as the DragController's
     * fling-to-delete target.
     */
    void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec);

    /**
     * Check if a drop action can occur at, or near, the requested location.
     * This will be called just before onDrop.
     *
     * @return True if the drop will be accepted, false otherwise.
     */
    boolean acceptDrop(DragObject dragObject);

    // These methods are implemented in Views
    void getHitRectRelativeToDragLayer(Rect outRect);

    void getLocationInDragLayer(int[] loc);

    int getLeft();

    int getTop();
}

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

package com.example.kaizhang.kaiapplication.modle;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.kaizhang.kaiapplication.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Represents an item in the launcher.
 */
public class ItemInfo {

    /**
     * Intent extra to store the profile. Format: UserHandle
     */
    static final String EXTRA_PROFILE = "profile";

    static final int NO_ID = -1;

    /**
     * The id in the settings database for this item
     */
    public long id = NO_ID;

    /**
     * One of { LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
     * { LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
     * { LauncherSettings.Favorites#ITEM_TYPE_FOLDER}, or
     * { LauncherSettings.Favorites#ITEM_TYPE_APPWIDGET}.
     */
    public int itemType;

    /**
     * The id of the container that holds this item. For the desktop, this will be
     * { LauncherSettings.Favorites#CONTAINER_DESKTOP}. For the all applications folder it
     * will be {@link #NO_ID} (since it is not stored in the settings DB). For user folders
     * it will be the id of the folder.
     */
    public long container = NO_ID;

    /**
     * Iindicates the screen in which the shortcut appears.
     */
    public long screenId = -1;

    /**
     * Indicates the X position of the associated cell.
     */
    public int cellX = -1;

    /**
     * Indicates the Y position of the associated cell.
     */
    public int cellY = -1;

    /**
     * Indicates the X cell span.
     */
    public int spanX = 1;

    /**
     * Indicates the Y cell span.
     */
    int spanY = 1;

    /**
     * Indicates the minimum X cell span.
     */
    public int minSpanX = 1;

    /**
     * Indicates the minimum Y cell span.
     */
    public int minSpanY = 1;

    /**
     * Indicates that this item needs to be updated in the db
     */
    public boolean requiresDbUpdate = false;

    /**
     * Title of the item
     */
    CharSequence title;

    /**
     * Content description of the item.
     */
    CharSequence contentDescription;

    /**
     * The position of the item in a drag-and-drop operation.
     */
    int[] dropPos = null;

    ItemInfo() {
    }

    ItemInfo(ItemInfo info) {
        copyFrom(info);
    }

    public void copyFrom(ItemInfo info) {
        id = info.id;
        cellX = info.cellX;
        cellY = info.cellY;
        spanX = info.spanX;
        spanY = info.spanY;
        screenId = info.screenId;
        itemType = info.itemType;
        container = info.container;
        contentDescription = info.contentDescription;
    }

    void updateValuesWithCoordinates(ContentValues values, int cellX, int cellY) {
        values.put(Constants.CELLX, cellX);
        values.put(Constants.CELLY, cellY);
    }

    static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w("Favorite", "Could not write icon");
            return null;
        }
    }

    static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = flattenBitmap(bitmap);
            values.put(Constants.ICON, data);
        }
    }

    /**
     * It is very important that sub-classes implement this if they contain any references
     * to the activity (anything in the view hierarchy etc.). If not, leaks can result since
     * ItemInfo objects persist across rotation and can hence leak by holding stale references
     * to the old view hierarchy / activity.
     */
    void unbind() {
    }

    @Override
    public String toString() {
        return "Item(id=" + this.id + " type=" + this.itemType + " container=" + this.container
                + " screen=" + screenId + " cellX=" + cellX + " cellY=" + cellY + " spanX=" + spanX
                + " spanY=" + spanY + " dropPos=" + Arrays.toString(dropPos)
                + ")";
    }
}

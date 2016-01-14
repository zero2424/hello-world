package com.example.kaizhang.kaiapplication.utils;

import android.content.Context;

import com.example.kaizhang.kaiapplication.database.DatabaseHelper;

/**
 * Created by kai zhang on 2016/1/12.
 */
public class DatabaseSingleInstance {
    private DatabaseHelper mDatabaseHelper;

    private static class DatabaseSingleInstanceInner {
        static DatabaseSingleInstance inner = new DatabaseSingleInstance();
    }

    private DatabaseSingleInstance() {
    }

    public static DatabaseSingleInstance getInstance() {
        return DatabaseSingleInstanceInner.inner;
    }

    public synchronized DatabaseHelper getDatabaseHelper(Context context) {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = new DatabaseHelper(context);
        }
        return mDatabaseHelper;
    }
}

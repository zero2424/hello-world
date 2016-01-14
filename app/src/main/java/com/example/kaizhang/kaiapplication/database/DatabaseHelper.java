package com.example.kaizhang.kaiapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.kaizhang.kaiapplication.modle.FunctionInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kai zhang on 2016/1/11.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //类没有实例化,是不能用作父类构造器的参数,必须声明为静态

    private static final String name = "kai.db"; //数据库名称
    private static final String TABLE_FAVORITES = "favorites";

    private static final int version = 1; //数据库版本

    public DatabaseHelper(Context context) {

        //第三个参数CursorFactory指定在执行查询时获得一个游标实例的工厂类,设置为null,代表使用系统默认的工厂类

        super(context, name, null, version);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITES + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "isInHotseat INTEGER NOT NULL DEFAULT 0," +
                "functionType INTEGER NOT NULL DEFAULT 0," +
                "orderInHotseat INTEGER DEFAULT 0" +
                ");");
        insertDatas(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void insertDatas(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FunctionInfo.Favorites.FUNCTIONTYPE, FunctionInfo.Favorites.RemoteControl);
            contentValues.put(FunctionInfo.Favorites.TITLE, "远程启动");
            db.insert(TABLE_FAVORITES, null, contentValues);
            contentValues.put(FunctionInfo.Favorites.TITLE, "取消启动");
            db.insert(TABLE_FAVORITES, null, contentValues);
            contentValues.put(FunctionInfo.Favorites.TITLE, "远程锁门");
            db.insert(TABLE_FAVORITES, null, contentValues);
            contentValues.put(FunctionInfo.Favorites.TITLE, "闪灯鸣笛");
            db.insert(TABLE_FAVORITES, null, contentValues);

            contentValues.put(FunctionInfo.Favorites.FUNCTIONTYPE, FunctionInfo.Favorites.VehicleState);
            contentValues.put(FunctionInfo.Favorites.TITLE, "检测报告");
            db.insert(TABLE_FAVORITES, null, contentValues);
            contentValues.put(FunctionInfo.Favorites.TITLE, "经销商预约");
            db.insert(TABLE_FAVORITES, null, contentValues);

            contentValues.put(FunctionInfo.Favorites.FUNCTIONTYPE, FunctionInfo.Favorites.Navigation);
            contentValues.put(FunctionInfo.Favorites.TITLE, "实时路况");
            db.insert(TABLE_FAVORITES, null, contentValues);
            contentValues.put(FunctionInfo.Favorites.TITLE, "搜索兴趣点");
            db.insert(TABLE_FAVORITES, null, contentValues);
            contentValues.put(FunctionInfo.Favorites.TITLE, "电子围栏");
            db.insert(TABLE_FAVORITES, null, contentValues);
            contentValues.put(FunctionInfo.Favorites.TITLE, "一键回家");
            db.insert(TABLE_FAVORITES, null, contentValues);

            contentValues.put(FunctionInfo.Favorites.FUNCTIONTYPE, FunctionInfo.Favorites.Other);
            contentValues.put(FunctionInfo.Favorites.TITLE, "我的客服");
            db.insert(TABLE_FAVORITES, null, contentValues);
            contentValues.put(FunctionInfo.Favorites.TITLE, "电话云助手");
            db.insert(TABLE_FAVORITES, null, contentValues);
            contentValues.put(FunctionInfo.Favorites.TITLE, "超级管家");
            db.insert(TABLE_FAVORITES, null, contentValues);
            contentValues.put(FunctionInfo.Favorites.TITLE, "驾驶行为");
            db.insert(TABLE_FAVORITES, null, contentValues);
            contentValues.put(FunctionInfo.Favorites.TITLE, "测试用例1");
            db.insert(TABLE_FAVORITES, null, contentValues);
            contentValues.put(FunctionInfo.Favorites.TITLE, "测试用例2");
            db.insert(TABLE_FAVORITES, null, contentValues);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public int update(FunctionInfo functionInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FunctionInfo.Favorites.ID, functionInfo.getId());
        contentValues.put(FunctionInfo.Favorites.TITLE, functionInfo.getTitle());
        contentValues.put(FunctionInfo.Favorites.ISINHOTSEAT, functionInfo.isInHotseat());
        contentValues.put(FunctionInfo.Favorites.FUNCTIONTYPE, functionInfo.getFunctionType());
        contentValues.put(FunctionInfo.Favorites.ORDERINHOTSEAT, functionInfo.getOrderInHotseat());
        return db.update(TABLE_FAVORITES, contentValues, FunctionInfo.Favorites.ID + " =?", new String[]{functionInfo.getId() + ""});
    }

    public List<FunctionInfo> getAll() {
        List<FunctionInfo> functionInfos = new ArrayList<FunctionInfo>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        db.beginTransaction();
        try {
            c = getReadableDatabase().query(TABLE_FAVORITES, null, null, null, null, null, null);
            while (c != null && c.moveToNext()) {
                FunctionInfo functionInfo = new FunctionInfo(c.getLong(0), c.getString(1), c.getInt(2) != 0, c.getInt(3), c.getInt(4));
                functionInfos.add(functionInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            if (c != null) {
                c.close();
            }
        }
        return functionInfos;
    }
}

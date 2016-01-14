package com.example.kaizhang.kaiapplication;

import android.app.Application;
import android.content.pm.PackageManager;

import com.alipay.euler.andfix.patch.PatchManager;

import java.io.IOException;

/**
 * Created by kai zhang on 2016/1/13.
 */
public class MyApplication extends Application {
    PatchManager patchManager;

    @Override
    public void onCreate() {
        super.onCreate();
        patchManager = new PatchManager(this);
        try {
            patchManager.init(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        patchManager.loadPatch();
    }

    public void addPatch(String path){
        try {
            patchManager.addPatch(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

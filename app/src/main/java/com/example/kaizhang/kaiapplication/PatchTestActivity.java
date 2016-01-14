package com.example.kaizhang.kaiapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

/**
 * Created by kai zhang on 2016/1/13.
 */
public class PatchTestActivity extends Activity {
    Button button1;
    Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patch_test);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPatch();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printLog();
            }
        });
    }

    private void addPatch() {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/patchtest/out.apatch");
        if (file.exists()) {
            ((MyApplication) getApplication()).addPatch(file.getPath());
        }
    }

    private void printLog() {
        String text = "this is version 1";
        Log.i("zk", text);
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}

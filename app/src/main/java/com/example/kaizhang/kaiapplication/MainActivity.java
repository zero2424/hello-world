package com.example.kaizhang.kaiapplication;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;


public class MainActivity extends Activity {
    private View charge_status_percentage;
    private ImageView charge_status_car;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        charge_status_percentage = findViewById(R.id.charge_status_percentage);
        charge_status_car = (ImageView) findViewById(R.id.charge_status_car);
        charge_status_percentage.setOnClickListener(new View.OnClickListener() {
            int i = 0;

            @Override
            public void onClick(View v) {
                runCarAnim(i % 2 == 0);
                i++;
            }
        });
    }

    private void runCarAnim(boolean run) {
        if (run) {
            charge_status_car.setImageResource(R.drawable.phev_car);
            AnimationDrawable animationDrawable = (AnimationDrawable) charge_status_car.getDrawable();
            animationDrawable.start();
        } else {
            AnimationDrawable animationDrawable = (AnimationDrawable) charge_status_car.getDrawable();
            if (animationDrawable != null) {
                animationDrawable.stop();
                animationDrawable.setCallback(null);
            }
            charge_status_car.clearAnimation();
            charge_status_car.setImageResource(R.drawable.onstar_charge_status_frame0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

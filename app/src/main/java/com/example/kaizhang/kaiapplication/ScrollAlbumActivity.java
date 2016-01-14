package com.example.kaizhang.kaiapplication;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.TextView;

import com.example.kaizhang.kaiapplication.view.CustomHorizontalScrollView;
import com.example.kaizhang.kaiapplication.view.OnstarAlbum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kai zhang on 2015/12/28.
 */
public class ScrollAlbumActivity extends Activity {
    private OnstarAlbum album;
    private List<String> itemDesc;
    private CustomHorizontalScrollView scrollView;
    private ArrayList<Integer> grids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_album);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        album = (OnstarAlbum) findViewById(R.id.album);
        album.setItemLayoutId(R.layout.scroll_album_item);
        album.setScreenSize(size);
        scrollView = (CustomHorizontalScrollView) findViewById(R.id.album_scrollview);
        scrollView.setScrollViewListener(new CustomHorizontalScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(View scrollView, int x, int y, int oldx, int oldy) {
                if (grids == null) {
                    grids = album.getGrids();
                }
            }

            @Override
            public void onScrollStopped(int stopPosition) {
                Log.i("zk", "grids=" + grids + ",stopPosition=" + stopPosition);
                if (grids == null)
                    return;
                int nP = findNearestPosition(grids, stopPosition);
                scrollView.smoothScrollTo(grids.get(nP), 0);
            }
        });
        itemDesc = new ArrayList<String>();
        for (int i = 0; i < 4; i++) {
            itemDesc.add(i + "");
        }
        initItems(new String[]{"1", "2", "3", "4", "5", "6"});
    }

    private int findNearestPosition(List<Integer> list, int number) {
        if (list == null || list.size() <= 1) {
            return 0;
        }
        int diff = list.get(0) - number;
        for (int i = 1; i < list.size(); i++) {
            int nDiff = list.get(i) - number;
            if (Math.abs(nDiff) >= Math.abs(diff)) {
                return i - 1;
            }
            diff = nDiff;
        }
        return list.size() - 1;
    }

    private void initItems(String[] titles) {
        for (int i = 0; i < titles.length; i++) {
            View view = album.addOne();
            TextView tv1 = (TextView) view.findViewById(R.id.album_item_tv);
            tv1.setText(titles[i]);
        }
    }
}

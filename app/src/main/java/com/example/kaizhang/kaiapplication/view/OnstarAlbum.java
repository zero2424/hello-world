package com.example.kaizhang.kaiapplication.view;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by kai zhang on 2015/12/28.
 */
public class OnstarAlbum extends LinearLayout {
    private Context context;
    private int childCount;
    private int layoutId;
    private int screenW, screenH;
    private ArrayList<Integer> grids;

    public OnstarAlbum(Context context) {
        this(context, null);
    }

    public OnstarAlbum(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OnstarAlbum(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    public void setScreenSize(Point point) {
        screenW = point.x;
        screenH = point.y;
    }

    private void initView() {
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (childCount == 0 && getChildCount() != 0) {
            initChildren();
        }
    }

    public void setItemLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    public View addOne() {
        View view = LayoutInflater.from(context).inflate(layoutId, this, false);
        addView(view);
        return view;
    }

    private void initChildren() {
        int fullSize = screenW;
        Log.i("zk", "fullSize=" + fullSize);
        childCount = getChildCount();
        grids = new ArrayList<Integer>(childCount);
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
            layoutParams.width = fullSize / 2;
            if (i == 0) {
                layoutParams.leftMargin = fullSize / 4;
            } else {
                layoutParams.leftMargin = fullSize / 10;
            }
            if (i == childCount - 1) {
                layoutParams.rightMargin = fullSize / 4;
            } else {
                layoutParams.rightMargin = 0;
            }
            childView.setLayoutParams(layoutParams);
            grids.add((fullSize / 2 + fullSize / 10) * i);
        }
    }

    public ArrayList<Integer> getGrids() {
        return grids;
    }
}

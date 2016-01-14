package com.example.kaizhang.kaiapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.kaizhang.kaiapplication.controller.DragController;
import com.example.kaizhang.kaiapplication.database.DatabaseHelper;
import com.example.kaizhang.kaiapplication.drag_interface.BubbleButton;
import com.example.kaizhang.kaiapplication.layout.DragLayer;
import com.example.kaizhang.kaiapplication.layout.FunctionListLayout;
import com.example.kaizhang.kaiapplication.layout.Hotseat;
import com.example.kaizhang.kaiapplication.modle.FunctionInfo;
import com.example.kaizhang.kaiapplication.utils.DatabaseSingleInstance;

import java.util.List;


public class FunctionsActivity extends Activity implements View.OnLongClickListener, View.OnClickListener {
    private FunctionListLayout functionListLayout;
    private Hotseat mHotseat;
    private DragLayer dragLayer;
    private List<FunctionInfo> mFunctionInfoDatas;
    private DragController mDragController;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_buttons_view);
        functionListLayout = (FunctionListLayout) findViewById(R.id.functionlistlayout);
        mHotseat = (Hotseat) findViewById(R.id.hotseat);
        dragLayer = (DragLayer) findViewById(R.id.draglayer);
        functionListLayout.setOnLongClickListener(this);
        mHotseat.setOnLongClickListener(this);
        functionListLayout.setOnClickListener(this);
        mHotseat.setOnClickListener(this);
        databaseHelper = DatabaseSingleInstance.getInstance().getDatabaseHelper(this);
        prepareDatas();
        functionListLayout.setDatas(mFunctionInfoDatas);
        mHotseat.setDatas(mFunctionInfoDatas);
        mDragController = new DragController(dragLayer);
        dragLayer.setDragController(mDragController);
        mDragController.addDropTarget(functionListLayout);
        mDragController.addDragListener(functionListLayout);
        mDragController.addDropTarget(mHotseat);
        mDragController.addDragListener(mHotseat);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
    }

    private void prepareDatas() {
        mFunctionInfoDatas = databaseHelper.getAll();
    }

    boolean isHotseatLayout(View layout) {
        return mHotseat != null && layout != null && layout == mHotseat;
    }

    @Override
    public boolean onLongClick(View v) {
        if (!mDragController.isDragging()) {
            if (v instanceof BubbleButton) {
                if (((BubbleButton) v).getBelongTo() == BubbleButton.BelongTo.Hotseat) {
                    mDragController.startDrag(v, mHotseat, v.getTag());
                } else if (((BubbleButton) v).getBelongTo() == BubbleButton.BelongTo.FunctionList) {
                    mDragController.startDrag(v, functionListLayout, v.getTag());
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        Log.i("zk", "onClick v=" + v);
        if (v instanceof BubbleButton) {
            if (!((BubbleButton) v).hasPerformedLongPress()) {

            }
        }
    }
}

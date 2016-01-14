package com.example.kaizhang.kaiapplication.layout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.example.kaizhang.kaiapplication.R;
import com.example.kaizhang.kaiapplication.controller.DragController;
import com.example.kaizhang.kaiapplication.drag_interface.BubbleButton;
import com.example.kaizhang.kaiapplication.drag_interface.DragSource;
import com.example.kaizhang.kaiapplication.drag_interface.DropTarget;
import com.example.kaizhang.kaiapplication.modle.CellInfo;
import com.example.kaizhang.kaiapplication.modle.FunctionInfo;
import com.example.kaizhang.kaiapplication.utils.DatabaseSingleInstance;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by kai zhang on 2016/1/6.
 */
public class Hotseat extends FrameLayout implements DropTarget, DragSource, DragController.DragListener {
    private TreeSet<FunctionInfo> mDatas;
    private Context mContext;
    private int startH;
    private OnLongClickListener mOnLongClickListener;
    private OnClickListener mOnClickListener;
    private DragLayer mDragLayer;

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        mDatas = new TreeSet<>(new FunctionInfoComparator());
    }

    public void setDatas(List<FunctionInfo> datas) {
        mDatas.clear();
        for (int i = 0; i < datas.size(); i++) {
            FunctionInfo functionInfo = datas.get(i);
            if (functionInfo.isInHotseat()) {
                mDatas.add(functionInfo);
            }
        }
        initChildren();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        this.mOnLongClickListener = l;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        this.mOnClickListener = l;
    }

    private float getDp(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics());
    }

    private void initChildren() {
        if (mDatas == null)
            return;
        mDragLayer = (DragLayer) getParent();
        removeAllViews();
        startH = mDragLayer.startH;
        Iterator<FunctionInfo> iterator = mDatas.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            FunctionInfo functionInfo = iterator.next();
            BubbleButton function = addNewButton(functionInfo, i);
            addView(function);
        }
    }

    private void rearrange() {
        Iterator<FunctionInfo> iterator = mDatas.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            FunctionInfo functionInfo = iterator.next();
            functionInfo.setOrderInHotseat(i);
            DatabaseSingleInstance.getInstance().getDatabaseHelper(getContext()).update(functionInfo);
        }
    }

    private BubbleButton addNewButton(FunctionInfo functionInfo, int index) {
        int standardMargin = mDragLayer.standardMargin;
        int functionSize = mDragLayer.functionSize;
        String funcName = functionInfo.getTitle();
        BubbleButton function = new BubbleButton(getContext());
        function.setTag(new CellInfo(function, functionInfo));
        function.setBelongTo(BubbleButton.BelongTo.Hotseat);
        function.setBackgroundResource(R.drawable.function_icon);
        function.setTextColor(Color.WHITE);
        function.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        function.setText(funcName);
        function.setGravity(Gravity.CENTER);
        function.setOnLongClickListener(mOnLongClickListener);
        function.setOnClickListener(mOnClickListener);
        FrameLayout.LayoutParams functionParams = new FrameLayout.LayoutParams(functionSize, functionSize);
        functionParams.setMargins(standardMargin + index % 4 * (2 * standardMargin + functionSize), startH + standardMargin + index / 4 * (functionSize + standardMargin), 0, standardMargin);
        Log.i("zk", "functionParams leftMargin" + functionParams.leftMargin + ",functionParams topMargin" + functionParams.topMargin);
        function.setLayoutParams(functionParams);
        return function;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getY() <= startH) {
            return false;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {

    }

    @Override
    public void onDragEnd() {

    }

    @Override
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 0;
    }

    @Override
    public void onFlingToDeleteCompleted() {

    }

    @Override
    public void onDropCompleted(View target, DragObject d, boolean isFlingToDelete, boolean success) {
        CellInfo cellInfo = (CellInfo) d.dragInfo;
        FunctionInfo functionInfo = cellInfo.getFunctionInfo();
        if (success && target instanceof FunctionListLayout && mDatas.contains(functionInfo)) {
            mDatas.remove(functionInfo);
            rearrange();
            initChildren();
        } else {
            mDragLayer.animateViewIntoPosition(d.dragView, cellInfo.getCell(), -1,
                    null, this);
        }
    }

    @Override
    public boolean isDropEnabled() {
        return true;
    }

    @Override
    public void onDrop(DragObject dragObject) {
        CellInfo cellInfo = (CellInfo) dragObject.dragInfo;
        final FunctionInfo functionInfo;
        try {
            functionInfo = (FunctionInfo) cellInfo.getFunctionInfo().clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return;
        }
        View cell = cellInfo.getCell();
        functionInfo.setOrderInHotseat(mDatas.size());
        functionInfo.setIsInHotseat(true);
        DatabaseSingleInstance.getInstance().getDatabaseHelper(getContext()).update(functionInfo);
        View newCell = addNewButton(functionInfo, mDatas.size());
        newCell.setVisibility(INVISIBLE);
        addView(newCell);
        mDatas.add(functionInfo);
        final Runnable onCompleteRunnable = new Runnable() {
            @Override
            public void run() {
            }
        };
        mDragLayer.animateViewIntoPosition(dragObject.dragView, cell, newCell, -1,
                onCompleteRunnable, this);
    }

    @Override
    public void onClick(DragObject dragObject) {

    }

    @Override
    public void onDragEnter(DragObject dragObject) {

    }

    @Override
    public void onDragOver(DragObject dragObject) {

    }

    @Override
    public void onDragExit(DragObject dragObject) {

    }

    @Override
    public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {

    }

    @Override
    public boolean acceptDrop(DragObject dragObject) {
        CellInfo cellInfo = (CellInfo) dragObject.dragInfo;
        FunctionInfo functionInfo;
        try {
            functionInfo = (FunctionInfo) cellInfo.getFunctionInfo().clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return false;
        }
        if (!functionInfo.isInHotseat() && mDatas.size() < 8 && dragObject.dragSource instanceof FunctionListLayout) {
            return true;
        }
        return false;
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        mDragLayer.getDescendantRectRelativeToSelf(this, outRect);
        outRect.top = mDragLayer.startH;
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {

    }

    private class FunctionInfoComparator implements Comparator<FunctionInfo> {
        @Override
        public int compare(FunctionInfo lhs, FunctionInfo rhs) {
            return lhs.getOrderInHotseat() - rhs.getOrderInHotseat();
        }
    }

}

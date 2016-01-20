package com.example.kaizhang.kaiapplication.layout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by kai zhang on 2016/1/6.
 */
public class FunctionListLayout extends FrameLayout implements DropTarget, DragSource, DragController.DragListener {
    private int titleId = 0x00001000;
    private int functionId = 0x00002000;
    private LinkedHashMap<String, TreeSet<FunctionInfo>> mDatas;
    private List<FunctionInfo> mRawDatas;
    private Context mContext;
    private OnLongClickListener mOnLongClickListener;
    private OnClickListener mOnClickListener;
    private DragLayer mDragLayer;

    public FunctionListLayout(Context context) {
        this(context, null);
    }

    public FunctionListLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FunctionListLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        mDatas = new LinkedHashMap<>();
    }

    public void setDatas(List<FunctionInfo> datas) {
        mRawDatas = datas;
        mDatas.clear();
        mDatas.put(FunctionInfo.Favorites.getFunctionTypeDesc(FunctionInfo.Favorites.RemoteControl), new TreeSet<FunctionInfo>(new FunctionInfoListComparator()));
        mDatas.put(FunctionInfo.Favorites.getFunctionTypeDesc(FunctionInfo.Favorites.VehicleState), new TreeSet<FunctionInfo>(new FunctionInfoListComparator()));
        mDatas.put(FunctionInfo.Favorites.getFunctionTypeDesc(FunctionInfo.Favorites.Navigation), new TreeSet<FunctionInfo>(new FunctionInfoListComparator()));
        mDatas.put(FunctionInfo.Favorites.getFunctionTypeDesc(FunctionInfo.Favorites.Other), new TreeSet<FunctionInfo>(new FunctionInfoListComparator()));
        for (int i = 0; i < datas.size(); i++) {
            FunctionInfo functionInfo = datas.get(i);
            if (functionInfo.isInHotseat()) {
                continue;
            }
            switch (functionInfo.getFunctionType()) {
                case FunctionInfo.Favorites.RemoteControl:
                    mDatas.get(FunctionInfo.Favorites.getFunctionTypeDesc(FunctionInfo.Favorites.RemoteControl)).add(functionInfo);
                    break;
                case FunctionInfo.Favorites.VehicleState:
                    mDatas.get(FunctionInfo.Favorites.getFunctionTypeDesc(FunctionInfo.Favorites.VehicleState)).add(functionInfo);
                    break;
                case FunctionInfo.Favorites.Navigation:
                    mDatas.get(FunctionInfo.Favorites.getFunctionTypeDesc(FunctionInfo.Favorites.Navigation)).add(functionInfo);
                    break;
                case FunctionInfo.Favorites.Other:
                    mDatas.get(FunctionInfo.Favorites.getFunctionTypeDesc(FunctionInfo.Favorites.Other)).add(functionInfo);
                    break;
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
        mDragLayer = (DragLayer) getParent().getParent();
        int standardMargin = mDragLayer.standardMargin;
        removeAllViews();
        Iterator<Map.Entry<String, TreeSet<FunctionInfo>>> iterator = mDatas.entrySet().iterator();
        int functionSize = mDragLayer.functionSize;
        int titleHeight = (int) getDp(20);
        FrameLayout.LayoutParams functionParams = null;
        ScrollView scrollView = (ScrollView) getParent();
        scrollView.getLayoutParams().height = mDragLayer.startH;
        for (int i = 0; iterator.hasNext(); i++) {
            Map.Entry<String, TreeSet<FunctionInfo>> entry = iterator.next();
            TextView titleTv = new TextView(getContext());
            titleTv.setTextColor(Color.WHITE);
            titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            titleTv.setText(entry.getKey());
            titleTv.setId(titleId + i);
            titleTv.setGravity(Gravity.CENTER_VERTICAL);
            FrameLayout.LayoutParams titleParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, titleHeight);
            titleParams.setMargins(standardMargin, standardMargin, standardMargin, standardMargin);
            if (i > 0) {
                titleParams.topMargin = functionParams.topMargin + functionSize + standardMargin;
                if (i == mDatas.size() - 1) {
                    titleParams.bottomMargin = standardMargin + (entry.getValue().size() / 4 + 1) * (functionSize + standardMargin);
                }
            }
            addView(titleTv, titleParams);
            if (entry.getValue() == null || entry.getValue().size() == 0) {
                functionParams = new FrameLayout.LayoutParams(functionSize, functionSize);
                functionParams.topMargin = titleParams.topMargin + standardMargin + titleHeight - functionSize;
                continue;
            }
            Iterator<FunctionInfo> elementIterator = entry.getValue().iterator();
            for (int j = 0; elementIterator.hasNext(); j++) {
                FunctionInfo functionInfo =elementIterator.next();
                String funcName = functionInfo.getTitle();
                BubbleButton function = new BubbleButton(getContext());
                function.setTag(new CellInfo(function, functionInfo));
                function.setBelongTo(BubbleButton.BelongTo.FunctionList);
                function.setBackgroundResource(R.drawable.function_icon);
                function.setTextColor(Color.WHITE);
                function.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                function.setText(funcName);
                function.setId(functionId + i + j);
                function.setGravity(Gravity.CENTER);
                function.setOnLongClickListener(mOnLongClickListener);
                function.setOnClickListener(mOnClickListener);
                functionParams = new FrameLayout.LayoutParams(functionSize, functionSize);
                functionParams.setMargins(standardMargin + j % 4 * (2 * standardMargin + functionSize), titleParams.topMargin + standardMargin + titleHeight +
                        j / 4 * (functionSize + standardMargin), 0, standardMargin);
                addView(function, functionParams);
            }
        }
    }

    private BubbleButton findChildByFunctionInfo(FunctionInfo functionInfo) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof BubbleButton) {
                CellInfo cellInfo = (CellInfo) view.getTag();
                if (cellInfo.getFunctionInfo() == functionInfo) {
                    return (BubbleButton) view;
                }
            }
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
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
        if (success && target instanceof Hotseat && mDatas.get(FunctionInfo.Favorites.getFunctionTypeDesc(functionInfo.getFunctionType())).contains(functionInfo)) {
            mDatas.get(FunctionInfo.Favorites.getFunctionTypeDesc(functionInfo.getFunctionType())).remove(functionInfo);
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
        functionInfo.setIsInHotseat(false);
        mDatas.get(FunctionInfo.Favorites.getFunctionTypeDesc(functionInfo.getFunctionType())).add(functionInfo);
        initChildren();
        View newCell = findChildByFunctionInfo(functionInfo);
        if (newCell == null) {
            return;
        }
        DatabaseSingleInstance.getInstance().getDatabaseHelper(getContext()).update(functionInfo);
        ScrollView parent = (ScrollView) getParent();
        FrameLayout.LayoutParams layoutParams = (LayoutParams) newCell.getLayoutParams();
        float lowY = layoutParams.topMargin + layoutParams.height - parent.getScrollY();
        float highY = layoutParams.topMargin - parent.getScrollY();
        if (lowY > mDragLayer.startH) {
            parent.scrollTo(parent.getScrollX(), (int) (parent.getScrollY() + lowY + mDragLayer.standardMargin - mDragLayer.startH));
        } else if (highY < 0) {
            parent.scrollTo(parent.getScrollX(), (int) (parent.getScrollY() + highY - mDragLayer.standardMargin));
        }
        newCell.setVisibility(INVISIBLE);
        final Runnable onCompleteRunnable = new Runnable() {
            @Override
            public void run() {
            }
        };
        mDragLayer.animateViewIntoPosition(dragObject.dragView, cell, newCell, -1,
                onCompleteRunnable, this);
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
        if (functionInfo.isInHotseat() && dragObject.dragSource instanceof Hotseat) {
            return true;
        }
        return false;
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        mDragLayer.getDescendantRectRelativeToSelf(this, outRect);
        outRect.bottom = mDragLayer.startH;
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {

    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {

    }

    @Override
    public void onDragEnd() {

    }

    private class FunctionInfoListComparator implements Comparator<FunctionInfo> {
        @Override
        public int compare(FunctionInfo lhs, FunctionInfo rhs) {
            return lhs.getOrderInFunctionListLayout() - rhs.getOrderInFunctionListLayout();
        }
    }
}

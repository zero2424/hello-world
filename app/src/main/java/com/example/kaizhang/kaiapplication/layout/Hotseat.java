package com.example.kaizhang.kaiapplication.layout;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.example.kaizhang.kaiapplication.R;
import com.example.kaizhang.kaiapplication.controller.DragController;
import com.example.kaizhang.kaiapplication.drag_interface.DragSource;
import com.example.kaizhang.kaiapplication.drag_interface.DropTarget;
import com.example.kaizhang.kaiapplication.drag_interface.widget.BubbleTextView;
import com.example.kaizhang.kaiapplication.modle.CellInfo;
import com.example.kaizhang.kaiapplication.modle.FunctionInfo;
import com.example.kaizhang.kaiapplication.utils.DatabaseSingleInstance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private boolean deleteMode;
    private List<Point> grids;
    private final int COLUMN = 4;
    private LayoutInflater mInflater;

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
        mDatas = new TreeSet<>(new FunctionInfoHotseatComparator());
        grids = new ArrayList<>();
        mInflater = LayoutInflater.from(mContext);
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
        grids.clear();
        mDragLayer = (DragLayer) getParent();
        removeAllViews();
        startH = mDragLayer.startH;
        Iterator<FunctionInfo> iterator = mDatas.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            FunctionInfo functionInfo = iterator.next();
            BubbleTextView function = addNewButton(functionInfo, i);
            addView(function);
        }
    }

    private void reIndex() {
        Iterator<FunctionInfo> iterator = mDatas.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            FunctionInfo functionInfo = iterator.next();
            functionInfo.setOrderInHotseat(i);
            DatabaseSingleInstance.getInstance().getDatabaseHelper(getContext()).update(functionInfo);
        }
    }

    private void reSort(int newOrder, int oldOrder) {
        if (newOrder == oldOrder) {
            return;
        }
        Iterator<FunctionInfo> iterator = mDatas.iterator();
        Map<FunctionInfo, Integer> functionInfoIntegerHashMap = new HashMap<FunctionInfo, Integer>();
        for (; iterator.hasNext(); ) {
            FunctionInfo functionInfo = iterator.next();
            if (functionInfo.getOrderInHotseat() == oldOrder) {
                functionInfoIntegerHashMap.put(functionInfo, newOrder);
                continue;
            }
            if (newOrder > oldOrder) {
                if (functionInfo.getOrderInHotseat() > oldOrder && functionInfo.getOrderInHotseat() <= newOrder) {
                    functionInfoIntegerHashMap.put(functionInfo, functionInfo.getOrderInHotseat() - 1);
                }
            } else {
                if (functionInfo.getOrderInHotseat() >= newOrder && functionInfo.getOrderInHotseat() < oldOrder) {
                    functionInfoIntegerHashMap.put(functionInfo, functionInfo.getOrderInHotseat() + 1);
                }
            }
        }
        mDatas.removeAll(functionInfoIntegerHashMap.keySet());
        Iterator<Map.Entry<FunctionInfo, Integer>> infoIntegerIterator = functionInfoIntegerHashMap.entrySet().iterator();
        for (; infoIntegerIterator.hasNext(); ) {
            Map.Entry<FunctionInfo, Integer> map = infoIntegerIterator.next();
            map.getKey().setOrderInHotseat(map.getValue());
            DatabaseSingleInstance.getInstance().getDatabaseHelper(getContext()).update(map.getKey());
            mDatas.add(map.getKey());
        }
    }

    private int figureOutOrderFromPosition(float targetX, float targetY, float originX, float originY) {
        int onePiece = mDragLayer.functionSize + 2 * mDragLayer.standardMargin;
        float diffX = targetX - originX;
        float diffY = targetY - originY;
        if (Math.abs(diffX) <= onePiece && Math.abs(diffY) <= onePiece) {
            return -1;
        }
        int horizontalGrid = grids.size() < COLUMN ? grids.size() : COLUMN;
        int gridX = -1;
        for (int i = 0; i < horizontalGrid; i++) {
            if (grids.get(i).x >= targetX) {
                if (targetX - originX > 0) {
                    gridX = i - 1;
                    break;
                } else {
                    gridX = i;
                    break;
                }
            }
        }
        if (targetX - originX > 0 && gridX == -1) {
            gridX = horizontalGrid - 1;
        }
        int gridY = -1;
        int rowNum = grids.size() / COLUMN + (grids.size() % COLUMN != 0 ? 1 : 0);
        for (int i = 0; i < rowNum; i++) {
            if (grids.get(COLUMN * i).y >= targetY) {
                if (targetY - originY > 0) {
                    gridY = i - 1;
                    break;
                } else {
                    gridY = i;
                    break;
                }
            }
        }
        if (targetY - originY > 0 && gridY == -1) {
            gridY = grids.size() != COLUMN ? rowNum - 1 : rowNum;
        }
        int result = gridX + gridY * COLUMN;
        result = result > grids.size() - 1 ? grids.size() - 1 : result;
        Point point = new Point((int) originX, (int) originY);
        if (grids.indexOf(point) == result) {
            result = -1;
        }
        return result;
    }

    private FunctionInfo getFunctionInfoFromOrder(int order) {
        Iterator<FunctionInfo> infoIterator = mDatas.iterator();
        while (infoIterator.hasNext()) {
            FunctionInfo functionInfo = infoIterator.next();
            if (functionInfo.getOrderInHotseat() == order) {
                return functionInfo;
            }
        }
        return null;
    }

    private BubbleTextView addNewButton(FunctionInfo functionInfo, int index) {
        int standardMargin = mDragLayer.standardMargin;
        int functionSize = mDragLayer.functionSize;
        String funcName = functionInfo.getTitle();
        BubbleTextView function = (BubbleTextView) mInflater.inflate(R.layout.function_icon, this, false);
        function.applyFromShortcutInfo(new CellInfo(function, functionInfo, getResources().getDrawable(R.drawable.ic_launcher)));
        function.setBelongTo(BubbleTextView.BelongTo.Hotseat);
//        function.setBackgroundResource(R.drawable.function_icon);
//        function.setTextColor(Color.WHITE);
//        function.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
//        function.setText(funcName);
        function.setGravity(Gravity.CENTER);
        function.setOnLongClickListener(mOnLongClickListener);
        function.setOnClickListener(mOnClickListener);
        FrameLayout.LayoutParams functionParams = new FrameLayout.LayoutParams(functionSize, functionSize);
        functionParams.setMargins(standardMargin + index % 4 * (2 * standardMargin + functionSize), startH + standardMargin + index / 4 * (functionSize + standardMargin), 0, standardMargin);
        Log.i("zk", "functionParams leftMargin" + functionParams.leftMargin + ",functionParams topMargin" + functionParams.topMargin);
        function.setLayoutParams(functionParams);
        Point point = new Point(functionParams.leftMargin, functionParams.topMargin);
        grids.add(point);
        return function;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getY() <= startH) {
            if (deleteMode) {
                dimissDeleteMode();
                return true;
            }
            return false;
        } else {
            return super.onTouchEvent(event);
        }
    }

    private void dimissDeleteMode() {
        deleteMode = false;
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
            reIndex();
            initChildren();
        } else {

        }
    }

    @Override
    public boolean isDropEnabled() {
        return true;
    }

    @Override
    public void onDrop(DragObject dragObject) {
        CellInfo cellInfo = (CellInfo) dragObject.dragInfo;
        FunctionInfo originFunctionInfo = cellInfo.getFunctionInfo();
        if (originFunctionInfo.isInHotseat()) {
            View cell = cellInfo.getCell();
            LayoutParams layoutParams = (LayoutParams) cell.getLayoutParams();
            float dx = dragObject.dragView.getX();
            float dy = dragObject.dragView.getY();
            int newOrder = figureOutOrderFromPosition(dx, dy, layoutParams.leftMargin, layoutParams.topMargin);
            if (newOrder == -1) {//no change
                mDragLayer.animateViewIntoPosition(dragObject.dragView, cell, -1,
                        null, this);
            } else {
                reSort(newOrder, originFunctionInfo.getOrderInHotseat());
                initChildren();
                Log.i("zk", "mDatas=" + mDatas);
                Log.i("zk", "dx=" + dx + ",dy=" + dy + ",layoutParams.leftMargin" + layoutParams.leftMargin + ",layoutParams.topMargin" + layoutParams.topMargin);
                mDragLayer.animateViewIntoPosition(dragObject.dragView, cell, getChildAt(newOrder), -1,
                        null, this);
            }

        } else {
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
        FunctionInfo functionInfo = cellInfo.getFunctionInfo();
        if (functionInfo.isInHotseat() || (mDatas.size() < 8 && dragObject.dragSource instanceof FunctionListLayout)) {
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

    private class FunctionInfoHotseatComparator implements Comparator<FunctionInfo> {
        @Override
        public int compare(FunctionInfo lhs, FunctionInfo rhs) {
            return lhs.getOrderInHotseat() - rhs.getOrderInHotseat();
        }
    }

}

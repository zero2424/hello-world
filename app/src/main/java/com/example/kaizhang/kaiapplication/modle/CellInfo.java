package com.example.kaizhang.kaiapplication.modle;

import android.view.View;

/**
 * Created by kai zhang on 2016/1/8.
 */
public class CellInfo {
    private View cell;
    private FunctionInfo functionInfo;
    int cellX = -1;
    int cellY = -1;
    int spanX;
    int spanY;
    long screenId;
    long container;

    public CellInfo(View v, FunctionInfo info) {
        cell = v;
        functionInfo = info;
    }

    public View getCell() {
        return cell;
    }

    public void setCell(View cell) {
        this.cell = cell;
    }

    public FunctionInfo getFunctionInfo() {
        return functionInfo;
    }

    public void setFunctionInfo(FunctionInfo functionInfo) {
        this.functionInfo = functionInfo;
    }
}

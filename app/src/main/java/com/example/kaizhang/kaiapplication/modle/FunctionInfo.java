package com.example.kaizhang.kaiapplication.modle;

/**
 * Created by kai zhang on 2016/1/11.
 */
public class FunctionInfo {
    static final int NO_ID = -1;
    private long _id = NO_ID;
    private String title;
    private boolean isInHotseat;
    private int functionType;
    private int orderInHotseat;
    private int orderInFunctionListLayout;

    public FunctionInfo(long _id, String title, boolean isInHotseat, int functionType, int orderInHotseat, int orderInFunctionListLayout) {
        this._id = _id;
        this.title = title;
        this.isInHotseat = isInHotseat;
        this.functionType = functionType;
        this.orderInHotseat = orderInHotseat;
        this.orderInFunctionListLayout = orderInFunctionListLayout;
    }

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isInHotseat() {
        return isInHotseat;
    }

    public void setIsInHotseat(boolean isInHotseat) {
        this.isInHotseat = isInHotseat;
    }

    public int getFunctionType() {
        return functionType;
    }

    public void setFunctionType(int functionType) {
        this.functionType = functionType;
    }

    public int getOrderInHotseat() {
        return orderInHotseat;
    }

    public void setOrderInHotseat(int orderInHotseat) {
        this.orderInHotseat = orderInHotseat;
    }

    public int getOrderInFunctionListLayout() {
        return orderInFunctionListLayout;
    }

    public void setOrderInFunctionListLayout(int orderInFunctionListLayout) {
        this.orderInFunctionListLayout = orderInFunctionListLayout;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        FunctionInfo functionInfo = new FunctionInfo(_id, title, isInHotseat, functionType, orderInHotseat, orderInFunctionListLayout);
        return functionInfo;
    }

    @Override
    public String toString() {
        return "title=" + title + ",isInHotseat=" + isInHotseat + ",orderInHotseat=" + orderInHotseat;
    }

    public static final class Favorites {
        public static final String ID = "_id";
        public static final String TITLE = "title";
        public static final String ISINHOTSEAT = "isInHotseat";
        public static final String FUNCTIONTYPE = "functionType";
        public static final String ORDERINHOTSEAT = "orderInHotseat";
        public static final String ORDERINFUNCTIONLISTLAYOUT = "orderInFunctionListLayout";

        public static final int RemoteControl = 0;
        public static final int VehicleState = 1;
        public static final int Navigation = 2;
        public static final int Other = 3;

        public static String getFunctionTypeDesc(int functionType) {
            switch (functionType) {
                case RemoteControl:
                    return "远程遥控";
                case VehicleState:
                    return "车辆状态";
                case Navigation:
                    return "导航";
                case Other:
                    return "其他";
                default:
                    return "";
            }
        }

    }

}

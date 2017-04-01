package com.mx.pull_lay;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/3/31.
 */

public class MyViewGroup extends ViewGroup {
    public MyViewGroup(Context context) {
        super(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(1000,1000);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int topSum = 0;
        int leftSum = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            view.layout(0,topSum,leftSum+view.getMeasuredWidth(),view.getMeasuredHeight());
            leftSum = leftSum+view.getMeasuredWidth();
            topSum = topSum+view.getMeasuredHeight();
        }
    }
}

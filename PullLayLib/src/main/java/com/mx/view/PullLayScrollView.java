package com.mx.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;

import com.mx.pulllay.lib.R;
import com.mx.view.base.PullLayBase;

/**
 * 创建人： zhangmengxiong
 * 创建时间： 2017/3/31.
 * 联系方式: zmx_final@163.com
 */

public class PullLayScrollView extends PullLayBase {

    public PullLayScrollView(Context context) {
        super(context);
        initView(context, null);
    }

    public PullLayScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public PullLayScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public PullLayScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        if (attrs == null) return;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PullLayout);
        try {
            int head_lay = array.getResourceId(R.styleable.PullLayout_pull_down_lay, -1);
            if (head_lay > 0) {
                View head = LayoutInflater.from(context).inflate(head_lay, null);
                setHeaderView(head);
            }

            int foot_lay = array.getResourceId(R.styleable.PullLayout_pull_up_lay, -1);
            if (foot_lay > 0) {
                View footer = LayoutInflater.from(context).inflate(foot_lay, null);
                setFooterView(footer);
            }

            {
                int height = array.getDimensionPixelOffset(R.styleable.PullLayout_pull_height, 60);
                setFooterPullHeight(height);
                setHeadPullHeight(height);

                height = array.getDimensionPixelOffset(R.styleable.PullLayout_pull_up_height, -1);
                if (height >= 0) setFooterPullHeight(height);
                height = array.getDimensionPixelOffset(R.styleable.PullLayout_pull_down_height, -1);
                if (height >= 0) setHeadPullHeight(height);
            }

            boolean mEnablePullDown = array.getBoolean(R.styleable.PullLayout_pull_down, true);
            boolean mEnablePullUp = array.getBoolean(R.styleable.PullLayout_pull_up, true);
            setEnabledPullUp(mEnablePullUp);
            setEnablePullDown(mEnablePullDown);
        } finally {
            array.recycle();
        }
    }

    @Override
    protected final boolean isViewOnTopScroll(View view) {
        boolean intercept = false;
        if (view == null || !(view instanceof ScrollView)) {
            throw new IllegalStateException("Child View Must be a ScrollView");
        }
        
        if (view.getScrollY() <= 0) {
            intercept = true;
        }
        return intercept;
    }

    @Override
    protected final boolean isViewOnBottomScroll(View view) {
        boolean intercept = false;
        if (view == null || !(view instanceof ScrollView)) {
            throw new IllegalStateException("Child View Must be a ScrollView");
        }

        ScrollView scrollView = (ScrollView) view;
        View scrollChild = scrollView.getChildAt(0);

        if (scrollView.getScrollY() >= (scrollChild.getHeight() - scrollView.getHeight())) {
            intercept = true;
        }
        return intercept;
    }
}

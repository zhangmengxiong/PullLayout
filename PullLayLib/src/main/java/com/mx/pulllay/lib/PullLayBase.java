package com.mx.pulllay.lib;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

import static com.mx.pulllay.lib.PullStatus.LOAD_MORE;
import static com.mx.pulllay.lib.PullStatus.NORMAL;
import static com.mx.pulllay.lib.PullStatus.REFRESH;
import static com.mx.pulllay.lib.PullStatus.TRY_LOAD_MORE;
import static com.mx.pulllay.lib.PullStatus.TRY_REFRESH;

/**
 * 创建人： zhangmengxiong
 * 创建时间： 2017/3/31.
 * 联系方式: zmx_final@163.com
 */
abstract class PullLayBase extends ViewGroup {
    // Scroller的滑动速度
    private static final int SCROLL_SPEED = 650;


    // 是否允许下拉刷新
    private boolean mEnablePullDown;
    // 是否允许上拉加载
    private boolean mEnablePullUp;

    // 头视图(即上拉刷新时显示的部分)
    private View mHeaderView;
    // 尾视图(即下拉加载时显示的部分)
    private View mFooterView;
    // 用于平滑滑动的Scroller对象
    private Scroller mLayoutScroller;

    private PullStatus cStatus = PullStatus.NORMAL;

    // 事件监听接口
    private IRefreshListener mListener;

    // 最小有效滑动距离(滑动超过该距离才视作一次有效的滑动刷新/加载操作)
    private int mEffectiveScroll;

    public PullLayBase(Context context) {
        super(context);
        init(context);
    }

    public PullLayBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullLayBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public PullLayBase(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        // 实例化Scroller
        mLayoutScroller = new Scroller(context);
    }

    public void setOnRefreshListener(IRefreshListener listener) {
        mListener = listener;
    }

    public void setFooterView(View mFooterView) {
        this.mFooterView = mFooterView;
    }

    public void setHeaderView(View mHeaderView) {
        this.mHeaderView = mHeaderView;
    }

    public void setEnabledPullUp(boolean b) {
        this.mEnablePullUp = b;
        if (!mEnablePullUp && mFooterView != null && mFooterView.isShown()) {
            mFooterView.setVisibility(View.GONE);
        }
    }

    public void setEnablePullDown(boolean b) {
        this.mEnablePullDown = b;
        if (!b && mHeaderView != null && mHeaderView.isShown()) {
            mHeaderView.setVisibility(View.GONE);
        }
    }

    int lastChildIndex;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        lastChildIndex = getChildCount() - 1;

        // 添加上拉刷新部分
        if (mEnablePullDown)
            addLayoutHeader();
        // 添加下拉加载部分
        if (mEnablePullUp)
            addLayoutFooter();
    }

    /**
     * 添加上拉刷新布局作为header
     */
    private void addLayoutHeader() {
        if (mHeaderView == null) return;
        // 设置布局参数(宽度为MATCH_PARENT,高度为MATCH_PARENT)
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        // 将Header添加进Layout当中
        addView(mHeaderView, params);
    }

    /**
     * 添加下拉加载布局作为footer
     */
    private void addLayoutFooter() {
        if (mFooterView == null) return;
        // 设置布局参数(宽度为MATCH_PARENT,高度为MATCH_PARENT)
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        // 将footer添加进Layout当中
        addView(mFooterView, params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 遍历进行子视图的测量工作
        for (int i = 0; i < getChildCount(); i++) {
            // 通知子视图进行测量
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }

        int height = 0;
        if (mHeaderView != null) {
            height = mHeaderView.getMeasuredHeight();
        }

        if (mFooterView != null) {
            height = Math.max(height, mFooterView.getMeasuredHeight());
        }
        if (height > 0) mEffectiveScroll = 60;
    }

    // ViewGroup的内容高度(不包括header与footer的高度)
    private int mLayoutContentHeight;
    // 当滚动到内容最底部时Y轴所需要滑动的举例
    private int mReachBottomScroll;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 重置(避免重复累加)
        mLayoutContentHeight = 0;
        // 遍历进行子视图的置位工作
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child == mHeaderView) { // 头视图隐藏在ViewGroup的顶端
                child.layout(0, 0 - child.getMeasuredHeight(), child.getMeasuredWidth(), 0);
            } else if (child == mFooterView) { // 尾视图隐藏在ViewGroup所有内容视图之后
                child.layout(0, mLayoutContentHeight, child.getMeasuredWidth(), mLayoutContentHeight + child.getMeasuredHeight());
            } else { // 内容视图根据定义(插入)顺序,按由上到下的顺序在垂直方向进行排列
                child.layout(0, mLayoutContentHeight, child.getMeasuredWidth(), mLayoutContentHeight + child.getMeasuredHeight());
                if (index <= lastChildIndex) {
                    if (child instanceof ScrollView) {
                        mLayoutContentHeight += getMeasuredHeight();
                        continue;
                    }
                    mLayoutContentHeight += child.getMeasuredHeight();
                }
            }
        }
        // 计算到达内容最底部时ViewGroup的滑动距离
        mReachBottomScroll = mLayoutContentHeight - getMeasuredHeight();
    }

    // 用于计算滑动距离的Y坐标中介
    private int mLastYMoved;
    // 用于判断是否拦截触摸事件的Y坐标中介
    private int mLastYIntercept;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.cStatus == REFRESH || this.cStatus == LOAD_MORE) return true;
        boolean intercept = false;
        // 记录此次触摸事件的y坐标
        int y = (int) event.getY();
        // 判断触摸事件类型
        switch (event.getAction()) {
            // Down事件
            case MotionEvent.ACTION_DOWN: {
                // 记录下本次系列触摸事件的起始点Y坐标
                mLastYMoved = y;
                // 不拦截ACTION_DOWN，因为当ACTION_DOWN被拦截，后续所有触摸事件都会被拦截
                intercept = false;
                break;
            }
            // Move事件
            case MotionEvent.ACTION_MOVE: {
                if (y > mLastYIntercept) { // 下滑操作
                    // 获取最顶部的子视图
                    View child = getChildAt(0);
                    intercept = isViewCanPullDown(child);
                } else if (y < mLastYIntercept) { // 上拉操作
                    // 获取最底部的子视图
                    View child = getChildAt(lastChildIndex);
                    intercept = isViewCanPullUp(child);
                } else {
                    intercept = false;
                }
                break;
            }
            // Up事件
            case MotionEvent.ACTION_UP: {
                intercept = false;
                break;
            }
        }

        mLastYIntercept = y;
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (isRefreshing()) return true;
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                // 计算本次滑动的Y轴增量(距离)
                int dy = mLastYMoved - y;
                Log.v("11", "" + dy);
                // 如果滑动增量小于0，即下拉操作
                if (dy < 0) {
                    if (mEnablePullDown) {
                        // 如果下拉的距离小于mHeaderView1/2的高度,则允许滑动
                        if (getScrollY() > 0 || Math.abs(getScrollY()) <= mHeaderView.getMeasuredHeight() / 3) {
                            if (cStatus != TRY_LOAD_MORE && cStatus != LOAD_MORE) {
                                scrollBy(0, dy);
                                if (cStatus != REFRESH) {
                                    if (getScrollY() <= 0) {
                                        if (cStatus != TRY_REFRESH)
                                            updateStatus(TRY_REFRESH);

                                        if (Math.abs(getScrollY()) > mEffectiveScroll)
                                            updateStatus(REFRESH);
                                    }
                                }
                            } else {
                                if (getScrollY() > 0) {
                                    dy = dy > 30 ? 30 : dy;
                                    scrollBy(0, dy);
                                    if (getScrollY() < mReachBottomScroll + mEffectiveScroll) {
                                        updateStatus(TRY_LOAD_MORE);
                                    }
                                }
                            }
                        }
                    }
                } else if (dy > 0) {
                    if (mEnablePullUp) {
                        if (getScrollY() <= mReachBottomScroll + mFooterView.getMeasuredHeight() / 3) {
                            // 进行Y轴上的滑动
                            if (cStatus != TRY_REFRESH && cStatus != REFRESH) {
                                scrollBy(0, dy);
                                if (cStatus != LOAD_MORE) {
                                    if (getScrollY() >= mReachBottomScroll) {
                                        if (cStatus != TRY_LOAD_MORE)
                                            updateStatus(TRY_LOAD_MORE);

                                        if (getScrollY() >= mReachBottomScroll + mEffectiveScroll)
                                            updateStatus(LOAD_MORE);
                                    }
                                }
                            } else {
                                if (getScrollY() <= 0) {
                                    dy = dy > 30 ? 30 : dy;
                                    scrollBy(0, dy);
                                    if (Math.abs(getScrollY()) < mEffectiveScroll)
                                        updateStatus(TRY_REFRESH);
                                }
                            }
                        }
                    }
                }
                // 记录y坐标
                mLastYMoved = y;
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                // 判断本次触摸系列事件结束时,Layout的状态
                switch (cStatus) {
                    case NORMAL: {
                        break;
                    }
                    case TRY_REFRESH: {
                        mLayoutScroller.startScroll(0, getScrollY(), 0, -getScrollY(), SCROLL_SPEED);
                        cStatus = NORMAL;
                        break;
                    }
                    case REFRESH: {
                        mLayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - (-mEffectiveScroll)), SCROLL_SPEED);
                        if (mListener != null)
                            mListener.onRefresh();
                        break;
                    }
                    case TRY_LOAD_MORE: {
                        mLayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - mReachBottomScroll), SCROLL_SPEED);
                        cStatus = NORMAL;
                        break;
                    }
                    case LOAD_MORE: {
                        mLayoutScroller.startScroll(0, getScrollY(), 0, -((getScrollY() - mEffectiveScroll) - mReachBottomScroll), SCROLL_SPEED);
                        if (mListener != null)
                            mListener.onLoadMore();
                        break;
                    }
                }
            }
        }
        Log.v("11", "" + this.cStatus);

        mLastYIntercept = 0;
        postInvalidate();
        return true;
    }

    private void updateStatus(PullStatus status) {
        switch (status) {
            case NORMAL:
                break;
            case TRY_REFRESH: {
                this.cStatus = TRY_REFRESH;
                break;
            }
            case REFRESH: {
                this.cStatus = REFRESH;
                break;
            }
            case TRY_LOAD_MORE: {
                this.cStatus = TRY_LOAD_MORE;
                break;
            }
            case LOAD_MORE:
                this.cStatus = LOAD_MORE;
//                tvPullUp.setText(R.string.srl_release_to_refresh);
                break;
        }
    }

    public boolean isRefreshing() {
        return cStatus == LOAD_MORE || cStatus == REFRESH;
    }

    public void resetLayoutLocation() {
        cStatus = NORMAL;
        scrollTo(0, 0);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mLayoutScroller.computeScrollOffset()) {
            scrollTo(0, mLayoutScroller.getCurrY());
        }
        postInvalidate();
    }

    abstract boolean isViewCanPullDown(View view);

    abstract boolean isViewCanPullUp(View view);
}

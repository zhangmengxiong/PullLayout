package com.mx.view.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

/**
 * 创建人： zhangmengxiong
 * 创建时间： 2017/3/31.
 * 联系方式: zmx_final@163.com
 */
public abstract class PullLayBase extends ViewGroup {
    // Scroller的滑动速度
    private int SCROLL_SPEED = 500;

    private Handler mHandler;
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
    private int mHeadPullHeight;
    private int mFooterPullHeight;

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
        mHandler = new Handler();
    }

    /**
     * 设置刷新监听
     *
     * @param listener
     */
    public void setOnRefreshListener(IRefreshListener listener) {
        mListener = listener;
    }

    /**
     * 设置下拉刷新松开时头部View的高度
     *
     * @param i 单位：px
     */
    public void setHeadPullHeight(int i) {
        this.mHeadPullHeight = i;
    }

    /**
     * 设置上拉加载松开时头部View的高度
     *
     * @param i 单位：px
     */
    public void setFooterPullHeight(int i) {
        this.mFooterPullHeight = i;
    }

    public void setFooterView(View mFooterView) {
        this.mFooterView = mFooterView;
    }

    /**
     * 设置动画时长
     *
     * @param i
     */
    public void setDuration(int i) {
        this.SCROLL_SPEED = i;
    }

    public View getFooterView() {
        return mFooterView;
    }

    public void setHeaderView(View mHeaderView) {
        this.mHeaderView = mHeaderView;
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    /**
     * 设置是否可以向上拖动加载
     *
     * @param b true：可以拖动
     *          false：不能拖动
     */
    public void setEnabledPullUp(boolean b) {
        this.mEnablePullUp = b;
        if (!mEnablePullUp && mFooterView != null && mFooterView.isShown()) {
            mFooterView.setVisibility(View.GONE);
        }
    }

    /**
     * 设置是否可以向下拖动刷新
     *
     * @param b true：可以拖动
     *          false：不能拖动
     */
    public void setEnablePullDown(boolean b) {
        this.mEnablePullDown = b;
        if (!b && mHeaderView != null && mHeaderView.isShown()) {
            mHeaderView.setVisibility(View.GONE);
        }
    }

    private int lastChildIndex;

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
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
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
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        // 将footer添加进Layout当中
        addView(mFooterView, params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = getChildCount();
        if (mHeaderView != null) size--;
        if (mFooterView != null) size--;
        if (size > 1) {
            throw new IllegalStateException("PullLay can host only one direct child");
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 遍历进行子视图的测量工作
        for (int i = 0; i < getChildCount(); i++) {
            // 通知子视图进行测量
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
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
    // 手指按下的Y轴
    private int mLastYTouch;

    // 用于判断是否拦截触摸事件的Y坐标中介
    private int mLastYIntercept;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (cStatus != PullStatus.NORMAL) return true;
        boolean intercept = false;
        // 记录此次触摸事件的y坐标
        int y = (int) event.getY();
        // 判断触摸事件类型
        switch (event.getAction()) {
            // Down事件
            case MotionEvent.ACTION_DOWN: {
                // 记录下本次系列触摸事件的起始点Y坐标
                mLastYTouch = y;
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
                    intercept = isViewOnTopScroll(child);
                } else if (y < mLastYIntercept) { // 上拉操作
                    // 获取最底部的子视图
                    View child = getChildAt(lastChildIndex);
                    intercept = isViewOnBottomScroll(child);
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

    private Boolean isTouchDown = null;// 是否往下拖动的标记

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isRefreshing() || isInAnimation()) return true;

        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // 记录下本次系列触摸事件的起始点Y坐标
                mLastYTouch = y;
                mLastYMoved = y;
                isTouchDown = null;
                updateStatus(PullStatus.NORMAL);
                if (isRefreshing()) return true;
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                // 是否往下拖动
                if (isTouchDown == null) {
                    isTouchDown = mLastYTouch - y < 0;
                }

                // 计算本次滑动的Y轴增量(距离)
                int dy = mLastYMoved - y;
//                Log.v("11", "getScrollY() =" + getScrollY());
                // 如果滑动增量小于0，即下拉操作

                if (isTouchDown) {
                    // 正在向下滑动
                    if (mEnablePullDown) {
                        int scrollSize = Math.abs(mLastYTouch - y);
                        if (scrollSize <= mHeadPullHeight * 3) {
                            if (dy + getScrollY() < 0) {
                                // 还没滑动到顶部，自动滑
                                scrollBy(0, dy);
                            } else {
                                // 滑动距离超过了顶部，再算一次滑动距离！
                                dy = -getScrollY();
                                scrollBy(0, dy);
                            }
                        }
                        if (scrollSize < mHeadPullHeight) {
                            // 状态：TRY_LOAD_MORE
                            updateStatus(PullStatus.START_REFRESH);
                        } else if (y > mLastYTouch) {
                            updateStatus(PullStatus.TRY_REFRESH);
                        }
                    }
                } else {
                    // 正在向上拖动
                    if (mEnablePullUp) {
                        int scrollSize = Math.abs(mLastYTouch - y);
                        if (scrollSize <= mFooterPullHeight * 3) {
                            if (dy + getScrollY() >= 0) {
                                scrollBy(0, dy);
                            } else {
                                dy = -getScrollY();
                                scrollBy(0, dy);
                            }
                        }
                        if (scrollSize < mFooterPullHeight) {
                            // 状态：TRY_LOAD_MORE
                            updateStatus(PullStatus.START_LOAD_MORE);
                        } else if (getScrollY() > 0) {
                            updateStatus(PullStatus.TRY_LOAD_MORE);
                        }
                    }
                }
                // 记录y坐标
                mLastYMoved = y;
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mLastYTouch = y;
                mLastYMoved = y;
                isTouchDown = null;

                // 判断本次触摸系列事件结束时,Layout的状态
                switch (cStatus) {
                    case START_REFRESH: {
                        mLayoutScroller.startScroll(0, getScrollY(), 0, -getScrollY(), SCROLL_SPEED);
                        updateStatus(PullStatus.NORMAL);
                        break;
                    }
                    case TRY_REFRESH: {
                        updateStatus(PullStatus.REFRESH);
                        mLayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() + mHeadPullHeight), SCROLL_SPEED);
                        break;
                    }
                    case REFRESH: {
                        break;
                    }
                    case START_LOAD_MORE: {
                        updateStatus(PullStatus.NORMAL);
                        mLayoutScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - mReachBottomScroll), SCROLL_SPEED);
                        break;
                    }
                    case TRY_LOAD_MORE: {
                        updateStatus(PullStatus.LOAD_MORE);
                        mLayoutScroller.startScroll(0, getScrollY(), 0, -((getScrollY() - mFooterPullHeight) - mReachBottomScroll), SCROLL_SPEED);
                        break;
                    }
                }
            }
        }

        mLastYIntercept = 0;
        postInvalidate();
        return true;
    }

    /**
     * 更新当前的状态！
     *
     * @param status
     */
    private void updateStatus(PullStatus status) {
        boolean change = (status != cStatus);
        switch (status) {
            case NORMAL:
                break;
            case START_REFRESH:
                if (change && mListener != null) {
                    mListener.onPullDownStart(mHeaderView);
                }
                break;
            case TRY_REFRESH:
                if (change && mListener != null) {
                    mListener.onPullDownLoad(mHeaderView);
                }
                break;
            case REFRESH:
                if (change && mListener != null) {
                    mListener.onPullDownSuccess(mHeaderView);
                }
                break;
            case START_LOAD_MORE:
                if (change && mListener != null) {
                    mListener.onPullUpStart(mFooterView);
                }
                break;
            case TRY_LOAD_MORE:
                if (change && mListener != null) {
                    mListener.onPullUpLoad(mFooterView);
                }
                break;
            case LOAD_MORE:
                if (change && mListener != null) {
                    mListener.onPullUpSuccess(mFooterView);
                }
                break;
        }
        if (change) {
            Log.v("aa", "" + status);
        }
        this.cStatus = status;
    }

    /**
     * 是否正在刷新状态
     *
     * @return
     */
    public boolean isRefreshing() {
        return cStatus == PullStatus.LOAD_MORE || cStatus == PullStatus.REFRESH;
    }

    /**
     * 是否正在运行动画中
     *
     * @return
     */
    private boolean isInAnimation() {
        return mLayoutScroller != null && !mLayoutScroller.isFinished();
    }

    public void refreshFinish(int delay) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(delayFinish, delay);
    }

    private Runnable delayFinish = new Runnable() {
        @Override
        public void run() {
            refreshFinish();
        }
    };

    /**
     * 刷新完成！
     */
    public void refreshFinish() {
        cStatus = PullStatus.NORMAL;
        if (mLayoutScroller != null && !mLayoutScroller.isFinished())
            mLayoutScroller.abortAnimation();
        mLayoutScroller.startScroll(0, getScrollY(), 0, -getScrollY(), SCROLL_SPEED);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mLayoutScroller.computeScrollOffset()) {
            scrollTo(0, mLayoutScroller.getCurrY());
        }
        postInvalidate();
    }

    /**
     * 判断当前容器内的控件是否到达了顶部
     *
     * @param view 子控件对象
     * @return 如果返回false，则说明子空间还可以向上滑动，否则将拦截向上滑动的事件！
     */
    protected abstract boolean isViewOnTopScroll(View view);

    /**
     * 判断子控件是否到达了底部不能再向上滑动
     *
     * @param view 子控件对象
     * @return 如果返回false，则说明子空间还可以向下滑动，否则将拦截向下滑动的事件！
     */
    protected abstract boolean isViewOnBottomScroll(View view);
}

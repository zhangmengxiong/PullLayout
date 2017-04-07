package com.mx.view.base;

/**
 * 创建人： zhangmengxiong
 * 创建时间： 2017/3/31.
 * 联系方式: zmx_final@163.com
 */

enum PullStatus {
    NORMAL,         // 普通状态
    START_REFRESH,  // 开始向下拉
    TRY_REFRESH,    // 超过一定的距离后，放开触摸就会刷新
    REFRESH,        // 刷新状态
    START_LOAD_MORE,// 开始往上拉
    TRY_LOAD_MORE,  // 超过一定的距离后，放开触摸就会刷新
    LOAD_MORE       // 加载状态
}

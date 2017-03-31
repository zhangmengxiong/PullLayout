package com.mx.pulllay.lib;

/**
 * 创建人： zhangmengxiong
 * 创建时间： 2017/3/31.
 * 联系方式: zmx_final@163.com
 */

public enum PullStatus {
    NORMAL,         // 普通状态
    TRY_REFRESH,    // 意图刷新
    REFRESH,        // 刷新状态
    TRY_LOAD_MORE,  // 意图加载
    LOAD_MORE       // 加载状态
}

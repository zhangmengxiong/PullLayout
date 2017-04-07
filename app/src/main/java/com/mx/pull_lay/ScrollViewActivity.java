package com.mx.pull_lay;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mx.view.PullLayScrollView;
import com.mx.view.base.IRefreshListener;

public class ScrollViewActivity extends Activity {
    private PullLayScrollView adaptView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_view);
        adaptView = (PullLayScrollView) findViewById(R.id.scrollView);
        adaptView.setOnRefreshListener(new IRefreshListener() {
            TextView pullDownTxv;

            @Override
            public void onPullDownStart(View view) {
                if (pullDownTxv == null)
                    pullDownTxv = (TextView) view.findViewById(R.id.srl_tv_pull_down);
                pullDownTxv.setText("用力往下拉");
            }

            @Override
            public void onPullDownLoad(View view) {
                if (pullDownTxv == null)
                    pullDownTxv = (TextView) view.findViewById(R.id.srl_tv_pull_down);
                pullDownTxv.setText("松开刷新");
            }

            @Override
            public void onPullUpStart(View view) {
            }

            @Override
            public void onPullUpLoad(View view) {
            }

            @Override
            public void onPullDownSuccess(View view) {
                if (pullDownTxv == null)
                    pullDownTxv = (TextView) view.findViewById(R.id.srl_tv_pull_down);
                pullDownTxv.setText("正在刷新");
                adaptView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullDownTxv.setText("刷新成功！");
                        adaptView.refreshFinish(1000);
                    }
                }, 100);
            }

            @Override
            public void onPullUpSuccess(View view) {
                adaptView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adaptView.refreshFinish(1000);
                    }
                }, 100);
            }
        });
    }

}

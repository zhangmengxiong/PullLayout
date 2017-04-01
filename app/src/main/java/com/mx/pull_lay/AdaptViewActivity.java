package com.mx.pull_lay;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mx.pulllay.lib.IRefreshListener;
import com.mx.pulllay.lib.PullLayAdapterView;

public class AdaptViewActivity extends Activity {
    private final String TAG = getClass().getSimpleName();
    private PullLayAdapterView adaptView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapt_view);
        adaptView = (PullLayAdapterView) findViewById(R.id.adaptView);
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
                        adaptView.refreshFinish();
                    }
                }, 2000);
            }

            @Override
            public void onPullUpSuccess(View view) {
                adaptView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adaptView.refreshFinish();
                    }
                }, 2000);
            }
        });

        ListView list = (ListView) findViewById(R.id.listView);

        //定义数据源作为ListView内容
        String[] arr_data = {"数据1", "数据2", "数据3", "数据4",
                "数据12", "数据12", "数据13", "数据14",
                "数据21", "数据22", "数据23", "数据24",
                "数据31", "数据32", "数据33", "数据34"};

        //新建一个数组适配器ArrayAdapter绑定数据，参数(当前的Activity，布局文件，数据源)
        ArrayAdapter arr_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr_data);
        //视图(ListView)加载适配器
        list.setAdapter(arr_adapter);
    }
}

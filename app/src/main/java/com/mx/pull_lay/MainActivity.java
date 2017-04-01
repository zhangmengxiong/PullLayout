package com.mx.pull_lay;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mx.pulllay.lib.IRefreshListener;
import com.mx.pulllay.lib.PullLayAdapterView;

public class MainActivity extends Activity {
    private final String TAG = getClass().getSimpleName();
    private PullLayAdapterView adaptView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adaptView = (PullLayAdapterView) findViewById(R.id.adaptView);
        adaptView.setOnRefreshListener(new IRefreshListener() {
            @Override
            public void onPullDownStart() {
                Log.v(TAG, "onPullDownStart()");
            }

            @Override
            public void onPullDownLoad() {
                Log.v(TAG, "onPullDownLoad()");
            }

            @Override
            public void onPullUpStart() {
                Log.v(TAG, "onPullUpStart()");
            }

            @Override
            public void onPullUpLoad() {
                Log.v(TAG, "onPullUpLoad()");
            }

            @Override
            public void onPullDownSuccess() {
                Log.v(TAG, "onPullDownSuccess()");
                adaptView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adaptView.refreshFinish();
                    }
                }, 2000);
            }

            @Override
            public void onPullUpSuccess() {
                Log.v(TAG, "onPullUpSuccess()");
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

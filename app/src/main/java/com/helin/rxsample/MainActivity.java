package com.helin.rxsample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kc.lite_rxjava.base.ActivityLifeCycleEvent;
import com.kc.lite_rxjava.base.BaseActivity;
import com.kc.lite_rxjava.http.Api;
import com.kc.lite_rxjava.http.HttpUtil;
import com.kc.lite_rxjava.view.SimpleLoadDialog;


import rx.Observable;

public class MainActivity extends BaseActivity {

    private TextView mText;
    private SimpleLoadDialog dialogHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialogHandler = new SimpleLoadDialog(MainActivity.this, null, true);
        mText = (TextView) findViewById(R.id.text);
        ((Button) findViewById(R.id.btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dialogHandler.obtainMessage(SimpleLoadDialog.SHOW_PROGRESS_DIALOG).sendToTarget();
                doGet();
            }
        });
    }

    /**
     * 提示开发者在系统内存不足的时候，通过处理部分资源来释放内存，从而避免被 Android 系统杀死
     * @param level
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    /**
     * 会取消请求
     */
    @Override
    protected void onPause() {
        super.onPause();
//        dialogHandler.obtainMessage(SimpleLoadDialog.DISMISS_PROGRESS_DIALOG).sendToTarget();
    }

    private void doGet() {
        //获取豆瓣电影TOP 100
        Observable ob = Api.getDefault().getTopMovie(0, 100);

        HttpUtil.getInstance().toSubscribe(ob, new ProgressSubscriber<List<Subject>>(this) {
            @Override
            protected void _onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void _onNext(List<Subject> list) {
                String str = "";
                for (int i = 0; i < list.size(); i++) {
                    str += "电影名：" + list.get(i).getTitle() + "\n";
                }
                mText.setText(str);
            }
        }, "cacheKey", ActivityLifeCycleEvent.DESTROY, lifecycleSubject, false, false);
    }
}

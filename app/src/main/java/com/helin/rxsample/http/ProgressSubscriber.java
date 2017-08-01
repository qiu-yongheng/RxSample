package com.helin.rxsample.http;


import android.content.Context;

import com.helin.rxsample.view.SimpleLoadDialog;

import rx.Subscriber;

/**
 * Created by helin on 2016/10/10 15:49.
 * 订阅者
 */

public  abstract class ProgressSubscriber<T> extends Subscriber<T> implements ProgressCancelListener{


    private SimpleLoadDialog dialogHandler;

    public ProgressSubscriber(Context context) {
        dialogHandler = new SimpleLoadDialog(context,this,true);
    }

    @Override
    public void onCompleted() {
        dismissProgressDialog();
    }


    /**
     * 显示Dialog
     */
    public void showProgressDialog(){
        if (dialogHandler != null) {
            dialogHandler.show();
        }
    }

    @Override
    public void onNext(T t) {
        _onNext(t);
    }

    /**
     * 隐藏Dialog
     */
    private void dismissProgressDialog(){
        if (dialogHandler != null) {
            dialogHandler.dismiss();
            dialogHandler=null;
        }
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        // TODO 这里自行替换判断网络的代码
        if (false) {
            _onError("网络不可用");
        } else if (e instanceof ApiException) {
            _onError(e.getMessage());
        } else {
            _onError("请求失败，请稍后再试...");
        }

        // 隐藏Dialog
        dismissProgressDialog();
    }

    /**
     * ProgressCancelListener的回调
     */
    @Override
    public void onCancelProgress() {
        if (!this.isUnsubscribed()) {
            this.unsubscribe();
        }
    }

    protected abstract void _onNext(T t);
    protected abstract void _onError(String message);
}

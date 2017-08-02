package com.kc.lite_rxjava.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.helin.rxsample.R;
import com.helin.rxsample.http.ProgressCancelListener;

import java.lang.ref.WeakReference;

/**
 * 在handler中创建dialog
 */
public class SimpleLoadDialog extends Handler {

    private Dialog load = null;

    /**
     * 是否显示dialog的标示
     */
    public static final int SHOW_PROGRESS_DIALOG = 1;
    public static final int DISMISS_PROGRESS_DIALOG = 2;

    private Context context;
    private boolean cancelable;
    private ProgressCancelListener mProgressCancelListener;
    /**
     * 弱引用
     */
    private final WeakReference<Context> reference;

    public SimpleLoadDialog(Context context, ProgressCancelListener mProgressCancelListener, boolean cancelable) {
        super();
        // 弱引用, 保存context
        this.reference = new WeakReference<>(context);
        this.mProgressCancelListener = mProgressCancelListener;
        this.cancelable = cancelable;
    }

    /**
     * 创建dialog, 并显示
     */
    private void create() {
        if (load == null) {
            context = reference.get();

            /**
             * 创建dialog
             */
            load = new Dialog(context, R.style.loadstyle);
            View dialogView = LayoutInflater.from(context).inflate(
                    R.layout.custom_sload_layout, null);
            load.setCanceledOnTouchOutside(false);
            load.setCancelable(cancelable);
            load.setContentView(dialogView);

            /** dialog取消显示监听 */
            load.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (mProgressCancelListener != null)
                        mProgressCancelListener.onCancelProgress();
                }
            });

            /** 设置窗口位置 */
            Window dialogWindow = load.getWindow();
            dialogWindow.setGravity(Gravity.CENTER_VERTICAL
                    | Gravity.CENTER_HORIZONTAL);
        }
        if (!load.isShowing() && context != null) {
            load.show();
        }
    }

    /**
     * 创建dialog, 并显示
     */
    public void show() {
        create();
    }

    /**
     * 隐藏dialog
     * 1. dialog正在显示
     * 2. activity没有销毁
     */
    public void dismiss() {
        context = reference.get();
        if (load != null && load.isShowing() && !((Activity) context).isFinishing()) {
            String name = Thread.currentThread().getName();
            load.dismiss();
            load = null;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SHOW_PROGRESS_DIALOG:
                create();
                break;
            case DISMISS_PROGRESS_DIALOG:
                dismiss();
                break;
        }
    }
}

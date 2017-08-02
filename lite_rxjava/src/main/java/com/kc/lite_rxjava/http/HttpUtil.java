package com.kc.lite_rxjava.http;

import com.helin.rxsample.base.ActivityLifeCycleEvent;
import com.helin.rxsample.enity.HttpResult;

import rx.Observable;
import rx.functions.Action0;
import rx.subjects.PublishSubject;

/**
 * //  ┏┓　　　┏┓
 * //┏┛┻━━━┛┻┓
 * //┃　　　　　　　┃
 * //┃　　　━　　　┃
 * //┃　┳┛　┗┳　┃
 * //┃　　　　　　　┃
 * //┃　　　┻　　　┃
 * //┃　　　　　　　┃
 * //┗━┓　　　┏━┛
 * //   ┃　　　┃   神兽保佑
 * //   ┃　　　┃   代码无BUG！
 * //   ┃　　　┗━━━┓
 * //   ┃　　　　　　　┣┓
 * //   ┃　　　　　　　┏┛
 * //   ┗┓┓┏━┳┓┏┛
 * //     ┃┫┫　┃┫┫
 * //     ┗┻┛　┗┻┛
 * <p>
 * Created by helin on 2016/10/10 11:32.
 */

public class HttpUtil {

    /**
     * 构造方法私有
     */
    private HttpUtil() {

    }

    /**
     * 在访问HttpMethods时创建单例
     */
    private static class SingletonHolder {
        private static final HttpUtil INSTANCE = new HttpUtil();
    }

    /**
     * 获取单例
     */
    public static HttpUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 添加线程管理并订阅
     *
     * @param ob               网络请求返回的Observable
     * @param subscriber       自定义的订阅者
     * @param cacheKey         缓存kay
     * @param event            Activity 生命周期
     * @param lifecycleSubject 绑定了Activity的生命周期, 当生命周期发生改变时, 会发射数据
     * @param isSave           是否缓存
     * @param forceRefresh     是否强制刷新
     */
    public void toSubscribe(Observable ob,
                            final ProgressSubscriber subscriber,
                            String cacheKey,
                            final ActivityLifeCycleEvent event,
                            final PublishSubject<ActivityLifeCycleEvent> lifecycleSubject,
                            boolean isSave,
                            boolean forceRefresh) {
        /** 数据预处理, 对状态码进行处理, 绑定Activity生命周期 */
        Observable.Transformer<HttpResult<Object>, Object> result = RxHelper.handleResult(event, lifecycleSubject);

        /** 重用操作符 compose操作符, 功能类似flatMap, 只是compose操作符更适用于重用 */
        Observable observable = ob.compose(result)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        //显示Dialog和一些其他操作
                        subscriber.showProgressDialog();
                    }
                });

        /** 缓存 */
        RetrofitCache
                .load(cacheKey, observable, isSave, forceRefresh)
                .subscribe(subscriber);

    }
}

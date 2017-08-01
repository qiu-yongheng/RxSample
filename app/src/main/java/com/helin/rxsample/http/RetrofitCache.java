package com.helin.rxsample.http;

import com.orhanobut.hawk.Hawk;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by helin on 2016/11/10 10:41.
 * <p>
 * retrofit缓存
 */

public class RetrofitCache {
    /**
     * @param cacheKey     缓存的Key
     * @param fromNetwork
     * @param isSave       是否缓存
     * @param forceRefresh 是否强制刷新
     * @param <T>
     * @return
     */
    public static <T> Observable<T> load(final String cacheKey,
                                         Observable<T> fromNetwork,
                                         boolean isSave,
                                         boolean forceRefresh) {
        /**
         * 获取缓存
         */
        Observable<T> fromCache = Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                // 缓存数据到Hawk
                T cache = Hawk.get(cacheKey);
                if (cache != null) {
                    subscriber.onNext(cache);
                } else {
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());


        //是否缓存
        if (isSave) {
            /**
             * 这里的fromNetwork 不需要指定Schedule,在handleRequest中已经变换了
             */
            fromNetwork = fromNetwork.map(new Func1<T, T>() {
                @Override
                public T call(T result) {
                    // 缓存数据到Hawk
                    Hawk.put(cacheKey, result);
                    return result;
                }
            });
        }

        //强制刷新
        if (forceRefresh) {
            // 不读取缓存
            return fromNetwork;
        } else {
            // 判断是否需要读取缓存
            return Observable.concat(fromCache, fromNetwork).takeFirst(new Func1<T, Boolean>() {
                @Override
                public Boolean call(T t) {
                    return t != null;
                }
            });
        }
    }
}
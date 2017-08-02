package com.kc.lite_rxjava.http;


import android.support.annotation.NonNull;

import com.helin.rxsample.base.ActivityLifeCycleEvent;
import com.helin.rxsample.enity.HttpResult;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;


/**
 * Created by helin on 2016/11/9 17:02.
 *
 * 对数据的状态码进行处理, 添加Activity生命周期管理
 */

public class RxHelper {

    /**
     * 利用Observable.takeUntil()停止网络请求
     * <p>
     * 1. 当activity生命周期发生改变时, lifecycleSubject会发射数据
     * <p>
     * 2. 获取lifecycleSubject发射的数据, 如果与指定的相同(如指定pause), 返回true
     * <p>
     * 3. 触发takeFirst操作符, 发射判定为true的第一项数据
     * <p>
     * 4. takeUntil操作符, 当第二个Observable发射一项数据时, takeUntil停止发射数据
     *
     * @param event
     * @param lifecycleSubject
     * @param <T>
     * @return
     */
    @NonNull
    public <T> Observable.Transformer<T, T> bindUntilEvent(@NonNull final ActivityLifeCycleEvent event,
                                                           final PublishSubject<ActivityLifeCycleEvent> lifecycleSubject) {
        return new Observable.Transformer<T, T>() {

            @Override
            public Observable<T> call(Observable<T> sourceObservable) {
                /**
                 * first: 过滤操作符
                 * 当activity生命周期发生改变时, lifecycleSubject会发射数据
                 * <p>
                 * first: 传递一个谓词函数给first，然后发射这个函数判定为true的第一项数据
                 * takeFirst与first类似，除了这一点：
                 * 如果原始Observable没有发射任何满足条件的数据，first会抛出一个NoSuchElementException，
                 * takeFist会返回一个空的Observable（不调用onNext()但是会调用onCompleted）
                 */
                Observable<ActivityLifeCycleEvent> compareLifecycleObservable = lifecycleSubject.takeFirst(new Func1<ActivityLifeCycleEvent, Boolean>() {
                    @Override
                    public Boolean call(ActivityLifeCycleEvent activityLifeCycleEvent) {
                        /** 发射这个函数判定为true的第一项数据 */
                        return activityLifeCycleEvent.equals(event);
                    }
                });

                /**
                 * Observable1.takeUntil(Observable2))
                 *
                 * 第二个Observable发射一项数据或一个onError通知或一个onCompleted通知都会导致takeUntil停止发射数据
                 */
                return sourceObservable.takeUntil(compareLifecycleObservable);
            }
        };
    }

    /**
     * 网络请求获取到数据, 对数据的状态码进行处理:
     * 1. 请求成功, 返回数据
     * 2. 请求失败, 统一给ApiException处理
     *
     * 对Activity的生命周期进行监听, 在界面销毁时, 停止发射数据
     *
     * @param <T>
     * @return
     */
    public static <T> Observable.Transformer<HttpResult<T>, T> handleResult(final ActivityLifeCycleEvent event,
                                                                            final PublishSubject<ActivityLifeCycleEvent> lifecycleSubject) {
        return new Observable.Transformer<HttpResult<T>, T>() {
            /**
             * 把 HTTPResult<T> 转换为 T
             * @param tObservable
             * @return
             */
            @Override
            public Observable<T> call(Observable<HttpResult<T>> tObservable) {
                Observable<ActivityLifeCycleEvent> compareLifecycleObservable =
                        lifecycleSubject.takeFirst(new Func1<ActivityLifeCycleEvent, Boolean>() {
                            @Override
                            public Boolean call(ActivityLifeCycleEvent activityLifeCycleEvent) {
                                /** 如果返回true, takeFirst操作符发射第一个为true的数据 */
                                return activityLifeCycleEvent.equals(event);
                            }
                        });

                /**
                 *
                 */
                return tObservable.flatMap(new Func1<HttpResult<T>, Observable<T>>() {
                    @Override
                    public Observable<T> call(HttpResult<T> result) {
                        /** 处理状态码, 如果请求成功, 返回数据, 请求失败, 抛出异常 */
                        if (result.getCount() != 0) {
                            return createData(result.getSubjects());
                        } else {
                            return Observable.error(new ApiException(result.getCount()));
                        }
                    }
                }).takeUntil(compareLifecycleObservable) // 如果compareLifecycleObservable发射数据, takeUntil停止发射数据
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 创建成功的数据
     *
     * @param data
     * @param <T>
     * @return
     */
    private static <T> Observable<T> createData(final T data) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                try {
                    subscriber.onNext(data);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

}

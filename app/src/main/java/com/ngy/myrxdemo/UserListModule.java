package com.ngy.myrxdemo;

import android.util.Log;

import com.ngy.myrxdemo.data.Common;
import com.ngy.myrxdemo.data.Events;
import com.ngy.myrxdemo.data.User;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by niuguangyuan on 7/20/2015.
 */
public enum UserListModule {

    INSTANCE;

    private ConnectableObservable<Object> mRefreshEventEmitter;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    private Queue<User> mUserCache = new ConcurrentLinkedQueue<>();

    public void init() {
        mRefreshEventEmitter = RxBus.INSTANCE.toObserverable().publish();
        mSubscription.add(mRefreshEventEmitter.subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                if (o instanceof Events.RefreshUserListEvent) {
                    Log.d("UserListModule", "refresh button clicked");
                    if (mUserCache.size() < Common.DISPLAY_USER_LIST_SIZE) {
                        getUserListFromServerObservable()
                        .subscribe(new Observer<List<User>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(List<User> users) {
                                addUsers(users);
                                broadcastNextUserBatch();
                            }
                        });
                    } else {
                        broadcastNextUserBatch();
                    }
                }
            }
        }));
        mSubscription.add(mRefreshEventEmitter.connect());
    }

    private void broadcastNextUserBatch() {
        if (RxBus.INSTANCE.hasObservers()) {
            List<User> userList = new ArrayList<>();
            for (int i = 0; i < Common.DISPLAY_USER_LIST_SIZE; i++) {
                userList.add(mUserCache.poll());
            }
            RxBus.INSTANCE.send(new Events.NewDisplayListEvent(userList));
        }
    }

    private synchronized void addUsers(List<User> userList) {
        mUserCache.addAll(userList);
    }


    private Observable<List<User>> getUserListFromServerObservable() {
        String url = "https://api.github.com/users?since=" + (int) (Math.random() * 500);
        return Observable.just(url).map(new Func1<String, List<User>>() {
            @Override
            public List<User> call(String url) {
                String response = HttpClient.INSTANCE.getCall(url);
                return parseUserList(response);
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }


    private List<User> parseUserList(String respStr) {
        List<User> userList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(respStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                User user = new User();
                user.icon = jsonArray.getJSONObject(i).getString("avatar_url");
                user.name = jsonArray.getJSONObject(i).getString("login");
                userList.add(user);
            }
            return userList;
        } catch (JSONException e) {
            return null;
        }
    }

}

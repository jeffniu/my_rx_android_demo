package com.ngy.myrxdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ngy.myrxdemo.data.Events;
import com.ngy.myrxdemo.data.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by niuguangyuan on 7/20/2015.
 */
public class NetworkDemoActivity extends Activity {

    RecyclerView mUserListView;
    UserAdapter mUserAdapter;
    TextView mClickInfo;

    Button mRefreshButton;
    CompositeSubscription mBusSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_demo_layout);
        mUserListView = (RecyclerView) findViewById(R.id.user_list);
        mUserListView.setLayoutManager(new LinearLayoutManager(this));
        mUserAdapter = new UserAdapter(this);
        mUserListView.setAdapter(mUserAdapter);
        mRefreshButton = (Button) findViewById(R.id.refresh);
        mClickInfo = (TextView) findViewById(R.id.click_info);

        mRefreshClickStream = ViewObservable.clicks(mRefreshButton).observeOn(AndroidSchedulers.mainThread());

        mRefreshClickStream.debounce(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
        .map(new Func1<OnClickEvent, Events.RefreshUserListEvent>() {
            @Override
            public Events.RefreshUserListEvent call(OnClickEvent onClickEvent) {
                return new Events.RefreshUserListEvent();
            }
        })
        .mergeWith(Observable.just(new Events.RefreshUserListEvent()))
        .subscribe(RxBus.INSTANCE.toObserver());


        ConnectableObservable<Object> newListEventEmitter = RxBus.INSTANCE.toObserverable().publish();
        mBusSubscription = new CompositeSubscription();
        mBusSubscription.add(AppObservable.bindActivity(this, newListEventEmitter)
        .subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                if (o instanceof Events.NewDisplayListEvent) {
                    Events.NewDisplayListEvent event = (Events.NewDisplayListEvent) o;
                    mUserAdapter.setUserList(event.displayList);
                } else if (o instanceof  Events.NewDataAddedEvent) {
                    Events.NewDataAddedEvent event = (Events.NewDataAddedEvent) o;
                    mClickInfo.setText(String.format("%d more users added to cache at %d", event.size, System.currentTimeMillis()));
                } else {

                }
            }
        }));
        mBusSubscription.add(newListEventEmitter.connect());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    Observable<OnClickEvent> mRefreshClickStream;


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

        private Context mContext;

        public UserAdapter(Context context) {
            mContext = context;
            mUserList = new ArrayList<>();
        }

        static class UserViewHolder extends RecyclerView.ViewHolder {

            View mView;
            TextView mUserName;
            ImageView mUserIcon;

            public UserViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
                mUserName = (TextView) itemView.findViewById(R.id.user_name);
                mUserIcon = (ImageView) itemView.findViewById(R.id.user_icon);
            }

        }

        private List<User> mUserList;

        List<User> getUserList() {
            return mUserList;
        }

        void setUserList(List<User> userList) {
            mUserList = userList;
            notifyDataSetChanged();
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout, null);
            UserViewHolder viewHolder = new UserViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            if (position < mUserList.size()) {
                User user = mUserList.get(position);
                holder.mUserName.setText(user.name);
                Picasso.with(mContext).load(user.icon).into(holder.mUserIcon);
            }
        }

        @Override
        public int getItemCount() {
            return mUserList.size();
        }
    }


}

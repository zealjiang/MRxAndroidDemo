package com.example.library.mrxandroiddemo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity {

    @BindView(R.id.main_thread)
    Button mThreadButton;

    @BindView(R.id.main_async)
    Button mAsyncButton;

    @BindView(R.id.main_rx)
    Button mRxButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // 线程运行
        mThreadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThreadButton.setEnabled(false);
                longRunningOperation();
                Snackbar.make(mThreadButton, longRunningOperation(), Snackbar.LENGTH_LONG).show();
                mThreadButton.setEnabled(true);
            }
        });

        // 异步运行
        mAsyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAsyncButton.setEnabled(false);
                new MyAsyncTasks().execute();
            }
        });


        // 使用IO线程处理, 主线程响应
        final Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext(longRunningOperation());
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        // 响应式运行
        mRxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRxButton.setEnabled(false);
                observable.subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        mRxButton.setEnabled(true);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        Snackbar.make(mRxButton, s, Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
    }



    // 长时间运行的任务
    private String longRunningOperation() {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            Log.e("DEBUG", e.toString());
        }

        return "Complete!";
    }

    // 异步线程
    private class MyAsyncTasks extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            Snackbar.make(mRxButton, s, Snackbar.LENGTH_LONG).show();
            mAsyncButton.setEnabled(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            return longRunningOperation();
        }
    }


}

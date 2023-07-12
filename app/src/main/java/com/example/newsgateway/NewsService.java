package com.example.newsgateway;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class NewsService extends Service {
    private static final String ACTION_ARTICLES = "ARTICLES";
    private static final String ACTION_SERVICE = "SERVICE";

    private boolean running = true;

    private ArrayList<Article> articleList = new ArrayList<>();
    private ServiceReceiver serviceReceiver;
    private NewsService newsService;

    public NewsService() {
       newsService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION_SERVICE);
        registerReceiver(serviceReceiver,intentFilter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(running){
                    while(articleList.isEmpty()){
                        try {
                            Thread.sleep(250);
                        } catch(InterruptedException e)
                        {e.printStackTrace();}
                    }
                    Intent articleIntent = new Intent();
                    articleIntent.setAction(ACTION_ARTICLES);
                    articleIntent.putExtra("articles",articleList);
                    sendBroadcast(articleIntent);
                    articleList.clear();
                }
            }
        }).start();
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(serviceReceiver);
        running = false;
        super.onDestroy();
    }

    public class ServiceReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: intent's action is " +  intent.getAction());
            switch (intent.getAction()){
                case ACTION_SERVICE:
                    String sourceId = intent.getStringExtra("sourceID");
                    ArticleLoaderRunnable art = new ArticleLoaderRunnable(newsService, sourceId,handler);
                    new Thread(art).start();
                    break;
            }
        }
    }

    public void getArticles(ArrayList<Article> articles){
        articleList.clear();
        articleList.addAll(articles);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                ArrayList<Article> s = (ArrayList<Article>) msg.obj;
                getArticles(s);

            }
        }
    };
}
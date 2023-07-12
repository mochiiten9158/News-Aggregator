package com.example.newsgateway;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class ArticleLoaderRunnable implements Runnable {
    private static final String URL_START = "https://newsapi.org/v2/everything?sources=";
    private static final String URL_END = "&language=en&pageSize=100&apiKey=9c142167aef840c3ac0ec3fadef0c02e";

    private NewsService newsService;
    private String sourceId;
    private Handler handler;
    private ArrayList<Article> articlesList;

    public ArticleLoaderRunnable(NewsService newsService, String sourceId,Handler handler) {
        this.newsService = newsService;
        this.sourceId = sourceId;
        this.handler = handler;
    }

    private void processResults(String s) {
        try{
            Message message = new Message();
            message.what = 1;
            message.obj = parseJSON(s);
            handler.sendMessage(message);
            Log.d("TZ message", "processResults: add");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            URL url = new URL(URL_START + sourceId + URL_END);
            OkHttpClient client=new OkHttpClient();
            Request request = new Request.Builder().url(url) .build();
            Response response= client.newCall(request).execute();
            String message=response.body().string();
            processResults(message);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private ArrayList<Article> parseJSON(String s) {
        articlesList = new ArrayList<>();
        Article a;
        try{
            JSONObject jsonObject = new JSONObject(s);
            JSONArray articles = jsonObject.getJSONArray("articles");
            for (int i = 0; i < (articles.length() > 10 ? 10: articles.length() ); i++) {
                JSONObject jObj = articles.getJSONObject(i);
                String author = jObj.getString("author");
                String title = jObj.getString("title");
                String description = jObj.getString("description");
                String urlToImage = jObj.getString("urlToImage");
                String publishedAt = jObj.getString("publishedAt");

                String url = jObj.getString("url");
                Log.d(TAG, "parseJSON: title" + title);
                a = new Article(author,title,description,urlToImage,publishedAt,url);
                articlesList.add(a);
            }
            return articlesList;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
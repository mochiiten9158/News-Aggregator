package com.example.newsgateway;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;
import java.util.ArrayList;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SourceLoaderRunnable implements Runnable {

    private String URL_PRE = "https://newsapi.org/v2/sources?language=en&country=us&category=";
    private String APIKEY = "&apiKey=9c142167aef840c3ac0ec3fadef0c02e";

    private MainActivity mainActivity;
    private String category;
    private Handler handler;

    private ArrayList<String> categoriesList;
    private ArrayList<Source> sourceList;

    private Source source;
    public SourceLoaderRunnable(MainActivity mainActivity, String category, Handler handler){
        this.mainActivity = mainActivity;
        if(category.equals("all") || category.equals(""))
            this.category = "";
        else
            this.category = category;
        this.handler = handler;
    }

    private void processResults(String s) {
        try{
            Message message = new Message();
            message.what = 1;
            message.obj = new Object[] { parseJSON(s), categoriesList};
            handler.sendMessage(message);
            Log.d("TZ message", "onPostExecute: add");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            URL url = new URL(URL_PRE + category + APIKEY );
            OkHttpClient client=new OkHttpClient();
            Request request = new Request.Builder().url(url) .build();
            Response response= client.newCall(request).execute();
            String message=response.body().string();
            processResults(message);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private ArrayList<Source> parseJSON(String s){
        sourceList = new ArrayList<>();
        categoriesList = new ArrayList<>();
        try{
            JSONObject jsonObject = new JSONObject(s);
            JSONArray sources = jsonObject.getJSONArray("sources");

            for (int i = 0; i < sources.length() ; i++) {
                JSONObject src = sources.getJSONObject(i);
                String id = src.getString("id");
                String url = src.getString("url");
                String name = src.getString("name");
                String category = src.getString("category");

                if(! categoriesList.contains(category)){
                    categoriesList.add(category);
                }
                source = new Source(id,name,url,category);
                sourceList.add(source);
            }
            return sourceList;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
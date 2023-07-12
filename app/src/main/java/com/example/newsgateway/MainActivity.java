/* Shambhawi Sharma
*  A20459117
*  11/29/2022
* */

package com.example.newsgateway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.newsgateway.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_ARTICLES = "ARTICLES";
    private static final String ACTION_SERVICE = "SERVICE";
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private MainActivity ma = this;
    private NewsReceiver nr;

    private MyPageAdapter pa;
    private List <Fragment> fragments = new ArrayList<>();
    private ViewPager pager;
    private Menu mymenu;

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private ArrayList<Article>  articles = new ArrayList<>();
    private ArrayList<String> categories = new ArrayList<>();

    private String currentCategory = "";
    private String currentSource = "";

    private HashMap <String,Source> HashMapSource = new HashMap<>();
    private ArrayList <String> NameListSource = new ArrayList<>();
    private HashMap <String,ArrayList<String>> HashMapCategory = new HashMap<>();

    public class setColorAdapter extends ArrayAdapter{
        public setColorAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = TextView.inflate(getBaseContext(), R.layout.drawer_item, null);
            }

            TextView textView = convertView.findViewById(R.id.text_view);
            Random random = new Random();
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);
            textView.setTextColor(Color.rgb(r,g,b));
            return super.getView(position, convertView, parent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(MainActivity.this, NewsService.class);
        startService(intent);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        nr = new NewsReceiver();

        IntentFilter intentFilter = new IntentFilter(ACTION_ARTICLES);
        registerReceiver(nr, intentFilter);

        drawerLayout = binding.drawerLayout;
        drawerList = binding.drawerList;

        drawerList.setAdapter(new setColorAdapter(this, R.layout.drawer_item, NameListSource));

        drawerList.setOnItemClickListener(
                new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        pager.setBackground(null);

                        String current = NameListSource.get(position);
                        getSupportActionBar().setTitle(current);

                        Intent intentClick = new Intent();
                        intentClick.setAction(ACTION_SERVICE);

                        Source source = HashMapSource.get(NameListSource.get(position));

                        Log.d(TAG, "onItemClick: add selected source id " + source.getId());
                        intentClick.putExtra("sourceID", source.getId());

                        sendBroadcast(intentClick);
                        Log.d(TAG, "onItemClick: broadcast sent");

                        drawerLayout.closeDrawer(drawerList);
                        Log.d(TAG, "onItemClick: Source Clicked: " + NameListSource.get(position));
                    }
                }
        );

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        pa = new MyPageAdapter(getSupportFragmentManager());
        pager = binding.viewpager;
        pager.setAdapter(pa);

        if(savedInstanceState != null) {
            HashMapSource.clear();
            HashMapSource = (HashMap<String, Source>) savedInstanceState.getSerializable("sourceHashMap");
            NameListSource.clear();
            NameListSource = (ArrayList<String>) savedInstanceState.getSerializable("sourceNameList");
            HashMapCategory.clear();
            HashMapCategory = (HashMap<String,ArrayList<String>>) savedInstanceState.getSerializable("categoryHashMap");
            categories = (ArrayList<String>)savedInstanceState.getSerializable("categories");

            Collections.sort(categories);

            if(HashMapSource.size() != 0 ){
                drawerLayout.setBackgroundResource(0);
                drawerList.setAdapter(new setColorAdapter(this, R.layout.drawer_item, NameListSource));
                articles = (ArrayList<Article>)savedInstanceState.getSerializable("articles");
                if(articles != null)
                    addFragment(articles);
            }
            else{
                SourceLoaderRunnable arl = new SourceLoaderRunnable(ma,"",handler);
                new Thread(arl).start();
            }
            pa.notifyDataSetChanged();
            for (int i = 0; i< pa.getCount(); i++) pa.notifyChangeInPosition(i);
        }
        else{
            SourceLoaderRunnable arl = new SourceLoaderRunnable(ma,"",handler);
             new Thread(arl).start();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1) {
                Object[] objs = (Object[]) msg.obj;
                ArrayList<Source> s = (ArrayList<Source>)objs[0];
                ArrayList<String> c = (ArrayList<String>)objs[1];
                getSources(s, c);
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("sourceNameList", NameListSource);
        outState.putSerializable("sourceHashMap", HashMapSource);
        if(articles !=  null)
            outState.putSerializable("articles", articles);
        outState.putSerializable("categories",categories);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(nr);
        Intent intent = new Intent(ma, NewsService.class);
        stopService(intent);
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        mymenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mymenu = menu;
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: drawerToggle " + item);
            return true;
        }

        Log.d(TAG, "onOptionsItemSelected: else, category selected from options menu");
        String category = (String) item.getTitle();
        Log.d(TAG, "onOptionsItemSelected: item.getTitle() is " + category);
        currentCategory = category; // for save instance state

        ArrayList<String> t;
        if(!category.equals("All")){
            t = new ArrayList<>(HashMapCategory.get(category));
        }
        else{
            t = new ArrayList<>();
            for (ArrayList<String> value : HashMapCategory.values()){
                for (String v : value){
                    t.add(v);
                }
            }
        }
        NameListSource.clear();
        NameListSource.addAll(t);

        ((ArrayAdapter) drawerList.getAdapter()).notifyDataSetChanged();

        if (((String) getTitle()).contains("News Gateway")) {
            setTitle("News Gateway (" + NameListSource.size() + ")");
        }

        return super.onOptionsItemSelected(item);
    }

    public void getSources(ArrayList<Source> sources, ArrayList<String> categoriesList) {
        HashMapSource.clear();
        NameListSource.clear();
        HashMapCategory.clear();

        categories.addAll(categoriesList);
        Source source;
        String c;
        String n;
        for (int i = 0; i < sources.size(); i++) {
            source = sources.get(i);
            c = source.getCategory();
            n = source.getName();

            NameListSource.add(n);
            HashMapSource.put(n, source);

            if (!HashMapCategory.containsKey(c) ) {
                HashMapCategory.put(c, new ArrayList<String>());
            }
            HashMapCategory.get(c).add(n);
        }

        Collections.sort(categories);
        mymenu.add("All");
        for (String s : categories) {
            mymenu.add(s);
        }
        ((ArrayAdapter) drawerList.getAdapter()).notifyDataSetChanged();
        setTitle("News Gateway (" + NameListSource.size() + ")");
    }

    public class NewsReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: intent's action is " + intent.getAction());
            switch (intent.getAction()){
                case ACTION_ARTICLES:
                    try{
                        Log.d(TAG, "onReceive: aaaaaaaaaaaaaaaaaa");
                        articles = (ArrayList<Article>) intent.getSerializableExtra("articles");
                        addFragment(articles);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public void addFragment(ArrayList<Article> articleArrayList){
        for(int i = 0; i < pa.getCount(); i++ ){
            pa.notifyChangeInPosition(i);
        }
        if(fragments!=null)
            fragments.clear();
        for (int i = 0; i < articleArrayList.size(); i++) {
            fragments.add(MyFragment.newInstance(ma,articleArrayList.get(i),articleArrayList.size(),i));
        }
        pa.notifyDataSetChanged();
        pager.setCurrentItem(0);
    }

    private class MyPageAdapter extends FragmentPagerAdapter {
        private long b = 0;

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            return b + position;
        }

        public void notifyChangeInPosition(int n) {
            b += getCount() + n;
        }
    }
}
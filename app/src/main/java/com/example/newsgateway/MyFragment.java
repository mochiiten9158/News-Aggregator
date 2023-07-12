package com.example.newsgateway;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class MyFragment extends Fragment implements View.OnClickListener {

    public static MainActivity mainActivity;

    public TextView title_news;
    public TextView date_news;
    public TextView author_news;
    public ImageView image_news;
    public TextView description_news;
    public TextView count_news;

    public Article article;

    public static final MyFragment newInstance(MainActivity ma, Article article, int n, int i){
        mainActivity = ma;
        MyFragment fra = new MyFragment();
        Bundle bd = new Bundle();
        bd.putSerializable("article", article);
        bd.putInt("i",i);
        bd.putInt("n",n);

        fra.setArguments(bd);
        return fra;
    }

    @Override
    public void onClick(View view) {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.myfragment,container,false);
        if (savedInstanceState == null) {
            article = (Article) getArguments().getSerializable("article");
        }
        else {
            article = (Article) savedInstanceState.getSerializable("article");
        }

        int i = getArguments().getInt("i");
        int n = getArguments().getInt("n");

        title_news = view.findViewById(R.id.title);
        date_news = view.findViewById(R.id.date);
        author_news = view.findViewById(R.id.author);
        image_news = view.findViewById(R.id.image);
        description_news = view.findViewById(R.id.description);
        count_news = view.findViewById(R.id.count);

        title_news.setOnClickListener(this);
        image_news.setOnClickListener(this);
        description_news.setOnClickListener(this);

        title_news.setText(article.getTitle());
        author_news.setText(article.getAuthor());

        date_news.setText(article.getPublishedAt());
        date_news = view.findViewById(R.id.date);
        date_news.setText(article.getPublishedAt());

        String formattedDate = "";
        String fPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        String sPattern = "yyyy-MM-dd'T'HH:mm:ss+hh:mm";
        String tPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        if(!article.getPublishedAt().equals("null")) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fPattern);
                formattedDate = new SimpleDateFormat("MMM d, yyyy HH:mm").format(Objects.requireNonNull(simpleDateFormat.parse(article.getPublishedAt())));
            } catch (ParseException e) {
                try {
                    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(sPattern);
                    formattedDate = new SimpleDateFormat("MMM d, yyyy HH:mm").format(Objects.requireNonNull(simpleDateFormat1.parse(article.getPublishedAt())));
                } catch (Exception err) {
                    try {
                        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(tPattern);
                        formattedDate = new SimpleDateFormat("MMM d, yyyy HH:mm").format(Objects.requireNonNull(simpleDateFormat2.parse(article.getPublishedAt())));
                    } catch (Exception er) {
                        date_news.setText(article.getPublishedAt());
                    }
                }
            }
            date_news.setText(formattedDate);
        } else{
            date_news.setText("");
        }

        description_news.setText(article.getDescription());
        count_news.setText((i+1) + " of " + n);

        description_news.setMovementMethod(new ScrollingMovementMethod());
        image_news.setImageResource(R.drawable.loading);

        if(checkNetwork()){
            if(article.getUrlToImage() != null){
                final String photoUrl = (article.getUrlToImage());
                Picasso picasso = new Picasso.Builder(mainActivity).listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        final String changedUrl = photoUrl.replace("http:", "https:");
                        picasso.load(changedUrl).error(R.drawable.brokenimage).placeholder(R.drawable.loading).into(image_news);
                    }
                }).build();
                picasso.load(photoUrl).error(R.drawable.brokenimage).placeholder(R.drawable.loading).into(image_news);
            }
        }

        if(article.getUrl() != null){
            image_news.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = article.getUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
            title_news.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = article.getUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
            description_news.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = article.getUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
        }
        return view;
    }

    private boolean checkNetwork(){
        ConnectivityManager cm = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("article", article);
        super.onSaveInstanceState(outState);
    }
}
package com.asg.florafauna;

import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ScrollView;


/**
 * Created by brada on 3/13/2018.
 */

public class MapActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        final ScrollView sv = (ScrollView) findViewById(R.id.scrollview);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_map);

        final WebView myWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        myWebView.loadUrl("file:///android_asset/map.html");

        myWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        sv.requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        sv.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return myWebView.onTouchEvent(event);
            }
        });
    }

    public void openHelp(View view){
        Intent intent = new Intent(MapActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    public void openSettings(View view){
        Intent intent = new Intent(MapActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openSearch(View view){
        Intent intent = new Intent(MapActivity.this, SearchActivity.class);
        startActivity(intent);
    }

    public void goBack(View view){
        finish();
    }

}

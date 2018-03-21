package com.asg.florafauna;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by brada on 3/13/2018.
 */

public class MapActivity extends AppCompatActivity{

    WebView myWebView;
    String points = "-91.69000244140625 31.219999313 -90.00507354736328 30.337696075439453 -93.58332824707031 32.58332824707031 -89.84539794921875 30.270082473754883";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        final ScrollView sv = (ScrollView) findViewById(R.id.scrollview);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_map);

        myWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);


        myWebView.loadUrl("file:///android_asset/map.html");
        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");

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

    public class WebAppInterface {
        Context mContext;


        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Get the value */
        @JavascriptInterface
        public String getValue() {
            return points;
        }
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

package com.asg.florafauna;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
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
            return bisonpoints;
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

    public void loadAdditionalPoint(View view){
        points = points + " -93.70411682128906 32.44822692871094";
        myWebView.reload();
    }

    String bisonpoints = "";
    public void searchBison(View view){
        bisonpoints = "";
        getPointsFromBison(this);
    }

    public void getPointsFromBison(Context context){
        String url = "https://bison.usgs.gov/api/search.json?species=Mimus%20polyglottos&type=scientific_name&state=Louisiana&countyFips=22015&start=0&count=500";


        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest pointsFromBison = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        try {

                            final JSONArray speciesArray = response.getJSONArray("data");

                            for(int i = 0; i < speciesArray.length(); i++) {
                                String latitude = speciesArray.getJSONObject(i).getString("decimalLatitude");
                                String longitude = speciesArray.getJSONObject(i).getString("decimalLongitude");
                                bisonpoints = bisonpoints + longitude + " " + latitude + " ";
                                Log.d("latitude", bisonpoints.length() + "a");
                            }

                            Log.d("latitude", bisonpoints.length() + "a");
                            bisonpoints = bisonpoints.substring(0, bisonpoints.length() - 1);
                            myWebView.reload();

                        }
                        catch (JSONException error) {
                            Log.e("searchResponseException", error.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", error.toString());

            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(pointsFromBison);

        //bisonpoints = bisonpoints.substring(0, bisonpoints.length() - 1);
        //Log.d("bisonpoints", bisonpoints.length() + "a");

        //myWebView.reload();
    }


}

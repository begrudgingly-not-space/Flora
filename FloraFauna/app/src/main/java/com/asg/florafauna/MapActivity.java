package com.asg.florafauna;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;

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

import static com.asg.florafauna.CountyFinder.countyFinder;
import static com.asg.florafauna.StateFinder.stateFinder;


/**
 * Created by brada on 3/13/2018.
 */

public class MapActivity extends AppCompatActivity{

    WebView myWebView;
    String points = "-91.69000244140625 31.219999313 -90.00507354736328 30.337696075439453 -93.58332824707031 32.58332824707031 -89.84539794921875 30.270082473754883";
    private EditText speciesInput;
    private EditText locationInput;
    private ProgressDialog dialog;
    private InputMethodManager imm;

    private double mapLongitude = -96.9583498;   
    private double mapLatitude = 40.7507204;
    private double mapZoom = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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

        speciesInput = findViewById(R.id.SearchEditText);
        locationInput = findViewById(R.id.SearchEditTextRegion);

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

        @JavascriptInterface
        public double getMapLongitude() {
            return mapLongitude;
        }

        @JavascriptInterface
        public double getMapLatitude() {
            return mapLatitude;
        }

        @JavascriptInterface
        public double getMapZoom() {
            return mapZoom;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settings_intent = new Intent(MapActivity.this, SettingsActivity.class);
                startActivity(settings_intent);
                return true;
            case R.id.action_help:
                Intent help_intent = new Intent(MapActivity.this, HelpActivity.class);
                startActivity(help_intent);
                return true;
            case R.id.action_home:
                Intent intent = new Intent(MapActivity.this, SearchActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*public void openHelp(View view){
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
    }*/

    public void goBack(View view){
        finish();
    }

    public void loadAdditionalPoint(View view){
        points = points + " -93.70411682128906 32.44822692871094";
        myWebView.reload();
    }

    String bisonpoints = "";
    public void sightingsSearch(View view){
        bisonpoints = "";
        //getPointsFromBison(this);

        dialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);

        String species = speciesInput.getText().toString();
        String location = locationInput.getText().toString();

        Spinner spinner = findViewById(R.id.search_selection);
        String selection = spinner.getSelectedItem().toString();
        Log.d("spinner", selection);

        String searchType = "";

        if (selection.equals("Common name by state")){
            Log.d("selection", "common state search");
            searchType = "common_name";
            sightingsByState(this, searchType, species, location);
        }
        else if (selection.equals("Common name by county")){
            Log.d("selection", "common county search");
            searchType = "common_name";
            sightingsByCounty(this, searchType, species, location);
        }
        else if (selection.equals("Scientific name by state")){
            Log.d("selection","scientific state search");
            searchType = "scientific_name";
            sightingsByState(this, searchType, species, location);
        }
        else {
            Log.d("selection", "scientific county search");
            searchType = "scientific_name";
            sightingsByCounty(this, searchType, species, location);
        }

    }

    public void sightingsByState(Context context, String searchType, String species, String location){
        String state = "";

        if (location.length() > 2) {
            state = location.substring(0, 1).toUpperCase() + location.substring(1);
            state = state.replaceAll(" ", "%20");
        }
        else if (location.length() == 2) {
            state = location.substring(0,2).toUpperCase();
            state = stateFinder(context, state);
        }

        species = species.replaceAll(" ", "%20");

        String url = "https://bison.usgs.gov/api/search.json?species=" + species + "&type=" + searchType + "&state=" + state + "&start=0&count=500";
        Log.d("url", url);
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest pointsFromBison = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();

                        try {

                            final JSONArray speciesArray = response.getJSONArray("data");

                            for(int i = 0; i < speciesArray.length(); i++) {
                                String latitude = speciesArray.getJSONObject(i).getString("decimalLatitude");
                                String longitude = speciesArray.getJSONObject(i).getString("decimalLongitude");
                                bisonpoints = bisonpoints + longitude + " " + latitude + " ";
                                Log.d("latitude", bisonpoints.length() + "a");
                            }

                            Log.d("latitude", bisonpoints.length() + "a");
                            if (bisonpoints.length() > 0) {
                                bisonpoints = bisonpoints.substring(0, bisonpoints.length() - 1);
                            }
                            myWebView.reload();

                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                        }
                        catch (JSONException error) {
                            Log.e("searchResponseException", error.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", error.toString());
                dialog.dismiss();
                AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();
                alertDialog.setTitle("Invalid State or Species");
                alertDialog.setMessage("Please input the full name of a species in the first box and a state in the other box.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface alertDialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();

            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(pointsFromBison);

        //bisonpoints = bisonpoints.substring(0, bisonpoints.length() - 1);
        //Log.d("bisonpoints", bisonpoints.length() + "a");

        //myWebView.reload();
    }

    public void sightingsByCounty(Context context, String searchType, String species, String location){
        String searchTerms[];
        String county = "";
        String state = "";

        if (location.contains(",") && location.length() >= 3) {
            searchTerms = location.split(",");

            if (searchTerms.length == 2) {
                county = searchTerms[0];
                state = searchTerms[1];
            }
        }

        if (state.length() > 3) {
            state = state.substring(1, 2).toUpperCase() + state.substring(2);
        }
        else if (state.length() == 3) {
            state = state.substring(1,3).toUpperCase();
            state = stateFinder(context, state);
        }

        String countyFips = countyFinder(context, state, county);

        species = species.replaceAll(" ", "%20");

        state = state.replaceAll(" ", "%20");

        String url = "https://bison.usgs.gov/api/search.json?species=" + species + "&type=" + searchType + "&state=" + state + "&countyFips=" + countyFips + "&start=0&count=500";

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest pointsFromBison = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();

                        try {

                            final JSONArray speciesArray = response.getJSONArray("data");

                            for(int i = 0; i < speciesArray.length(); i++) {
                                String latitude = speciesArray.getJSONObject(i).getString("decimalLatitude");
                                String longitude = speciesArray.getJSONObject(i).getString("decimalLongitude");
                                bisonpoints = bisonpoints + longitude + " " + latitude + " ";
                                Log.d("latitude", bisonpoints.length() + "a");
                            }

                            Log.d("latitude", bisonpoints.length() + "a");
                            if(bisonpoints.length() > 0) {
                                bisonpoints = bisonpoints.substring(0, bisonpoints.length() - 1);
                            }
                            myWebView.reload();

                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                        }
                        catch (JSONException error) {
                            Log.e("searchResponseException", error.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", error.toString());
                dialog.dismiss();
                AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();
                alertDialog.setTitle("Invalid County or Species");
                alertDialog.setMessage("Please input the full name of a species in the first box and a county, state in the other box.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface alertDialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();

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

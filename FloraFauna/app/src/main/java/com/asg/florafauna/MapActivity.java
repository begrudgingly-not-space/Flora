package com.asg.florafauna;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static com.asg.florafauna.CountyFinder.countyFinder;
import static com.asg.florafauna.SearchActivity.setAOIBbox;
import static com.asg.florafauna.StateFinder.stateFinder;

/**
 * Created by brada on 3/13/2018.
 */

public class MapActivity extends AppCompatActivity implements LocationListener{
    private static final String TAG = "MapActivity";
    WebView myWebView;
    private EditText speciesInput, locationInput, speciesInputCounty, stateInputForCounty, countyInputForCounty;
    private LinearLayout countyInput, stateInput;
    private Spinner spinner;
    private ProgressDialog dialog;
    private InputMethodManager imm;
    private Map<String, double[]> stateLocations = new HashMap<>();

    // Variables for nearby sightings
    private String locationPolygon;
    private double latitude;
    private double longitude;
    private double mileage;
    private String[] mileageArray = new String[1];
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_COARSE_LOCATION = 0;

    String points = "-91.69000244140625 31.219999313 -90.00507354736328 30.337696075439453 -93.58332824707031 32.58332824707031 -89.84539794921875 30.270082473754883";
    private double mapLongitude = -96.9583498;
    private double mapLatitude = 40.7507204;
    private double mapZoom = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeCreator.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        if (getSupportActionBar() != null) {
            FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_map);
        }

        // SSL Certification for webcalls
        if (BuildConfig.DEBUG) {
            SSLCertificates.trustAll();
        }

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        final ScrollView sv = findViewById(R.id.scrollview);

        myWebView = findViewById(R.id.webview);
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

        speciesInputCounty = findViewById(R.id.speciesInput);
        stateInputForCounty = findViewById(R.id.stateInput);
        countyInputForCounty = findViewById(R.id.countyInput);

        countyInput = findViewById(R.id.CountySearch);
        stateInput = findViewById(R.id.StateSearch);

        stateInputForCounty.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    sightingsSearch(v);
                }
                return false;
            }
        });

        locationInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    sightingsSearch(v);
                }
                return false;
            }
        });

        spinner = findViewById(R.id.search_selection);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Common name by state"))
                {
                    speciesInput.setHint("Common name");
                    locationInput.setHint("State");
                    countyInput.setVisibility(View.INVISIBLE);
                    stateInput.setVisibility(View.VISIBLE);
                    locationInput.setVisibility(View.VISIBLE);
                }
                else if (selectedItem.equals("Common name by county")){
                    speciesInputCounty.setHint("Common name");
                    stateInputForCounty.setHint("State");
                    countyInputForCounty.setHint("County");
                    countyInput.setVisibility(View.VISIBLE);
                    stateInput.setVisibility(View.INVISIBLE);
                    locationInput.setVisibility(View.INVISIBLE);
                }
                else if (selectedItem.equals("Scientific name by state")){
                    speciesInput.setHint("Scientific name");
                    locationInput.setHint("State");
                    countyInput.setVisibility(View.INVISIBLE);
                    stateInput.setVisibility(View.VISIBLE);
                    locationInput.setVisibility(View.VISIBLE);
                }
                else if (selectedItem.equals("Scientific name by county")){
                    speciesInputCounty.setHint("Scientific name");
                    stateInputForCounty.setHint("State");
                    countyInputForCounty.setHint("County");
                    countyInput.setVisibility(View.VISIBLE);
                    stateInput.setVisibility(View.INVISIBLE);
                    locationInput.setVisibility(View.INVISIBLE);
                }
                else if (selectedItem.equals("Nearby sightings (common name)")){
                    speciesInput.setHint("Common name");
                    locationInput.setHint("State");
                    countyInput.setVisibility(View.INVISIBLE);
                    stateInput.setVisibility(View.VISIBLE);
                    locationInput.setVisibility(View.GONE);
                }
                else {
                    speciesInput.setHint("Scientific name");
                    locationInput.setHint("State");
                    countyInput.setVisibility(View.INVISIBLE);
                    stateInput.setVisibility(View.VISIBLE);
                    locationInput.setVisibility(View.GONE);
                }
            } // To close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        stateLocations.put("Louisiana", new double[] { -92.600726, 31.314196, 5});
        stateLocations.put("Alabama", new double[] {-86.791130, 32.806671, 5});
        stateLocations.put("Alaska", new double[] {-152.404419, 61.370716, 2});
        stateLocations.put("Arizona", new double[] {-111.431221, 33.729759, 5});
        stateLocations.put("Arkansas", new double[] {-92.373123, 34.969704, 5});
        stateLocations.put("California", new double[] {-119.681564, 36.116203, 4});
        stateLocations.put("Colorado", new double[] {-105.311104, 39.059811, 5});
        stateLocations.put("Connecticut", new double[] {-72.755371, 41.597782, 5});
        stateLocations.put("Delaware", new double[] {-75.507141, 39.318523, 5});
        stateLocations.put("Florida", new double[] {-81.686783, 27.766279, 4});
        stateLocations.put("Georgia", new double[] {-83.643074, 33.040619, 5});
        stateLocations.put("Hawaii", new double[] {-157.498337, 21.094318, 5});
        stateLocations.put("Idaho", new double[] {-114.478828, 44.240459, 4});
        stateLocations.put("Illinois", new double[] {-88.986137, 40.349457, 5});
        stateLocations.put("Indiana", new double[] {-86.258278, 39.849426, 5});
        stateLocations.put("Iowa", new double[] {-93.210526, 42.011539, 5});
        stateLocations.put("Kansas", new double[] {-96.726486, 38.526600, 5});
        stateLocations.put("Kentucky", new double[] {-84.670067, 37.668140, 5});
        stateLocations.put("Maine", new double[] {-69.381927, 44.693947, 5});
        stateLocations.put("Maryland", new double[] {-76.802101, 39.063946, 5});
        stateLocations.put("Massachusetts", new double[] {-71.530106, 42.230171, 5});
        stateLocations.put("Michigan", new double[] {-84.536095, 43.326618, 4});
        stateLocations.put("Minnesota", new double[] {-93.900192, 45.694454, 4});
        stateLocations.put("Mississippi", new double[] {-89.678696, 32.741646, 5});
        stateLocations.put("Missouri", new double[] {-92.288368, 38.456085, 5});
        stateLocations.put("Montana", new double[] {-110.454353, 46.921925, 4});
        stateLocations.put("Nebraska", new double[] {-98.268082, 41.125370, 5});
        stateLocations.put("Nevada", new double[] {-117.055374, 38.313515, 4});
        stateLocations.put("New Hampshire", new double[] {-71.563896, 43.452492, 5});
        stateLocations.put("New Jersey", new double[] {-74.521011, 40.298904, 5});
        stateLocations.put("New Mexico", new double[] {-106.248482, 34.840515, 4});
        stateLocations.put("New York", new double[] {-74.948051, 42.165726, 4});
        stateLocations.put("North Carolina", new double[] {-79.806419, 35.630066, 5});
        stateLocations.put("North Dakota", new double[] {-99.784012, 47.528912, 5});
        stateLocations.put("Ohio", new double[] {-82.764915, 40.388783, 5});
        stateLocations.put("Oklahoma", new double[] {-96.928917, 35.565342, 5});
        stateLocations.put("Oregon", new double[] {-122.070938, 44.572021, 4});
        stateLocations.put("Pennsylvania", new double[] {-77.209755, 40.590752, 5});
        stateLocations.put("Rhode Island", new double[] {-71.511780, 41.680893, 6});
        stateLocations.put("South Carolina", new double[] {-80.945007, 33.856892, 5});
        stateLocations.put("South Dakota", new double[] {-99.438828, 44.299782, 5});
        stateLocations.put("Tennessee", new double[] {-86.692345, 35.747845, 5});
        stateLocations.put("Texas", new double[] {-97.563461, 31.054487, 4});
        stateLocations.put("Utah", new double[] {-111.862434, 40.150032, 5});
        stateLocations.put("Vermont", new double[] {-72.710686, 44.045876, 5});
        stateLocations.put("Virginia", new double[] {-78.169968, 37.769337, 5});
        stateLocations.put("Washington", new double[] {-121.490494, 47.400902, 5});
        stateLocations.put("West Virginia", new double[] {-80.954453, 38.491226, 5});
        stateLocations.put("Wisconsin", new double[] {-89.616508, 44.268543, 4});
        stateLocations.put("Wyoming", new double[] {-107.302490, 42.755966, 5});

        // onCreate methods needed for nearby sightings
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_COARSE_LOCATION);
        }
        else {
            getLocation();
            getMileage();
        }
    }

    public class WebAppInterface {
        Context mContext;

        // Instantiate the interface and set the context
        WebAppInterface(Context c) {
            mContext = c;
        }

        // Get the value
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
                Intent home_intent = new Intent(MapActivity.this, SearchActivity.class);
                startActivity(home_intent);
                return true;
            case R.id.action_recording:
                Intent recording_intent = new Intent(MapActivity.this, PersonalRecordingsActivity.class);
                startActivity(recording_intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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
        mapLongitude = -96.9583498;
        mapLatitude = 40.7507204;
        mapZoom = 2;

        dialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);

        String selection = spinner.getSelectedItem().toString();
        Log.d("spinner", selection);

        String speciesCountySearch = speciesInputCounty.getText().toString();
        String stateCountySearch = stateInputForCounty.getText().toString();
        String county = countyInputForCounty.getText().toString();

        String speciesStateSearch = speciesInput.getText().toString();
        String stateStateSearch = locationInput.getText().toString();

        String searchType = "";

        if (selection.equals("Common name by state")){
            Log.d("selection", "common state search");
            searchType = "common_name";
            sightingsByState(this, searchType, speciesStateSearch, stateStateSearch);
        }
        else if (selection.equals("Common name by county")){
            Log.d("selection", "common county search");
            searchType = "common_name";
            sightingsByCounty(this, searchType, speciesCountySearch, stateCountySearch, county);
        }
        else if (selection.equals("Scientific name by state")){
            Log.d("selection","scientific state search");
            searchType = "scientific_name";
            sightingsByState(this, searchType, speciesStateSearch, stateStateSearch);
        }
        else if (selection.equals("Scientific name by county")) {
            Log.d("selection", "scientific county search");
            searchType = "scientific_name";
            sightingsByCounty(this, searchType, speciesCountySearch, stateCountySearch, county);
        }
        else if (selection.equals("Nearby sightings (common name)")){
            Log.d("selection","nearby common");
            searchType = "common_name";
            latitude = 32.5261848;
            longitude = -92.6447334;
            if (latitude != 0 && longitude != 0) {
                double mileage = Double.parseDouble(mileageArray[0]);
                locationPolygon = setAOIBbox(latitude, longitude, mileage);
                Log.d(TAG, locationPolygon);
                nearbySightings(this, searchType, speciesStateSearch, locationPolygon);
            }
            else {
                // Error message
                dialog.dismiss();
                Log.e(TAG, "Location not found");
                AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();
                alertDialog.setTitle("Location not found");
                alertDialog.setMessage("The application may not be able to locate you.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface alertDialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }
        else {
            Log.d("selection", "nearby scientific");
            searchType = "scientific_name";
            latitude = 32.5261848;
            longitude = -92.6447334;
            if (latitude != 0 && longitude != 0) {
                double mileage = Double.parseDouble(mileageArray[0]);
                locationPolygon = setAOIBbox(latitude, longitude, mileage);
                Log.d(TAG, locationPolygon);
                nearbySightings(this, searchType, speciesStateSearch, locationPolygon);
            }
            else {
                // Error message
                dialog.dismiss();
                Log.e(TAG, "Location not found");
                AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();
                alertDialog.setTitle("Location not found");
                alertDialog.setMessage("The application may not be able to locate you.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface alertDialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }

    }

    public void sightingsByState(Context context, String searchType, String species, String state){
        AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();

        if (state.equals("") && species.equals("")){
            alertDialog.setTitle("No species nor state entered");
            alertDialog.setMessage("Please enter a common name or scientific name in the first search box and a state in the second search box.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }
        else if (species.equals("")){
            alertDialog.setTitle("No species entered");
            alertDialog.setMessage("Please enter a common name or scientific name in the first search box.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }
        else if (state.equals("")){
            alertDialog.setTitle("No state entered");
            alertDialog.setMessage("Please enter a state in the second search box.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }

        if (state.length() > 2) {
            state = state.substring(0, 1).toUpperCase() + state.substring(1);

            if (state.contains(" ")){
                String[] stateParts = state.split(" ");
                stateParts[1] = stateParts[1].substring(0,1).toUpperCase() + stateParts[1].substring(1);
                state = stateParts[0] + " " + stateParts[1];
            }

        }
        else if (state.length() == 2) {
            state = state.substring(0,2).toUpperCase();
            state = stateFinder(context, state);
        }

        double[] mapValues = stateLocations.get(state);
        if (mapValues != null){
            mapLongitude = mapValues[0];
            mapLatitude = mapValues[1];
            mapZoom = mapValues[2];
        }

        state = state.replaceAll(" ", "%20");
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
                            else {
                                AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();
                                alertDialog.setTitle("No results found");
                                alertDialog.setMessage("No observations of the entered species in this state");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface alertDialog, int which) {
                                                alertDialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
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

                if (error instanceof NoConnectionError){
                    alertDialog.setTitle("No internet connection");
                    alertDialog.setMessage("Device must be connected to internet to retrieve results.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else if (error instanceof TimeoutError) {
                    alertDialog.setTitle("Cannot connect to BISON servers at this time.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else {
                    alertDialog.setTitle("Invalid state or species");
                    alertDialog.setMessage("Please enter a valid species in the first box and a state in the second search box.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }

            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(pointsFromBison);
    }

    public void sightingsByCounty(Context context, String searchType, String species, String state, String county) {
        AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();

        if (species.equals("") && state.equals("") && county.equals("")) {
            alertDialog.setTitle("No species, state, nor county entered");
            alertDialog.setMessage("Please enter a common name or scientific name in the first search box, a state in the search box labelled state, and a county in the search box labelled county.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }
        else if (species.equals("") && state.equals("")){
            alertDialog.setTitle("No species nor state entered");
            alertDialog.setMessage("Please enter a common name or scientific name in the first search box and a state in the search box labelled state.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }
        else if (species.equals("") && county.equals("")){
            alertDialog.setTitle("No species nor county entered");
            alertDialog.setMessage("Please enter a common name or scientific name in the first search box and a county in the search box labelled county.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }
        else if (state.equals("") && county.equals("")){
            alertDialog.setTitle("No state nor county entered");
            alertDialog.setMessage("Please enter a state in the search box labelled state and a county in the search box labelled county.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }
        else if (species.equals("")){
            alertDialog.setTitle("No species entered");
            alertDialog.setMessage("Please enter a common name or scientific name in the first search box.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }
        else if (state.equals("")){
            alertDialog.setTitle("No state entered");
            alertDialog.setMessage("Please enter a state in the search box labelled state.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }
        else if (county.equals("")){
            alertDialog.setTitle("No county entered");
            alertDialog.setMessage("Please enter a county in the search box labelled county.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }


        if (state.length() > 2) {
            state = state.substring(0, 1).toUpperCase() + state.substring(1);

            if (state.contains(" ")){
                String[] stateParts = state.split(" ");
                stateParts[1] = stateParts[1].substring(0,1).toUpperCase() + stateParts[1].substring(1);
                state = stateParts[0] + " " + stateParts[1];
            }

        }
        else if (state.length() == 2) {
            state = state.substring(0,2).toUpperCase();
            state = stateFinder(context, state);
        }

        // Capitalizes first char in county
        if (county.length() > 0){
            county = county.substring(0, 1).toUpperCase() + county.substring(1);

            if (county.contains(" ") && county.length() > 2){
                String[] countyParts = county.split(" ");
                countyParts[1] = countyParts[1].substring(0,1).toUpperCase() + countyParts[1].substring(1);
                county = countyParts[0] + " " + countyParts[1];
            }

        }

        String countyFips = countyFinder(context, state, county);

        double[] mapValues = stateLocations.get(state);
        if (mapValues != null){
            mapLongitude = mapValues[0];
            mapLatitude = mapValues[1];
            mapZoom = mapValues[2];
        }

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
                            else {
                                AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();
                                alertDialog.setTitle("No results found");
                                alertDialog.setMessage("No observations of the entered species in this county");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface alertDialog, int which) {
                                                alertDialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
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

                if (error instanceof NoConnectionError){
                    alertDialog.setTitle("No internet connection");
                    alertDialog.setMessage("Device must be connected to internet to retrieve results.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else if (error instanceof TimeoutError) {
                    alertDialog.setTitle("Cannot connect to BISON servers at this time.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else {
                    alertDialog.setTitle("Invalid county or species");
                    alertDialog.setMessage("Please enter a valid species and county and state combination.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }

            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(pointsFromBison);
    }

    // Nearby sightings method
    public void nearbySightings(Context context, String searchType, String species, String polygon){
        AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();

        if (species.equals("")){
            alertDialog.setTitle("No species entered");
            alertDialog.setMessage("Please enter a common name or scientific name in the search box.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface alertDialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
            dialog.dismiss();
            return;
        }

        // Converts min & max points from polygon to double
        String[] polygonPoints = polygon.split(",");
        Double[] polygonPointsVal = new Double[4];
        for(int i = 0; i < polygonPoints.length; i++){
            polygonPointsVal[i] = Double.parseDouble(polygonPoints[i]);
        }

        // Averages min & max and sets the average as the center of the map
        mapLongitude = (polygonPointsVal[0] + polygonPointsVal[2]) / 2;
        mapLatitude = (polygonPointsVal[1] + polygonPointsVal[3]) / 2;
        mapZoom = 9;

        species = species.replaceAll(" ", "%20");

        String url = "https://bison.usgs.gov/api/search.json?aoibbox=" + polygon + "&species=" + species + "&type=" + searchType + "&start=0&count=500";
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
                            if(bisonpoints.length() > 0) {
                                bisonpoints = bisonpoints.substring(0, bisonpoints.length() - 1);
                            }
                            else {
                                AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this).create();
                                alertDialog.setTitle("No results found");
                                alertDialog.setMessage("No observations of the entered species in this location.");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface alertDialog, int which) {
                                                alertDialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                            myWebView.reload();

                            //imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                            imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
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

                if (error instanceof NoConnectionError){
                    alertDialog.setTitle("No internet connection");
                    alertDialog.setMessage("Device must be connected to internet to retrieve results.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else if (error instanceof TimeoutError) {
                    alertDialog.setTitle("Cannot connect to BISON servers at this time.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else {
                    alertDialog.setTitle("Invalid species");
                    alertDialog.setMessage("Please enter a valid species in the search box.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }

            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(pointsFromBison);
    }

    // Methods needed for nearby sightings
    private void getMileage() {
        try {
            // Opens the file to read its contents
            FileInputStream fis = this.openFileInput("mileage");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            mileageArray[0] = reader.readLine(); // Adds the line to the mileageArray
            reader.close();
            isr.close();
            fis.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            mileageArray[0] = "1";
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        mileage = Double.parseDouble(mileageArray[0]);
    }

    public void getLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5,this);
        }
        catch (SecurityException e) {
            Log.e("LocationSecurityExc", e.toString());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.d (TAG, "Latitude = " + latitude + "\n Longitude = " + longitude);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }
}

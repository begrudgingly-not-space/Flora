package com.asg.florafauna;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * Created by kkey on 2/1/2018.
 */

public class SearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private ListView speciesListView;
    private InputMethodManager imm;
    private ProgressDialog dialog;
    private LocationManager locationManager;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); // Enable hiding/showing keyboard

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED/* && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {
        // Register the listener with the Location Manager to receive location updates

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Dialog to explain why app wants permission
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
        else {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            // Define a listener that responds to location updates
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    Toast.makeText(SearchActivity.this, Double.toString(location.getLatitude()) + Double.toString(location.getLongitude()), Toast.LENGTH_SHORT).show();
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };
        }
        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_search);

        searchEditText = findViewById(R.id.SearchEditText);
        speciesListView = findViewById(R.id.ListSpecies);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                }
                else {
                    // Permission denied, What's Around Me functionality disabled
                }
            }
        }
    }

    public void openHelp(View view){
        Intent intent = new Intent(SearchActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    // opens settings
    public void openSettings(View view){
        Intent intent = new Intent(SearchActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed()
    {
        // Disables going back by manually pressing the back button
    }

    public void search(View view) {
        String searchInput = searchEditText.getText().toString();

        dialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);
        searchRequest(this, searchInput);
        //searchRequestWithSpecies(this, searchInput);
        //searchRequestWithCounty(this, "Louisiana", "22015");
        //String searchOutput = makeWebCall(searchInput);
    }

    private void searchRequest(final Context context, final String state) {
        // stateInput capitalizes the state
        // Bison produces an error if you input a state in all lowercase letters
        String stateInput = state.substring(0, 1).toUpperCase() + state.substring(1);
        final String url = "https://bison.usgs.gov/api/search.json?state=" + stateInput + "&start=0&count=500";
        Log.d("url", url);

        // Initialize request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        ArrayList<String> scientificNamesArray = new ArrayList<String>();

                        try {
                            JSONArray speciesArray = response.getJSONArray("data");

                            for(int i = 0; i < speciesArray.length(); i++) {
                                String currentScientificName = speciesArray.getJSONObject(i).getString("name");

                                if (!scientificNamesArray.contains(currentScientificName)) {
                                    scientificNamesArray.add(currentScientificName);
                                }
                            }

                            Log.d("searchResponse", scientificNamesArray.toString());

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, scientificNamesArray);

                            speciesListView.setAdapter(adapter);
                            speciesListView.setVisibility(View.VISIBLE);

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
                AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                alertDialog.setTitle("Invalid State");
                alertDialog.setMessage("Please input the full name of a state.");
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
        requestQueue.add(searchRequest);
    }

    private void searchRequestWithCounty(final Context context, final String state, final String countyFips) {
        final String url = "https://bison.usgs.gov/api/search.json?state=" + state + "&countyFips=" + countyFips + "&start=0&count=500";
        Log.d("url", url);

        // Initialize request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<String> scientificNamesArray = new ArrayList<String>();

                        try {
                            JSONArray speciesArray = response.getJSONArray("data");

                            for(int i = 0; i < speciesArray.length(); i++) {
                                String currentScientificName = speciesArray.getJSONObject(i).getString("name");

                                if (!scientificNamesArray.contains(currentScientificName)) {
                                    scientificNamesArray.add(currentScientificName);
                                }
                            }

                            Log.d("searchRespWithCounty", scientificNamesArray.toString());

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, scientificNamesArray);

                            speciesListView.setAdapter(adapter);
                            speciesListView.setVisibility(View.VISIBLE);

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
            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(searchRequest);
    }

    private void searchRequestWithSpecies(final Context context, final String speciesName)
    {
        // base address for searching for a species
        String baseAddress = "https://www.itis.gov/ITISWebService/services/ITISService/searchForAnyMatch?srchKey=";
        // url for searching
        String formattedName = speciesName.replaceAll(" ", "%20");
        final String query = baseAddress + formattedName;

        Log.i("final url:", query);

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // set up a StringRequest object for catching XML
        StringRequest stringRequest = new StringRequest(Request.Method.GET, query, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                // closes the loading, please wait dialog
                dialog.dismiss();

                try
                {
                    Log.i("response: ", response);

                    // create a DocumentBuilderFactory to grab a DocumentBuilder
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();

                    // create a Document from the DocumentBuilder to access a parser, then
                    // parse the results to access the XML data
                    Document doc = builder.parse(new InputSource(new StringReader(response)));

                    // grab the common names from the data -- get the first (most relevant)
                    NodeList commonNameList = doc.getElementsByTagName("ax23:commonName");
                    String commonName = commonNameList.item(0).getTextContent();
                    NodeList scientificNameList = doc.getElementsByTagName("ax23:sciName");
                    String scientificName = scientificNameList.item(0).getTextContent();

                    // concatenate the common name and scientific name to display both
                    String speciesArr[] = {commonName + ", " + scientificName};
                    Log.i("species name: ", speciesArr[0]);

                    // throw species name in ListView and display
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, speciesArr);
                    speciesListView.setAdapter(adapter);
                    speciesListView.setVisibility(View.VISIBLE);

                    // hide the keyboard
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                }
                catch(Exception exception)
                {
                    Log.e("Couldn't grab xml", exception.toString());
                }
            }
        }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e("Error: ", error.toString());
                    dialog.dismiss();
                }
            });

        requestQueue.add(stringRequest);
    }

    public void whatsAroundMe(View view) {
        String polygon = "-111.31079356054,38.814339278134,-110.57470957617,39.215537729772";
        dialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);
        whatsAroundMeRequest(this, polygon);
    }

    private void whatsAroundMeRequest(final Context context, final String polygon)
    {
        final String url = "https://bison.usgs.gov/api/search.json?aoibbox=" + polygon;
        Log.d("url", url);

        // Initialize request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest whatsAroundMeRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        ArrayList<String> scientificNamesArray = new ArrayList<String>();

                        try {
                            JSONArray speciesArray = response.getJSONArray("data");

                            for(int i = 0; i < speciesArray.length(); i++) {
                                String currentScientificName = speciesArray.getJSONObject(i).getString("name");

                                if (!scientificNamesArray.contains(currentScientificName)) {
                                    scientificNamesArray.add(currentScientificName);
                                }
                            }

                            Log.d("whatsAroundMeResponse", scientificNamesArray.toString());

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, scientificNamesArray);

                            speciesListView.setAdapter(adapter);
                            speciesListView.setVisibility(View.VISIBLE);

                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        }
                        catch (JSONException error) {
                            Log.e("whatsAroundMeRespExcept", error.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", error.toString());
                dialog.dismiss();
                AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                alertDialog.setTitle("What's Around Me Error Description");
                alertDialog.setMessage(error.toString());
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
        requestQueue.add(whatsAroundMeRequest);
    }

}
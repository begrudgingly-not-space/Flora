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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import android.widget.RadioButton;
import android.widget.Toast;

import static com.asg.florafauna.CountyFinder.countyFinder;


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
    private int searchType = 0;
    public static final String INTENT_EXTRA_SPECIES_NAME = "speciesName";


    private int offset = 0;

    private ArrayList<String> scientificNamesArray = new ArrayList<String>();


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

        //setup for load more button
        Button btnLoadMore = new Button(this);
        btnLoadMore.setText("Load More");
        speciesListView.addFooterView(btnLoadMore);

        //listener for load more button
        btnLoadMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (searchType != 0) {
                    loadMore(searchType);
                }

            }
        });
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

    //SEARCH FUNCTIONS
    public void search(View view) {
        String searchInput = searchEditText.getText().toString();
        scientificNamesArray = new ArrayList<String>();
        offset = 0;

        dialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);
        //create links to radio buttons
        RadioButton Scientific = findViewById(R.id.SpeciesButton);
        RadioButton County = findViewById(R.id.CountyButton);
        RadioButton State = findViewById(R.id.StateButton);

        if(State.isChecked())
        {
            // Search by State
            searchRequestWithState(this, searchInput);
        }
        else if(Scientific.isChecked())
        {
            // Search by Species/common name
            searchRequestWithSpecies(this, searchInput);
        }
        else if(County.isChecked())
        {
            // Search by County
            searchRequestWithCounty(this, searchInput);
        }
        }

    private void searchRequestWithState(final Context context, final String state) {
        // stateInput capitalizes the state
        // Bison produces an error if you input a state in all lowercase letters
        final int position = scientificNamesArray.size();

        String stateInput = state.substring(0, 1).toUpperCase() + state.substring(1);
        stateInput = stateInput.replaceAll(" ","%20");

        final String url = "https://bison.usgs.gov/api/search.json?state=" + stateInput + "&start=" + offset + "&count=500";
        Log.d("url", url);

        // Initialize request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest searchRequestWithState = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        //ArrayList<String> scientificNamesArray = new ArrayList<String>();

                        try {
                            searchType = 2;
                            final JSONArray speciesArray = response.getJSONArray("data");

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
                            speciesListView.setSelection(position);

                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                            speciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                            {
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                {
                                    String speciesName = speciesListView.getItemAtPosition(position).toString();
                                    Intent intent = new Intent(SearchActivity.this, SpeciesInfoActivity.class);
                                    intent.putExtra(INTENT_EXTRA_SPECIES_NAME, speciesName);
                                    startActivity(intent);
                                }
                            });
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
        requestQueue.add(searchRequestWithState);
    }

    private void searchRequestWithCounty(final Context context, final String searchInput) {
        final int position = scientificNamesArray.size();

        String searchTerms[];
        String county = "";
        String state = "";

        if (searchInput.contains(",") && searchInput.length() >= 3) {
            searchTerms = searchInput.split(",");

            if (searchTerms.length == 2) {
                county = searchTerms[0];
                state = searchTerms[1];
            }
        }

        if (state.length() >= 2) {
            state = state.substring(1, 2).toUpperCase() + state.substring(2);
        }

        String countyFips = countyFinder(context, state, county);

        Log.d("searchterms", county);
        Log.d("searchterms", state);

        state = state.replaceAll(" ", "%20");

        final String url = "https://bison.usgs.gov/api/search.json?state=" + state + "&countyFips=" + countyFips + "&start=" + offset + "&count=500";
        Log.d("url", url);

        // Initialize request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        //ArrayList<String> scientificNamesArray = new ArrayList<String>();

                        try {
                            searchType = 1;
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
                            speciesListView.setSelection(position);

                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                            speciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                            {
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                {
                                    String speciesName = speciesListView.getItemAtPosition(position).toString();
                                    Intent intent = new Intent(SearchActivity.this, SpeciesInfoActivity.class);
                                    intent.putExtra(INTENT_EXTRA_SPECIES_NAME, speciesName);
                                    startActivity(intent);
                                }
                            });
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
                alertDialog.setTitle("Invalid County, State");
                alertDialog.setMessage("Please input the name of a county followed by a comma and then the name of the state");
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

    private void searchRequestWithSpecies(final Context context, final String speciesName)
    {
        Log.i("species input", speciesName);
        // test for invalid input

        // base address for searching for a species
        String baseAddress = "https://www.itis.gov/ITISWebService/jsonservice/ITISService/searchForAnyMatch?srchKey=";
        // url for searching
        String formattedName = speciesName.replaceAll(" ", "%20");
        final String query = baseAddress + formattedName;
        final SpeciesSearchHelper helper = new SpeciesSearchHelper();

        Log.i("final url:", query);

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, query, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                // closes the loading, please wait dialog
                dialog.dismiss();

                try
                {
                    searchType = 4;
                    Log.i("response", response.toString());

                    // grab all results from response and place into one JSONObject
                    JSONObject results = response.getJSONArray("anyMatchList").getJSONObject(0);
                    String scientificName = results.getString("sciName");
                    Log.i("scientific name", scientificName);

                    // get common name from JSONArrays
                    JSONArray commonNames = results.getJSONObject("commonNameList").getJSONArray("commonNames");
                    String commonName = commonNames.getJSONObject(0).getString("commonName");
                    Log.i("common name", commonName);

                    // put names in array for later use
                    commonName = helper.capitalizeName(commonName);
                    String[] speciesArr = {commonName + ", " + scientificName};

                    // throw species name in ListView and display
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, speciesArr);
                    speciesListView.setAdapter(adapter);
                    speciesListView.setVisibility(View.VISIBLE);

                    // hide the keyboard
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                    // on clicking species name listview, user should be sent to species info page
                    speciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                        {
                            String speciesName = speciesListView.getItemAtPosition(position).toString();
                            Intent intent = new Intent(SearchActivity.this, SpeciesInfoActivity.class);
                            intent.putExtra(INTENT_EXTRA_SPECIES_NAME, speciesName);
                            startActivity(intent);
                        }
                    });


                }
                catch(Exception exception)
                {
                    Log.e("Couldn't grab JSON data", exception.getMessage());

                    AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
                    alertDialog.setTitle("Invalid Species Name");
                    alertDialog.setMessage("Please enter a common or scientific name");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface alertDialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
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

        requestQueue.add(searchRequest);
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
                            searchType = 3;
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

                            speciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                            {
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                {
                                    String speciesName = speciesListView.getItemAtPosition(position).toString();
                                    Intent intent = new Intent(SearchActivity.this, SpeciesInfoActivity.class);
                                    intent.putExtra(INTENT_EXTRA_SPECIES_NAME, speciesName);
                                    startActivity(intent);
                                }
                            });
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
    //changes offset and reruns search
    public void loadMore(int searchType){
        dialog = ProgressDialog.show(this, "",
                "Loading. Please wait...", true);
        offset += 500;
        String searchInput = searchEditText.getText().toString();
        if (searchType == 1) {
            searchRequestWithCounty(this, searchInput);
        }
        else if (searchType == 2) {
            searchRequestWithState(this, searchInput);
        }
        else if (searchType == 3) {
            whatsAroundMeRequest(this, "-111.31079356054,38.814339278134,-110.57470957617,39.215537729772");
        }
        else if (searchType == 4) {
            searchRequestWithSpecies(this, searchInput);
        }

    }
}
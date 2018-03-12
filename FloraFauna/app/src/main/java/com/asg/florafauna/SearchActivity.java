package com.asg.florafauna;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); // Enable hiding/showing keyboard

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_search);

        searchEditText = findViewById(R.id.SearchEditText);
        speciesListView = findViewById(R.id.ListSpecies);
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
        //searchRequest(this, searchInput);
        searchRequestWithSpecies(this, searchInput);
        //searchRequestWithCounty(this, "Louisiana", "22015");
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

                            speciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                            {
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                {
                                    Intent intent = new Intent(SearchActivity.this, SpeciesInfoActivity.class);
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

                            speciesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                            {
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                {
                                    Intent intent = new Intent(SearchActivity.this, SpeciesInfoActivity.class);
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
                Log.e("onErrorResponse", error.getMessage());
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

        // set up a StringRequest object for catching XML
        //StringRequest stringRequest = new StringRequest(Request.Method.GET, query, new Response.Listener<String>()
        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, query, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                // closes the loading, please wait dialog
                dialog.dismiss();

                try
                {
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
                            Intent intent = new Intent(SearchActivity.this, SpeciesInfoActivity.class);
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
                    Log.e("Error: ", error.getMessage());
                    dialog.dismiss();


                }
            });

        requestQueue.add(searchRequest);
    }

    public void whatsAroundMe(View view) {
        String polygon = "-111.31079356054,38.814339278134,-110.57470957617,39.215537729772";
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
                Log.e("onErrorResponse", error.getMessage());
            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(whatsAroundMeRequest);
    }

}
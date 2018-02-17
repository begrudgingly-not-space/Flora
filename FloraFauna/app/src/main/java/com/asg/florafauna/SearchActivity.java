package com.asg.florafauna;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import android.widget.EditText;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
        searchRequest(this, searchInput);

        //searchRequestWithCounty(this, "Louisiana", "22015");
        //String searchOutput = makeWebCall(searchInput);
    }

    private void searchRequest(final Context context, final String state) {
        final String url = "https://bison.usgs.gov/api/search.json?state=" + state + "&start=0&count=500";
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
                Log.e("onErrorResponse", error.getMessage());
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
                Log.e("onErrorResponse", error.getMessage());
            }
        }
        );

        // Adds request to queue which is then sent
        requestQueue.add(searchRequest);
    }

    public String makeWebCall(String speciesName)
    {

        String results = "";
        // base address for searching for a species
        String baseAddress = "https://www.itis.gov/ITISWebService/services/ITISService/searchForAnyMatch?srchKey=";
        // test user query
        String query = speciesName;
        query = query.replaceAll(" ", "%20");

        try
        {
            URL fullAddress = new URL(baseAddress + query);
            HttpURLConnection connection = (HttpURLConnection) fullAddress.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            String text = "";
            // get all text from request
            while((line = bufferedReader.readLine()) != null)
            {
                text += line;
            }

            bufferedReader.close();

            // itis web call results in xml data
            // get the scientific and common name from the resulting text
            // commonName & sciName
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            // parse the results to access XML data
            Document doc = builder.parse(new InputSource(new StringReader(text)));

            // grab the common names from the data -- get the first (most relevant)
            NodeList commonNameList = doc.getElementsByTagName("ax23:commonName");
            String commonName = commonNameList.item(0).getTextContent();
            NodeList scientificNameList = doc.getElementsByTagName("ax23:sciName");
            String scientificName = scientificNameList.item(0).getTextContent();

            results = commonName + " " + scientificName;

            return results;

        }
        catch(Exception e)
        {
            Log.e("Error: No Results", e.getMessage(), e);
        }

        return results;
    }
}
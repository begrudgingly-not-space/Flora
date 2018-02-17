package com.asg.florafauna;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.asg.florafauna.FloraFaunaActionBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kkey on 2/1/2018.
 */

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_search);

        searchRequest(this, "Louisiana");
    }

    private void searchRequest(final Context context, final String state) {
        final String url = "https://bison.usgs.gov/api/search.json?state=" + state + "&start=0&count=50";
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

                            Log.d("searchResponse", scientificNamesArray.toString());
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

    public void openHelp(View view){
        Intent intent = new Intent(SearchActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    //opens settings
    public void openSettings(View view){
        Intent intent = new Intent(SearchActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed()
    {
        // Disables going back by manually pressing the back button
    }
}
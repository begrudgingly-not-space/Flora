package com.asg.florafauna;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by brada on 2/15/2018.
 */

public class SearchByCounty{

    private Exception exception;

    public void main(String[] arg){
        String baseUrl = "https://bison.usgs.gov/api/search.json?";
        String speciesParam = "species=";
        String typeParam = "&type=";
        String stateParam = "&state=";
        String countyParam = "&countyFips=";

        String[] query = {"Mimus", "polyglottos", "scientific_name", "Louisiana", "22015"};

        try {

            URL fullUrl = new URL(baseUrl + speciesParam + query[0] + "%20" + query[1] + typeParam + query[2] + stateParam + query[3] + countyParam + query[4]);
            System.out.println(fullUrl);
            HttpURLConnection connection = (HttpURLConnection) fullUrl.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            String text = "";

            while((line = bufferedReader.readLine()) != null){
                text += line;
            }

            bufferedReader.close();

            System.out.println(text);

            //JSONObject speciesObject = new JSONObject(text);
            //System.out.println(speciesObject);
            //JSONArray results = speciesObject.getJSONArray("occurrences");
            //String num = results.getJSONObject(0).getString("occurrences");
            //System.out.println(num);

        }

        catch(Exception e){
            Log.e("Error: no results", e.getMessage(), e);
        }
    }

    private void searchRequest(final Context context, final String state) {
        final String url = "https://bison.usgs.gov/api/search.json?state=" + state + "&start=0&count=50";

        // Initialize request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonArrayRequest searchRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("searchResponse", response.toString());
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

}

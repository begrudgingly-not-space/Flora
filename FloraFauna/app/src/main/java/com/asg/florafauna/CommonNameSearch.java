package com.asg.florafauna;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.*;

/**
 * Created by shelby on 2/7/18.
 */

public class CommonNameSearch extends AsyncTask<Void, Void, String> {

    // base address for searching for a species
    // rank is a parameter for the web call
    // it will be used both for searching by common and scientific names
    String baseAddress = "http://api.gbif.org/v1/species/search?q=";
    String rank = "&rank=species";

    private Exception exception;

    protected void onPreExecute()
    {
        // grab the actual user query
        // format and get ready for use
    }

    protected String doInBackground(Void...params)
    {
        // test user query
        String[] query = {"American", "Alligator"};

        try
        {
            URL fullAddress = new URL(baseAddress + query[0] + "%20" + query[1] + rank);
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

            // need to grab the vernacular name and species name from text
            // 'results' is a json array
            JSONObject speciesObject = new JSONObject(text);
            JSONArray results = speciesObject.getJSONArray("results");

            String scientificName = results.getJSONObject(0).getString("species");

            // 'vernacularNames' is an array that holds the common name
            // vernacularName is the first object of the array, which will give the common name
            JSONArray vernNames = results.getJSONObject(0).getJSONArray("vernacularNames");
            String commonName = vernNames.getJSONObject(0).getString("vernacularName");


        }

        catch(Exception e)
        {
            Log.e("Error: No Results", e.getMessage(), e);
        }

        return null;
    }

    protected void onPostExecute(String response)
    {
        // format JSON response here
    }
}

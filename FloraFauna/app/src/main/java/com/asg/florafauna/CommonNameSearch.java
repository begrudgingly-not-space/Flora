package com.asg.florafauna;

import android.os.AsyncTask;

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

        }

        catch(Exception e)
        {

        }

        return null;
    }

    protected void onPostExecute(String response)
    {
        // format JSON response here
    }
}

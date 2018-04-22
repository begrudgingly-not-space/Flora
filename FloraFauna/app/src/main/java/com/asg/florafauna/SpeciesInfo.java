package com.asg.florafauna;

// Created by steven on 2/8/18.

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class SpeciesInfo
{
    private String scientificName, commonName, eolLink, description, imageLink, done, dispError;

    // Initializer for when only passed name (search results from bison)
    public SpeciesInfo(final Context context, String name)
    {
        this.scientificName = name;
        setFromEOL(context, name);
    }

    // Initializer for when passed eol link, skips first search (unused for now)
    /*
    public SpeciesInfo(String name, String link)
    {
        this.scientificName = name;
        this.eolLink = link;
    }*/

    // Pull relevant info from the search page and from the eol information page
    // All the description="*" lines are for tracking where I am getting to in the program
    private void setFromEOL(final Context context, String name)
    {
        String query = eolQuery(name);
        Log.i("query", query);
        //RequestQueue requestQueue = Volley.newRequestQueue(context);
        description = "\nIn setFromEOL";

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, query, null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                description = description.concat("in onResponse");
                try
                {
                    description+="\nin onResponse try block";

                    // This has been tested, gives the data that I am looking for
                    JSONObject results = response.getJSONArray("results").getJSONObject(0);
                    Log.i("linkResponse",results.getString("link"));
                    eolLink = results.getString("link");

                    // The log updates, but not every time?
                }
                catch (Exception e)
                {
                    Log.e("Error: ", e.toString());
                    dispError=e.toString();
                    description+="\nin onResponse catch block";
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("Error: ", error.toString());
                dispError = error.toString();
                description += "\nin onErrorResponse";
            }
        });
        requestQueue.add(searchRequest);
        //description = "finished with searchRequest";

    }

    /*private void setFromEOL(String name) {
        try
        {
            String query = eolQuery(name);
            String json = Jsoup.connect(query).ignoreContentType(true).execute().parse().toString();
            eolLink = json.substring(json.indexOf("http"),json.indexOf("?"));

            String page = Jsoup.connect(eolLink).timeout(10000).execute().parse().toString();
            int start = page.indexOf("</h4>",page.indexOf("<h4>Description"))+6;
            int stop = page.indexOf("\n", start);
            description = page.substring(start,stop);

            start = page.indexOf("<title>")+7;
            stop = page.indexOf("-",start);
            commonName = page.substring(start,stop);

            start = page.indexOf("<img alt");
            start = page.indexOf("src",start)+5;
            stop = page.indexOf("\"",start);
            imageLink = page.substring(start,stop);
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
            error=e.toString();
        }
    }*/

    // Format query to search for a species(exact name) on eol
    private String eolQuery(String name)
    {
        String first="http://eol.org/api/search/1.0.json?q=";
        String last="&page=1&exact=true&filter_by_taxon_concept_id=&filter_by_hierarchy_entry_id=&filter_by_string=&cache_ttl=";
        name=name.replaceAll(" ","+");
        return first + name + last;
    }

    // Format query to search for a species on gbif
    private String gbifQuery(String name)
    {
        String first = "http://api.gbif.org/v1/species?name=";
        name = name.replaceAll(" ","+");
        return first + name;
    }

    // Getters to return values from the object
    public String getScientificName()
    {
        return scientificName;
    }
    public String getCommonName()
    {
        return commonName;
    }
    public String getDescription(){return description;}
    public String getImageLink(){return imageLink;}
    public String getEolLink(){return eolLink;}
    public String getError(){return dispError;}
    public String getDone(){return done;}
}

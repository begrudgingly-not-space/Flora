package com.asg.florafauna;

import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.*;

/**
 * Created by brada on 2/15/2018.
 */

public class SearchByCounty{

    private Exception exception;

    public static void main(String[] arg){

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

            JSONObject speciesObject = new JSONObject(text);
            JSONArray results = speciesObject.getJSONArray("observation");
            //String num = results.getJSON

        }

        catch(Exception e){
            Log.e("Error: no results", e.getMessage(), e);
        }

    }

}

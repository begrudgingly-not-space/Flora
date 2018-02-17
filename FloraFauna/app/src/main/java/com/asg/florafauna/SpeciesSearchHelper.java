package com.asg.florafauna;

import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import org.w3c.dom.*;

/**
 * Created by shelby on 2/7/18.
 */

public class SpeciesSearchHelper extends AsyncTask<Void, Void, String> {

    private Exception exception;

    protected void onPreExecute()
    {
        // grab the actual user query
        // format and get ready for use
    }

    @Override
    protected String doInBackground(Void...params)
    {
        // base address for searching for a species
        String baseAddress = "https://www.itis.gov/ITISWebService/services/ITISService/searchForAnyMatch?srchKey=";
        // test user query
        String query = "American Alligator";
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

            String speciesName = commonName + " " + scientificName;


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

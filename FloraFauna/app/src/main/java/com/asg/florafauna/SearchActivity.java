package com.asg.florafauna;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Toast;

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

    EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar actionToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(actionToolbar);
        searchEditText = findViewById(R.id.SearchEditText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.action_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));

        }

        return super.onOptionsItemSelected(item);
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

    public void search(View view) {
        String searchInput = searchEditText.getText().toString();
        String searchOutput = makeWebCall(searchInput);
        Toast.makeText(this, searchOutput, Toast.LENGTH_LONG).show();
        Log.i("searchOutput", searchOutput);
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
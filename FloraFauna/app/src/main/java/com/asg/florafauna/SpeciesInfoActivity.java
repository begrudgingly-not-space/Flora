package com.asg.florafauna;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.InputStream;

import static com.asg.florafauna.SearchActivity.INTENT_EXTRA_SPECIES_NAME;

/**
 * Created by steven on 3/2/18.
 */

public class SpeciesInfoActivity extends AppCompatActivity
{
    private TextView description, eolLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speciesinfo);
        //default variables to take values from the results menu from the search/history
        String sName;
        String link;

        String speciesName = getIntent().getStringExtra(INTENT_EXTRA_SPECIES_NAME);

        if (speciesName != null) {
            sName = speciesName;
            // link = extras.getString("link");
        }
        else {
            sName = "No species name";
            // link = "http://eol.org/pages/326447/overview";
        }

        //create object for the animal selected
        //SpeciesInfo species = new SpeciesInfo(this,sName);


        TextView SNgetText = findViewById(R.id.ScientificName);
        //SNgetText.setText(String.valueOf(species.getScientificName()));

        TextView CNgetText = findViewById(R.id.CommonName);
        //CNgetText.setText(String.valueOf(species.getCommonName()));

        description = findViewById(R.id.Description);

        TextView ILgetText = findViewById(R.id.ImageLink);
        //ILgetText.setText(String.valueOf(species.getImageLink()));

        eolLink = findViewById(R.id.EoLLink);

        //action bar creation copied form HelpActivity.java
        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_speciesinfo);


        //from https://stackoverflow.com/questions/5776851/load-image-from-url#10868126
        //new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute(species.getImageLink());
        setFromEOL(this, speciesName);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.species_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_home:
                Intent search_intent = new Intent(SpeciesInfoActivity.this, SearchActivity.class);
                startActivity(search_intent);
                return true;
            case R.id.action_settings:
                Intent settings_intent = new Intent(SpeciesInfoActivity.this, SettingsActivity.class);
                startActivity(settings_intent);
                return true;
            case R.id.action_help:
                Intent help_intent = new Intent(SpeciesInfoActivity.this, HelpActivity.class);
                startActivity(help_intent);
                return true;
            case R.id.action_map:
                Intent intent = new Intent(SpeciesInfoActivity.this, MapActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*//opens settings
    public void openSettings(View view){
        Intent intent = new Intent(SpeciesInfoActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openSearch(View view){
        Intent intent = new Intent(SpeciesInfoActivity.this, SearchActivity.class);
        startActivity(intent);
    }

    public void openHelp(View view) {
        Intent intent =  new Intent(SpeciesInfoActivity.this, HelpActivity.class);
        startActivity(intent);
    }*/

    public void goBack(View view){
        /* closes the activity */
        finish();
    }


    //image viewer from https://stackoverflow.com/questions/5776851/load-image-from-url#10868126
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
    {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage)
        {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls)
        {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try
            {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                //Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
    //pull relevant info from the search page and from the eol information page
    //all the description="*" lines are for tracking where I am getting to in the program
    private void setFromEOL(final Context context, String name)
    {
        String query=eolQuery(name);
        Log.i("query",query);

        description.setText("\nIn setFromEOL");
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, query, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        description.append("\nin onResponse");
                        try
                        {
                            description.append("\nin onResponse try block");

                            //this has been tested, gives the data that I am looking for
                            JSONObject results = response.getJSONArray("results").getJSONObject(0);
                            Log.i("linkResponse",results.getString("link"));
                            eolLink.setText(results.getString("link"));

                            //the log updates, but not every time?
                        }
                        catch(Exception e)
                        {
                            Log.e("Error: ", e.toString());
                            description.append("\nin onResponse catch block");
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("Error: ", error.toString());
                description.append("\nin onErrorResponse");
            }
        });
        requestQueue.add(searchRequest);
        //description="finished with searchRequest";

    }
    /*private void setFromEOL(String name)
    {
        try
        {
            String query=eolQuery(name);

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
        catch(Exception e)
        {
            System.out.println(e.toString());
            error=e.toString();
        }
    }*/

    //format query to search for an animal(exact name) on eol
    private String eolQuery(String name)
    {
        String first="http://eol.org/api/search/1.0.json?q=";
        String last="&page=1&exact=true&filter_by_taxon_concept_id=&filter_by_hierarchy_entry_id=&filter_by_string=&cache_ttl=";
        name=name.replaceAll(" ","+");
        return first+name+last;
    }
}



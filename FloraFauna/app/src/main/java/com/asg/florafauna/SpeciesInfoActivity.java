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
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.asg.florafauna.SearchActivity.INTENT_EXTRA_SPECIES_NAME;

import java.net.URL;
/**
 * Created by steven on 3/2/18.
 */

public class SpeciesInfoActivity extends AppCompatActivity
{
    private String description="", eolLink="", scientificName="", commonName="", imageLink="";
    private String[] themeArray = new String[1];
    private Bitmap image;
    private ImageView bmImage;
    public static final String INTENT_EXTRA_IMAGELINK = "imageLink";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //setTheme(R.style.AppTheme);
        try {
            //opens the file to read its contents
            FileInputStream fis = this.openFileInput("theme");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            themeArray[0] = reader.readLine(); //adds the line to the temp array
            reader.close();
            isr.close();
            fis.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if (themeArray[0].equals("Green")){
            setTheme(R.style.AppTheme);
        }
        else if (themeArray[0].equals("Blue")){
            setTheme(R.style.AppThemeBlue);
        }
        else if (themeArray[0].equals("Mono")){
            setTheme(R.style.AppThemeMono);
        }
        else if (themeArray[0].equals("Cherry")){
            setTheme(R.style.AppThemeCherry);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speciesinfo);
        //default variables to take values from the results menu from the search/history

        scientificName = getIntent().getStringExtra(INTENT_EXTRA_SPECIES_NAME);

        //proof that displaying images works
        //new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute("https://media.eol.org/content/2014/10/09/11/00594_580_360.jpg");

        if (scientificName == null) {
            scientificName="No Species Name";
            //scientificName="Canis lupus";
            //getPage(this,scientificName);
        }
        else
        {
            getPage(this, scientificName);
        }

        //action bar creation copied form HelpActivity.java
        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_speciesinfo);
        //Log.i("Data","ImageLink "+imageLink);

        Button button = (Button) findViewById(R.id.button_send);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SpeciesInfoActivity.this, ImageActivity.class);
                intent.putExtra(INTENT_EXTRA_IMAGELINK, imageLink);
                //new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute(imageLink);
                startActivity(intent);

            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
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

    public void goBack(View view){
        /* closes the activity */
        finish();
    }



    //pull relevant info from the search page and from the eol information page
    private void getPage(final Context context, String name)
    {
        String query=eolQuery(name);

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, query, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            JSONObject results = response.getJSONArray("results").getJSONObject(0);

                            //initial link (will be redirected)
                            eolLink=results.getString("link");

                            //strip numeric designation for species
                            eolLink=eolLink.substring(eolLink.indexOf("org/")+4,eolLink.indexOf("?"));

                            //format to go to details page of that species
                            eolLink="http://eol.org/pages/"+eolLink+"/details";
                            Log.i("eolLink",eolLink);
                            setDataTask task=new setDataTask();
                            task.execute();

                        }
                        catch(Exception e)
                        {
                            Log.e("Error onResponse: ", e.toString());
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("Error onErrorResponse: ", error.toString());
            }
        });
        requestQueue.add(searchRequest);

    }

    //format query to search for an animal(exact name) on eol
    private String eolQuery(String name)
    {
        String first="http://eol.org/api/search/1.0.json?q=";
        String last="&page=1&exact=true&filter_by_taxon_concept_id=&filter_by_hierarchy_entry_id=&filter_by_string=&cache_ttl=";
        name=name.replaceAll(" ","+");
        return first+name+last;
    }
    private String pageReader(String url)
    {
        String line;
        String output="";
        try
        {
            URL page = new URL(url);
            BufferedReader in = new BufferedReader((new InputStreamReader(page.openStream())));
            while((line=in.readLine()) != null)
            {
                output+=line+"\n";
            }
            in.close();
        }
        catch(Exception e)
        {
            Log.e("Error Page Reader: ",e.toString());
            description="network error";
        }
        return output;
    }
    private class setDataTask extends AsyncTask<Void, String, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            try {
                //String page=Jsoup.connect(eolLink).timeout(10000).execute().parse().toString();
                String page=pageReader(eolLink);

                int start = page.indexOf("<title>") + 7;
                int stop = page.indexOf("-", start);
                commonName = page.substring(start, stop);

                getDescription(page);

                start=page.indexOf("http://media.eol.org/content");
                stop=page.indexOf("\'",start);
                imageLink=page.substring(start,stop);
                Log.i("imagelink",imageLink);
                //wikipedia page starts with wikipedia.org/w/index.php?title=
                //ends with & (and symbol)
            }
            catch(Exception e)
            {
                Log.e("Error diB SetDataTask: ", e.toString());
            }

            return null;
        }
        protected void onProgressUpdate(String... progress)
        {
            String imageL=progress[0];


            //new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute(progress[0]);
        }
        protected void onPostExecute(Void result)
        {
            if (scientificName.trim().equals(""))
            {
                scientificName="No data found";
            }
            if (commonName.trim().equals(""))
            {
                commonName="No data found";
            }
            if (description.trim().equals(""))
            {
                description="No data found";
            }
            if (eolLink.trim().equals(""))
            {
                eolLink="No data found";
            }
            if (imageLink.trim().equals(""))
            {
                imageLink="No data found";
            }
            // set each field based on global variable
            TextView scientificNameTV = findViewById(R.id.ScientificName);
            scientificNameTV.setText(scientificName);

            TextView commonNameTV = findViewById(R.id.CommonName);
            commonNameTV.setText(commonName);

            TextView descriptionTV = findViewById(R.id.Description);
            descriptionTV.setText(description);

            TextView eolLinkTV = findViewById(R.id.EoLLink);
            eolLinkTV.setText(eolLink);

            TextView imageLinkTV = findViewById(R.id.ImageLink);
            imageLinkTV.setText(imageLink);

            //bmImage.setImageBitmap(image);
        }
        protected void getDescription(String page)
        {
            String formattedDescription;
            int start, stop;

            //closest unique string to description on all pages i have tested
            //needs more testing
            //
            start=page.indexOf("Morphology</h3>");
            if (start!=-1) {
                //get to line above
                start = page.indexOf("copy", start);

                //get to line with description
                start = page.indexOf("\n", start) + 1;

                //stop at the end of that line
                //all descriptions i have found are on one line
                stop = page.indexOf("\n", start);

                //grab that line
                description = page.substring(start, stop);

                //Use some of the old formatting, makes it look cleaner, can be used to do better formatting at a later date
                description = description.replaceAll("<p>", "\n");

                //removes all other HTML markup
                while (description.contains("<")) {
                    //find start of tag
                    start = description.indexOf("<");
                    //find end of tag(after the start)
                    stop = description.indexOf(">", start);
                    //use everthing from the begining to the start of the tag, and everything after the end of the tag
                    description = description.substring(0, start) + description.substring(stop + 1);
                }
                //remove extra whitepsace from beginning and end
                description = description.trim();
            }
            //special cases found
            /*
            https://www.eol.org/pages/401139/details
            https://www.eol.org/pages/244454/details#morphology
            https://www.eol.org/pages/449887/details
            http://eol.org/pages/921578/details
            http://eol.org/pages/921578/details
             */


        }
    }



}



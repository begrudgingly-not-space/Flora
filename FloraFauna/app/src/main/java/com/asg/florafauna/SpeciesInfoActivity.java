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
import java.io.InputStreamReader;

import static com.asg.florafauna.SearchActivity.INTENT_EXTRA_SPECIES_NAME;

import java.net.URL;
/**
 * Created by steven on 3/2/18.
 */

public class SpeciesInfoActivity extends AppCompatActivity
{
    private String scientificName="", commonName="", description="", eolLink="",  imageLink="";
    private Bitmap image;
    private ImageView bmImage;
    public static final String INTENT_EXTRA_IMAGELINK = "imageLink";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeCreator.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speciesinfo);

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

    /*not setup*/
            //get scientific name sent by search
            scientificName = getIntent().getStringExtra(INTENT_EXTRA_SPECIES_NAME);

            getPage(this);

    }

    //pull relevant info from the search page and from the eol information page
    private void getPage(final Context context)
    {
        String query=eolQuery(scientificName);

        //everything until next try block is taken from search activity
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

    //webcalls without json require AsyncTask
    private class setDataTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            //returns text version of EoL page
            String page=pageReader(eolLink);

            //strip common name from page
            int start = page.indexOf("<title>") + 7;
            int stop = page.indexOf("-", start);
            commonName = page.substring(start, stop);

            //long enough, and will have enough options, I didn't want to clutter
            getDescription(page);

            //not working well yet, possibly due to app webcalls
            //503 error on phone browser, but works on computer browser
            //not unique, but both versions(in tested pages) return the same URL
            start=page.indexOf("http://media.eol.org/content");
            //stop at end quote
            stop=page.indexOf("\'",start);
            imageLink=page.substring(start,stop);
            Log.i("imagelink",imageLink);
            //wikipedia page starts with wikipedia.org/w/index.php?title=
            //ends with & (and symbol)

            return null;
        }
        //after page is downloaded and scraped, post the results
        protected void onPostExecute(Void result)
        {
            //error messages if nothing is found for a field
            //trim() removes whitespace at beggining and end
            //.equals to see if string is only empty quotes
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
    }

/*helper functions*/

    //format query to search for an animal(exact name) on eol
    private String eolQuery(String name)
    {
        //default start to every EoL api search query
        String first="http://eol.org/api/search/1.0.json?q=";

        //query options I am using
        String last="&page=1&exact=true&filter_by_taxon_concept_id=&filter_by_hierarchy_entry_id=&filter_by_string=&cache_ttl=";

        //name can't have spaces, needs "+"
        name=name.replaceAll(" ","+");
        return first+name+last;
    }

    //read in the webpage line by line
    //jsoup had issues closing
    private String pageReader(String url)
    {
        String line="", output="";
        try//needed for URL
        {
            URL page = new URL(url);
            BufferedReader in = new BufferedReader((new InputStreamReader(page.openStream())));

            //read in 1 line, if there is a line to read
            while((line=in.readLine()) != null)
            {
                //add it to document to be parsed(adding stripped newlines to make it easier to parse)
                output+=line+"\n";
            }
            //always close document
            in.close();
        }
        catch(Exception e)
        {
            Log.e("Error Page Reader: ",e.toString());
            description="network error";
        }
        return output;
    }

    private void getDescription(String page)
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
//TODO
        //special cases found
            /*
            https://www.eol.org/pages/401139/details
            https://www.eol.org/pages/244454/details#morphology
            https://www.eol.org/pages/449887/details
            http://eol.org/pages/921578/details
            http://eol.org/pages/921578/details
             */


    }

/*Setup functions*/
/*created by other people, only kinda know how they work*/
    //set the theme
    private void setTheme(){
        String themeArray="";
        try {
            //opens the file to read its contents
            FileInputStream fis = this.openFileInput("theme");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            themeArray = reader.readLine(); //adds the line to the temp array
            reader.close();
            isr.close();
            fis.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        switch (themeArray)
        {
            case "Blue":
                setTheme(R.style.AppThemeBlue);
                break;
            case "Mono":
                setTheme(R.style.AppThemeMono);
                break;
            case "Cherry":
                setTheme(R.style.AppThemeCherry);
                break;
            default:
                setTheme(R.style.AppTheme);
                break;
        }
    }

    //create options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.species_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //use options menu
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
    //Go back
    public void goBack(View view){
        /* closes the activity */
        finish();
    }


}



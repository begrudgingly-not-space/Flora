package com.asg.florafauna;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.widget.TextView;
import android.widget.ImageView;
import android.os.AsyncTask;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.asg.florafauna.SearchActivity.INTENT_EXTRA_SPECIES_NAME;

/**
 * Created by steven on 3/2/18.
 */

public class SpeciesInfoActivity extends AppCompatActivity
{
    private String scientificName="", commonName="", description="", eolLink="",  imageLink="";
    public static final String INTENT_EXTRA_IMAGELINK = "imageLink";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
/* setup*/
    setTheme();

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_speciesinfo);

    //action bar creation copied form HelpActivity.java
    FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_speciesinfo);
    //Log.i("Data","ImageLink "+imageLink);

/*not setup*/
        //get scientific name sent by search
        scientificName = getIntent().getStringExtra(INTENT_EXTRA_SPECIES_NAME);

        getID(this);

    }

    //pull relevant info from the search page and from the eol information page
    private void getID(final Context context)
    {
        String query="http://eol.org/api/search/1.0.json?q="+scientificName.replaceAll(" ","+")+"&page=1&exact=true&filter_by_taxon_concept_id=&filter_by_hierarchy_entry_id=&filter_by_string=&cache_ttl=";

        //everything until next try block is taken from search activity
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, query, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            //JSONArray results=response.getJSONArray("results");
                            JSONObject results = response.getJSONArray("results").getJSONObject(0);

                            //initial link (will be redirected)
                            eolLink=results.getString("link");

                            //strip numeric designation for species
                            getData(context, eolLink.substring(eolLink.indexOf("org/")+4,eolLink.indexOf("?")));
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
    private void getData(final Context context,String ID)
    {
        String query="http://eol.org/api/pages/1.0.json?batch=false&id="+ID+"&images_per_page=1&images_page=1&videos_per_page=0&videos_page=0&sounds_per_page=0&sounds_page=0&maps_per_page=0&maps_page=0&texts_per_page=1&texts_page=1&subjects=overview&licenses=all&details=true&common_names=true&synonyms=false&references=false&taxonomy=false&vetted=1&cache_ttl=&language=en";
        Log.i("query",query);
        //everything until next try block is taken from search activity
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, query, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        Boolean pref,en;
                        try
                        {
                            JSONArray names=response.getJSONArray("vernacularNames");
                            for(int i=0;i<names.length();i++)
                            {
                                JSONObject record=names.getJSONObject(i);

                                //is this and english name?
                                en=record.getString("language").equals("en");

                                //EoL doesn't load eol_preferred with false, just leaves it off
                                //so if it isn't preferred, i am setting it to false
                                try {pref = record.getBoolean("eol_preferred");}
                                catch(Exception e) {pref=false;}

                                //if this is an english name and a preferred name, its what we want
                                //could remove pref to get a list of possible names
                                if (en && pref)
                                {
                                    commonName = record.getString("vernacularName");
                                }
                            }

                            JSONArray results=response.getJSONArray("dataObjects");
                            description=results.getJSONObject(0).getString("description");
                            Log.i("description",description);
                            cleanDescription();

                            imageLink=results.getJSONObject(1).getString("mediaURL");
                            Log.i("imageLink",imageLink);

                            new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute(imageLink);
                        }
                        catch(Exception e)
                        {
                            Log.e("Error onResponse: ", e.toString());
                        }
                        setData();
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

/*helper functions*/

    //capitalize first letter of each word because java doesn't have .title()
    private String cap(String str)
    {
        String output="";
        String[] words=str.split(" ");
        for(String word:words)
        {
            output+=(word.substring(0,1).toUpperCase()+word.substring(1)+" ");
        }
       return output.trim();
    }

    //cleans up descriptions, adds linebreaks, removes HTML
    private void cleanDescription()
    {
        int start,stop;
        //removes tacked on links to more info
        if(description.contains("<br>"))
        {
            description = description.substring(0, description.indexOf("<br>"));
        }
        //format list of basic characteristics
        description=description.replaceAll("<p>","\n");
        //removes remaining HTML markup
        while (description.contains("<"))
        {
            //find start of tag
            start = description.indexOf("<");
            //find end of tag(after the start)
            stop = description.indexOf(">", start);
            //use everything from the beginning to the start of the tag, and everything after the end of the tag
            description = description.substring(0, start) +" "+ description.substring(stop + 1);
            Log.i("description",description.substring(start,stop));
        }
        description=description.replaceAll(" +"," ").trim();
    }
    private void setData()
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
        scientificNameTV.setText(cap(scientificName));

        TextView commonNameTV = findViewById(R.id.CommonName);
        commonNameTV.setText(cap(commonName));

        TextView descriptionTV = findViewById(R.id.Description);
        descriptionTV.setText(description.trim());

        TextView eolLinkTV = findViewById(R.id.EoLLink);
        eolLinkTV.setText(eolLink.trim());

        TextView imageLinkTV = findViewById(R.id.ImageLink);
        imageLinkTV.setText(imageLink.trim());

        //bmImage.setImageBitmap(image);
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
        //I changed this to switch statement AS claimed it would be faster
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


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        private DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.toString());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}



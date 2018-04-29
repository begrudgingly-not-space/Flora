package com.asg.florafauna;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
//import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ImageView;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.*;

import static com.asg.florafauna.SearchActivity.INTENT_EXTRA_SPECIES_NAME;

/**
 * Created by steven on 3/2/18.
 */

public class SpeciesInfoActivity extends AppCompatActivity
{
    private String scientificName = "", commonName = "", description = "", eolLink = "", imageLink = "";
    private int devWidth;
    private int devHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(ThemeCreator.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speciesinfo);
        if (getSupportActionBar() != null) {
            FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_speciesinfo);
        }

        // SSL Certification for webcalls
        if (BuildConfig.DEBUG) {
            SSLCertificates.trustAll();
        }

        /* Data population */
        // Get scientific name sent by search
        scientificName = getIntent().getStringExtra(INTENT_EXTRA_SPECIES_NAME);

        // Set scientific name on display
        TextView scientificNameTV = findViewById(R.id.ScientificName);
        scientificNameTV.setText(title(scientificName));

        //set device width and height so they can be used later for scaling images to the right size
        devWidth=this.getResources().getDisplayMetrics().widthPixels;
        devHeight=this.getResources().getDisplayMetrics().heightPixels;

        getID(this);
    }

    // Pull relevant info from the search page and from the eol information page
    private void getID(final Context context)
    {
        // Build query that contains the name from search, and set default options
        String query = "http://eol.org/api/search/1.0.json?q=" + scientificName.replaceAll(" ", "+") + "&page=1&exact=true&filter_by_taxon_concept_id=&filter_by_hierarchy_entry_id=&filter_by_string=&cache_ttl=";
        Log.i("ID", query);

        // Everything until next try block is taken from search activity
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest pageRequest = new JsonObjectRequest(Request.Method.GET, query, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            // Take results, all objects have same link, so take first
                            JSONObject results = response.getJSONArray("results").getJSONObject(0);

                            // Initial link (will be redirected)
                            eolLink = results.getString("link").trim();

                            // Strip ID that EoL uses for that species
                            String ID = eolLink.substring(eolLink.indexOf("org/") + 4, eolLink.indexOf("?"));

                            // Make sure data was found or set error message
                            if (eolLink.trim().equals(""))
                            {
                                eolLink="No EoL link found.";
                            }
                            //Set link to EoL page on display
                            TextView eolLinkTV = findViewById(R.id.EoLLink);
                            String html="<A href=\""+eolLink+"\" target=_blank>View on Encyclopedia of Life</A>";
                            Log.i("Thtml",html);
                            eolLinkTV.setText(Html.fromHtml(html));
                            eolLinkTV.setMovementMethod(LinkMovementMethod.getInstance());

                            //Get rest of the data
                            getData(context, ID);
                        }
                        catch(Exception e)
                        {
                            Log.e("Error: GetID JSON", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("onErrorResponse", error.toString());
            }
        });
        requestQueue.add(pageRequest);
    }

    private void getData(final Context context, String ID)
    {
        //create query with ID from previous call, and default options
        String query = "http://eol.org/api/pages/1.0.json?batch=false&id=" + ID + "&images_per_page=100&images_page=1&videos_per_page=0&videos_page=0&sounds_per_page=0&sounds_page=0&maps_per_page=0&maps_page=0&texts_per_page=1&texts_page=1&subjects=overview&licenses=all&details=true&common_names=true&synonyms=false&references=false&taxonomy=false&vetted=1&cache_ttl=&language=en";
        Log.i("Data", query);

        //everything until get statements taken from search activity
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, query, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {

                        //get functions for isolation and clarity
                        getCommonName(response);
                        getDescription(response);
                        getImageLink(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("Error onErrorResponse: ", error.toString());
            }
        });
        requestQueue.add(searchRequest);

    }

/*Get functions*/
    private void getCommonName(JSONObject response)
    {
        Boolean pref, en;
        try
        {
            //get list of common names, language for that name, and if it is the preferred name
            JSONArray names = response.getJSONArray("vernacularNames");

            //loop through each record in the array
            //for each doesn't work with JSONArray
            for (int i = 0; i < names.length(); i++)
            {
                JSONObject record = names.getJSONObject(i);

                // T/F for "is this name in english?"
                //EoL doesn't put english at the top even with language set to english
                en = record.getString("language").equals("en");

                //EoL doesn't load eol_preferred with false, just leaves it off
                //so if it isn't preferred, i am setting it to false
                try
                {
                    pref = record.getBoolean("eol_preferred");
                }
                catch (Exception e)
                {
                    pref = false;
                }

                //if this is an english name and a preferred name, its what we want
                //could remove pref to get a list of possible names
                if (en && pref)
                {
                    commonName = record.getString("vernacularName").trim();
                }
            }
        }
        catch (Exception e)
        {
            Log.e("Error GetCommonName", e.toString());
        }

        //make sure data was found or set error message
        if (commonName.equals(""))
        {
            commonName="";
        }
        //set common name on display
        TextView commonNameTV = findViewById(R.id.CommonName);
        commonNameTV.setText(title(commonName));
    }

    //fetches and then cleans up descriptions, adds newlines, removes HTML
    private void getDescription(JSONObject response)
    {
        //pull description from JSON
        try
        {
            description = response.getJSONArray("dataObjects").getJSONObject(0).getString("description");
        }
        catch (Exception e)
        {
            Log.e("Error GetDescription", e.toString());
        }
        if(description.contains("Links:<br>"))
        {
            description=description.substring(0,description.indexOf("Links:<br>"));
        }
        //make sure data was found or set error message
        if (description.equals(""))
        {
            description="No description found.";
        }
        //Set description on display
        TextView descriptionTV = findViewById(R.id.Description);
        descriptionTV.setText(Html.fromHtml(description));
        descriptionTV.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void getImageLink(JSONObject response)
    {
        try
        {
            JSONArray imageArray=response.getJSONArray("dataObjects");
            //loop through array, object 0 was description
            //imageArray is a bad name, but that's what most of the data is
            for(int i=1;i<imageArray.length()-1;i++)
            {
                if(imageArray.getJSONObject(i).has("mediaURL"))
                {
                    imageLink = imageArray.getJSONObject(i).getString("mediaURL");

                    //create view to place image in
                    ImageView imageView = new ImageView(this);

                    //pass that view and the link to the image to have the downloader download and fill
                    //has to be set because it needs to be async or will never get filled
                    new DownloadImageTask(imageView).execute(imageLink);
                }

            }
        }
        catch(Exception e)
        {
            Log.e("Error GetImageLink",e.toString());
        }
        log();
    }

/*Small Helper functions */
    //capitalize first letter of each word because java doesn't have .title()
    private String title(String str)
    {
        String output="";
        String[] words=str.split(" ");
        for(String word:words)
        {
            output+=(word.substring(0,1).toUpperCase()+word.substring(1)+" ");
        }
        return output.trim();
    }

    //log values
    private void log()
    {
        Log.i("TsciName",scientificName);
        Log.i("TcommonName",commonName);
        Log.i("Tdescription",description);
        Log.i("TeolLink",eolLink);
        Log.i("TimageLink",imageLink);
    }
/*Image downloader and display*/
    //https://stackoverflow.com/a/10868126
    //download and display an image given a URL
    //DO NOT MAKE THIS STATIC - breaks findViewByID
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
    {
        ImageView bmImage;
        LinearLayout layout = (LinearLayout) findViewById(R.id.linear);

        private DownloadImageTask(ImageView bmImage)
        {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls)
        {
            String urlPath = urls[0];
            Bitmap mIcon11 = null;
            try
            {
                URL url = new URL(urlPath);
                URLConnection con = url.openConnection();
                InputStream in = con.getInputStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            }
            catch (Exception e)
            {
                Log.e("Error", e.toString());
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result)
        {

            if (result==null)
            {
                Log.i("TimageTest","null");
            }
            else
            {
                Log.i("TimageSize", result.getWidth() + ","+result.getWidth());
                //values for calculating the scaling for the image
                int oldHeight=result.getHeight();
                int oldWidth=result.getWidth();
                int percent=30;
                int newHeight=devHeight/(100/percent);
                int newWidth=oldWidth*newHeight/oldHeight;
                //scale the image
                result=Bitmap.createScaledBitmap(result,newWidth,newHeight,false);
                try
                {
                    bmImage.setImageBitmap(result);
                    //add to the "album" at the top of the page
                    layout.addView(bmImage);
                }
                catch (Exception e)
                {
                    Log.e("Error","Can't set bitmap "+e);
                }

            }
        }
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
        // Closes the activity
        finish();
    }
}
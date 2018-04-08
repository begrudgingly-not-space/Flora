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
import org.jsoup.*;

/**
 * Created by steven on 3/2/18.
 */

public class SpeciesInfoActivity extends AppCompatActivity
{
    private String scientificName;
    private String commonName;
    private String description;
    private String eolLink;
    private String imageLink;

    //private TextView description, eolLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speciesinfo);
        //default variables to take values from the results menu from the search/history

        scientificName = getIntent().getStringExtra(INTENT_EXTRA_SPECIES_NAME);

        if (scientificName == null) {
            //scientificName="No Species Name";
            scientificName="Ursus Arctos";
            getPage(this,scientificName);
        }
        else
        {
            getPage(this, scientificName);
        }

        //action bar creation copied form HelpActivity.java
        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_speciesinfo);
        Log.i("Data","ImageLink "+imageLink);

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

    public void goBack(View view){
        /* closes the activity */
        finish();
    }



    //pull relevant info from the search page and from the eol information page
    //all the description="*" lines are for tracking where I am getting to in the program
    private void getPage(final Context context, String name)
    {
        String query=eolQuery(name);
        Log.i("Data",query);

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, query, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            //this has been tested, gives the data that I am looking for
                            JSONObject results = response.getJSONArray("results").getJSONObject(0);
                            eolLink=results.getString("link");
                            Log.i("eolLink",eolLink);
                            setDataTask task=new setDataTask();
                            Log.i("Place","Task created");
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

    private class setDataTask extends AsyncTask<Void, String, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            Log.i("Place","doInBackground");
            try {
                String page = Jsoup.connect(eolLink).timeout(10000).execute().parse().toString();

                int start = page.indexOf("<img alt");
                start = page.indexOf("src", start) + 5;
                int stop = page.indexOf("\"", start);
                imageLink = page.substring(start, stop);
                Log.i("IL for PP", imageLink);
                publishProgress(imageLink);

                start = page.indexOf("</h4>", page.indexOf("<h4>Description")) + 6;
                stop = page.indexOf("\n", start);
                description = page.substring(start, stop);
                Log.i("description", description);

                start = page.indexOf("<title>") + 7;
                stop = page.indexOf("-", start);
                commonName = page.substring(start, stop);
                Log.i("commonName", commonName);
            }
            catch(Exception e)
            {
                Log.e("Error diB SetDataTask: ", e.toString());
            }
            return null;
        }
        protected void onProgressUpdate(String... progress)
        {
            new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute(progress[0]);

        }
        protected void onPostExecute(Void result)
        {
            // Currently search page sends scientific name and common name in some cases
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

            //from https://stackoverflow.com/questions/5776851/load-image-from-url#10868126
            Log.i("Data preDIT",imageLink);
            //new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute(imageLink);
        }
    }

    //image viewer from https://stackoverflow.com/questions/5776851/load-image-from-url#10868126
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }
        @Override
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                Log.i("place", "working on image");
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}



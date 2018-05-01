package com.asg.florafauna;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.VolleyError;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.widget.Toast.LENGTH_LONG;
import static com.asg.florafauna.SearchActivity.INTENT_EXTRA_SPECIES_NAME;

/**
 * Created by steven on 3/2/18.
 */

public class SpeciesInfoActivity extends AppCompatActivity
{
    //must be global because it has to be set in onCreate, but used in downloadImageTask
    private int devHeight;
    //global for dirty hack so i don't have to pass it through 3 functions
    private String scientificName;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(ThemeCreator.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speciesinfo);
        if (getSupportActionBar() != null) {
            FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_speciesinfo);
        }

        dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);

        // SSL Certification for webcalls
        if (BuildConfig.DEBUG) {
            SSLCertificates.trustAll();
        }

        /* Data population */
        // Get scientific name sent by search
        scientificName = title(getIntent().getStringExtra(INTENT_EXTRA_SPECIES_NAME));

        //hack to get scientific name bold when common name isn't available
        //declare both common and scientific name Text views
        TextView scientificNameTV = findViewById(R.id.ScientificName);
        TextView commonNameTV = findViewById(R.id.CommonName);
        //hide the scientific name text view
        //will be unhidden when a common name is found
        //TODO setmaxheight in xml (default) to 0, only expand after common name
        scientificNameTV.setMaxHeight(0);
        //put scientific name in the common name text view(configured for larger text and bold)
        commonNameTV.setText(scientificName);

        //set device  height so it can be used later for scaling images to the right size
        devHeight=this.getResources().getDisplayMetrics().heightPixels;

        getID(this, scientificName);
    }

    // Pull relevant info from the search page and from the eol information page
    private void getID(final Context context,String scientificName)
    {
        // Build query that contains the name from search, and set default options
        String query = "http://eol.org/api/search/1.0.json?q=" + scientificName.replaceAll(" ", "+") + "&page=1&exact=true&filter_by_taxon_concept_id=&filter_by_hierarchy_entry_id=&filter_by_string=&cache_ttl=";
        Log.i("NameQuery", query);

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

                            // link to EoL for extra information
                            String eolLink = results.getString("link");

                            //Set link to EoL page on display
                            TextView eolLinkTV = findViewById(R.id.EoLLink);
                            //format so links work with different displayed text
                            String html="<A href=\""+eolLink+"\" target=_blank>View on Encyclopedia of Life</A>";
                            //format based on html tags instead of manually setting
                            eolLinkTV.setText(Html.fromHtml(html));
                            //allow the link to be followed
                            eolLinkTV.setMovementMethod(LinkMovementMethod.getInstance());

                            //get id
                            int ID=results.getInt("id");

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

    private void getData(final Context context, int ID)
    {
        //create query with ID from previous call, and default options
        String query = "http://eol.org/api/pages/1.0.json?batch=false&id=" + ID + "&images_per_page=100&images_page=1&videos_per_page=0&videos_page=0&sounds_per_page=0&sounds_page=0&maps_per_page=0&maps_page=0&texts_per_page=1&texts_page=1&subjects=overview&licenses=all&details=true&common_names=true&synonyms=false&references=false&taxonomy=false&vetted=1&cache_ttl=&language=en";
        Log.i("IDquery", query);

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
                        dialog.dismiss();
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
        String commonName="";
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
                if(record.has("eol_preferred"))
                {
                    pref = record.getBoolean("eol_preferred");
                }
                else
                {
                    pref = false;
                }
                //if this is an english name and a preferred name, its what we want
                //could remove pref to get a list of possible names
                if (en && pref)
                {
                    commonName = title(record.getString("vernacularName").trim());
                }
            }
        }
        catch (Exception e)
        {
            Log.e("Error GetCommonName", e.toString());
        }

        TextView commonNameTV = findViewById(R.id.CommonName);
        TextView scientificNameTV = findViewById(R.id.ScientificName);

        if(commonName.equals(""))
        {
            Log.i("commonName","not found");
        }
        else
        {
            scientificNameTV.setText(scientificName);
            commonNameTV.setText(commonName);
            scientificNameTV.setMaxHeight(100);
        }
        //set common name on display
    }

    //fetches and then cleans up descriptions, adds newlines, removes HTML
    private void getDescription(JSONObject response)
    {
        String description="";
        //pull description from JSON
        try
        {
            description = response.getJSONArray("dataObjects").getJSONObject(0).getString("description");
        }
        catch (Exception e)
        {
            Log.e("Error GetDescription", e.toString());
        }
        if (description.equals(""))
        {
            description="No description found.";
        }
        else if(description.contains("Links:<br>"))
        {
            description=description.substring(0,description.indexOf("Links:<br>"));
        }
        //make sure data was found or set error message

        //Set description on display
        TextView descriptionTV = findViewById(R.id.Description);
        descriptionTV.setText(Html.fromHtml(description));
        descriptionTV.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void getImageLink(JSONObject response)
    {
        //TODO this still runs in the background even if the activity is closed
        //main problem is all the async tasks, DONE happens after ~1 second
        //https://stackoverflow.com/questions/15185063/android-how-to-stop-cancel-asynctask-when-too-many-asynctask-are-running
        //applies to android home, back, and taskview buttons.
        //applies to app back button
        //killing entire app fixes
        String imageLink = "";
        try
        {
            JSONArray imageArray=response.getJSONArray("dataObjects");
            ArrayList<String> imageLinkArrayList = new ArrayList<>();
            //loop through array, object 0 was description
            //imageArray is a bad name, but that's what most of the data is
            //may want to lower the number of images possible
            /*for(int i=1;i<imageArray.length();i++)*/
            for(int i=1;i<10;i++)
            {
                JSONObject imageObject=imageArray.getJSONObject(i);
                if(imageObject.has("mediaURL")&&imageObject.has("rightsHolder"))
                {
                    //name of image owner
                    String rights="Rights Holder: "+imageObject.getString("rightsHolder");

                    imageLink = imageObject.getString("mediaURL");

                    //create view to place image in
                    ImageView imageView = new ImageView(this);

                    //pass that view and the link to the image to have the downloader download and fill
                    //also pass the information for the rights holder
                    //has to be set because it needs to be async or will never get filled
                    String noHttpImageLink = imageLink.substring(imageLink.indexOf("//") + 2, imageLink.length());
                    Log.i("noHttpImageLink", noHttpImageLink);
                    if (!imageLinkArrayList.contains(noHttpImageLink)) {
                        Log.i("imageLink", imageLink);
                        imageLinkArrayList.add(noHttpImageLink);
                        new DownloadImageTask(imageView).execute(imageLink,rights);
                    }
                }
            }
            Log.i("ImageTestDone","DONE");
        }
        catch(Exception e)
        {
            Log.e("Error GetImageLink",e.toString());
        }
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


/*Image downloader and display*/
    //Extreme thanks to https://stackoverflow.com/a/10868126 for solving this
    //download and display an image given a URL
    //DO NOT MAKE THIS STATIC - breaks findViewByID
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
    {
        ImageView bmImage;
        LinearLayout layout = (LinearLayout) findViewById(R.id.linear);
        String rights;
        private DownloadImageTask(ImageView bmImage)
        {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls)
        {
            //only sending 1 image at a time but this was breaking if I just used String url
            String urlPath = urls[0];
            rights=urls[1];
            Bitmap mIcon11 = null;
            try
            {
                //connect and download bitmap of the desired image
                //these 4 lines are why this has to be an AsyncTask
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
            //some images (upload.wikimedia being the most common offender) don't download through the app
            //so make sure an image exists (~2/3 images are null)
            if (result==null)
            {
                Log.i("ImageTest","null");
            }
            else
            {
                Log.i("ImageTest","Not null");
                //values for calculating the scaling for the image

                //height and width of the raw image from EoL
                int oldHeight=result.getHeight();
                int oldWidth=result.getWidth();

                //what percent of the height of the screen should be filled
                int percent=30;

                //do math to make the new height that percent of the screen
                int newHeight=devHeight/(100/percent);
                //more math to preserve ratio
                int newWidth=oldWidth*newHeight/oldHeight;
                //scale the image to set height and width
                result=Bitmap.createScaledBitmap(result,newWidth,newHeight,false);

                //add bitmap to imageView, not on the screen yet
                bmImage.setImageBitmap(result);
                bmImage.setPadding(8,0,8,0);

                bmImage.setClickable(true);
                bmImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast toast=new Toast(getApplicationContext());
                        toast.setGravity(Gravity.TOP, 0,0);
                        toast.makeText(v.getContext(), rights,LENGTH_LONG).show();
                    }
                });

                //add to the "album" at the top of the page
                layout.addView(bmImage);
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
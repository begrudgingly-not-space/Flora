package com.asg.florafauna;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.os.AsyncTask;
import java.io.InputStream;

import static com.asg.florafauna.SearchActivity.INTENT_EXTRA_SPECIES_NAME;

/**
 * Created by steven on 3/2/18.
 */

public class SpeciesInfoActivity extends AppCompatActivity
{

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
            //sName="No Species Information Found"
            sName = "Ursus Arctos";
            // link = "http://eol.org/pages/326447/overview";
        }

        //create object for the animal selected
        SpeciesInfo species = new SpeciesInfo(this,sName);

        TextView SNgetText = findViewById(R.id.ScientificName);
        SNgetText.setText(String.valueOf(species.getScientificName()));

        TextView CNgetText = findViewById(R.id.CommonName);
        CNgetText.setText(String.valueOf(species.getCommonName()));

        TextView DgetText = findViewById(R.id.Description);
        DgetText.setText(String.valueOf(species.getDescription()));

        TextView ILgetText = findViewById(R.id.ImageLink);
        ILgetText.setText(String.valueOf(species.getImageLink()));

        TextView ELgetText = findViewById(R.id.EoLLink);
        ELgetText.setText(String.valueOf(species.getEolLink()));

        //action bar creation copied form HelpActivity.java
        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_speciesinfo);


        //stolen from https://stackoverflow.com/questions/5776851/load-image-from-url#10868126
        new DownloadImageTask((ImageView) findViewById(R.id.imageView1)).execute(species.getImageLink());
    }

    //opens settings
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
    }

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
}



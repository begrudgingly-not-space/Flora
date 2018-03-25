package com.asg.florafauna;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

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
        SpeciesInfo test = new SpeciesInfo(this,sName);

        TextView SNgetText = findViewById(R.id.ScientificName);
        SNgetText.setText(String.valueOf(test.getScientificName()));

        TextView CNgetText = findViewById(R.id.CommonName);
        CNgetText.setText(String.valueOf(test.getCommonName()));


        TextView DgetText = findViewById(R.id.Description);
        DgetText.setText(String.valueOf(test.getDescription()));

        TextView ILgetText = findViewById(R.id.ImageLink);
        ILgetText.setText(String.valueOf(test.getImageLink()));

        TextView ELgetText = findViewById(R.id.EoLLink);
        ELgetText.setText(String.valueOf(test.getEolLink()));

        //action bar creation copied form HelpActivity.java
        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_speciesinfo);

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
}
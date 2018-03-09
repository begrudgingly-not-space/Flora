package com.asg.florafauna;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by steven on 3/2/18.
 */

public class SpeciesInfoActivity extends AppCompatActivity
{
    //copied from HelpActivity.java, no clue how this works
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speciesinfo);

        //default variables to take values from the results menu from the search/history
        String sName;
        String link;

        //pull values passed by previous page
        //stolen from:
        //https://stackoverflow.com/questions/2091465/how-do-i-pass-data-between-activities-in-android-application#7325248
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            //link=extras.getString("link");
            sName=extras.getString("sName");
        }
        else //default values for testing before pages are linked properly
        {
            sName="Ursus arctos";
            //link="http://eol.org/pages/326447/overview";
        }

        //create object for the animal selected
        SpeciesInfo test = new SpeciesInfo(sName);

        //copied from stackoverflow to set the text in the ScientificName section to what i want
        //stolen from:
        //https://stackoverflow.com/questions/5821051/how-to-display-the-value-of-a-variable-on-the-screen#5821117
        TextView SNgetText = findViewById(R.id.ScientificName);
        SNgetText.setText(String.valueOf(test.getScientificName()));

        //action bar creation copied form HelpActivity.java
        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_help);
    }
}
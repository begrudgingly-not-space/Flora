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
        String name;
        String link;

        //pull values passed by previous page
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            name=extras.getString("link");
            //link=extras.getString("name");
        }
        else //default values for testing before pages are linked properly
        {
            name="Ursus arctos";
            //link="http://eol.org/pages/326447/overview";
        }

        //create object for the animal selected
        SpeciesInfo test = new SpeciesInfo(name);

        //copied from stackoverflow to set the text in the ScientificName section to what i want
        //(the scientific name from the object created)
        //https://stackoverflow.com/questions/5821051/how-to-display-the-value-of-a-variable-on-the-screen#5821117
        TextView SNgetText = findViewById(R.id.ScientificName);
        SNgetText.setText(String.valueOf(test.getScientificName()));

        //action bar creation copied form HelpActivity.java
        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_help);
    }
}
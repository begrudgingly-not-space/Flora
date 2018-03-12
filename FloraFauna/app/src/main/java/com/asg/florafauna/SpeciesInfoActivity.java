package com.asg.florafauna;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by steven on 3/2/18.
 */

public class SpeciesInfoActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speciesinfo);

        Bundle extras = getIntent().getExtras();
        String name;
        if (extras != null)
        {
            name = extras.getString("name");
        }
        else
        {
            name = "Gorilla Gorilla";
        }
        SpeciesInfo test = new SpeciesInfo(name);


        TextView SNgetText = findViewById(R.id.ScientificName);
        SNgetText.setText(String.valueOf(test.getScientificName()));

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_speciesinfo);
    }

    public void openHelp(View view){
        Intent intent = new Intent(SpeciesInfoActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    // opens settings
    public void openSettings(View view){
        Intent intent = new Intent(SpeciesInfoActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openSearch(View view){
        Intent intent = new Intent(SpeciesInfoActivity.this, SearchActivity.class);
        startActivity(intent);
    }

    public void goBack(View view){
        /* closes the activity */
        finish();
    }
}
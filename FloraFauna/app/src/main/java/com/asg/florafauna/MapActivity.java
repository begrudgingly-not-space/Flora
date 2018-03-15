package com.asg.florafauna;

import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


/**
 * Created by brada on 3/13/2018.
 */

public class MapActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_map);
    }

    public void openHelp(View view){
        Intent intent = new Intent(MapActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    public void openSettings(View view){
        Intent intent = new Intent(MapActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openSearch(View view){
        Intent intent = new Intent(MapActivity.this, SearchActivity.class);
        startActivity(intent);
    }

    public void goBack(View view){
        finish();
    }

}

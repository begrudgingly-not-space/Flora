package com.asg.florafauna;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by brada on 2/2/2018.
 */

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_help);
    }

    public void openHelp(View view){
        Intent intent = new Intent(HelpActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    //opens settings
    public void openSettings(View view){
        Intent intent = new Intent(HelpActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void goBack(View view){
        finish();
    }

}

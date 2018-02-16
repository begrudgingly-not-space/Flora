package com.asg.florafauna;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_settings);
    }

    public void openHelp(View view){
        Intent intent = new Intent(SettingsActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    //opens settings
    public void openSettings(View view){
        Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void goBack(View view){
        /* closes the activity */
        finish();
    }

}

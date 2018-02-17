package com.asg.florafauna;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.asg.florafauna.FloraFaunaActionBar;

/**
 * Created by kkey on 2/1/2018.
 */

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_search);
    }

    public void openHelp(View view){
        Intent intent = new Intent(SearchActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    //opens settings
    public void openSettings(View view){
        Intent intent = new Intent(SearchActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed()
    {
        // Disables going back by manually pressing the back button
    }
}
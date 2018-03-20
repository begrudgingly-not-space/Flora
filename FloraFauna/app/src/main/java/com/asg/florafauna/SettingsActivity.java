package com.asg.florafauna;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_settings);
    }

    @Override
    public void onBackPressed()
    {
        // Disables going back by manually pressing the back button
    }

    public void openHelp(View view){
        Intent intent = new Intent(SettingsActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    public void openSearch(View view){
        Intent intent = new Intent(SettingsActivity.this, SearchActivity.class);
        startActivity(intent);
    }

    public void goBack(View view){
        /* closes the activity */
        setResult(RESULT_OK, null);
        finish();

    }

    public void clearHistory(View view){
        File dir = getFilesDir();
        File file = new File(dir, "history");
        file.delete();

    }

}

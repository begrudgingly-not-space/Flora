package com.asg.florafauna;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import static com.asg.florafauna.FloraFaunaActionBar.createActionBar;

/**
 * Created by brada on 2/2/2018.
 */

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeCreator.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        if (getSupportActionBar() != null) {
            createActionBar(getSupportActionBar(), R.layout.ab_help);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.help_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_home:
                Intent search_intent = new Intent(HelpActivity.this, SearchActivity.class);
                startActivity(search_intent);
                return true;
            case R.id.action_settings:
                Intent settings_intent = new Intent(HelpActivity.this, SettingsActivity.class);
                startActivity(settings_intent);
                return true;
            case R.id.action_map:
                Intent intent = new Intent(HelpActivity.this, MapActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_recording:
                Intent recordings_intent = new Intent(HelpActivity.this, PersonalRecordingsActivity.class);
                startActivity(recordings_intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void goBack(View view){
        finish();
    }
}

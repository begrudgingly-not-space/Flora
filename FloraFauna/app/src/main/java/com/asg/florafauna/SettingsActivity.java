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
        Toolbar actionToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(actionToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.action_home:
                startActivity(new Intent(this, SearchActivity.class));
                return true;

            case R.id.action_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goBack(View view){
        /* closes the activity */
        finish();
    }

}

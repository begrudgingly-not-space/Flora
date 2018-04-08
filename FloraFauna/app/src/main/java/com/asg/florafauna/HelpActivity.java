package com.asg.florafauna;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by brada on 2/2/2018.
 */

public class HelpActivity extends AppCompatActivity {

    private String[] themeArray = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.AppTheme);
        try {
            //opens the file to read its contents
            FileInputStream fis = this.openFileInput("theme");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            themeArray[0] = reader.readLine(); //adds the line to the temp array
            reader.close();
            isr.close();
            fis.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        if (themeArray[0].equals("Green")){
            setTheme(R.style.AppTheme);
        }
        else if (themeArray[0].equals("Blue")){
            setTheme(R.style.AppTheme);
        }
        else if (themeArray[0].equals("Mono")){
            setTheme(R.style.AppTheme);
        }
        else if (themeArray[0].equals("Cherry")){
            setTheme(R.style.AppThemeCherry);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_help);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //opens settings
    public void openSettings(View view){
        Intent intent = new Intent(HelpActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openSearch(View view){
        Intent intent = new Intent(HelpActivity.this, SearchActivity.class);
        startActivity(intent);
    }

    public void goBack(View view){
        finish();
    }

}

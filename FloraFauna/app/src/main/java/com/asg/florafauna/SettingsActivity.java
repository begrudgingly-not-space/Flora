package com.asg.florafauna;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_settings);


        // save mileage
        Spinner mileage = (Spinner) findViewById(R.id.miles);
        mileage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String text = parent.getItemAtPosition(pos).toString();

                try{
                    FileOutputStream fOut = openFileOutput("mileage", MODE_PRIVATE);
                    OutputStreamWriter osw = new OutputStreamWriter(fOut);
                    osw.write(text);
                    osw.flush();
                    osw.close();
                    fOut.close();
                }
                catch (FileNotFoundException e){
                    e.printStackTrace();
                }
                catch (IOException e){
                    Log.e("Exception", "Failed to save history: " + e.toString());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // save style/theme
        Spinner theme = (Spinner) findViewById(R.id.Style);
        theme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String text = parent.getItemAtPosition(pos).toString();

                try{
                    FileOutputStream fOut = openFileOutput("theme", MODE_PRIVATE);
                    OutputStreamWriter osw = new OutputStreamWriter(fOut);
                    osw.write(text);
                    osw.flush();
                    osw.close();
                    fOut.close();
                }
                catch (FileNotFoundException e){
                    e.printStackTrace();
                }
                catch (IOException e){
                    Log.e("Exception", "Failed to save history: " + e.toString());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_home:
                Intent search_intent = new Intent(SettingsActivity.this, SearchActivity.class);
                startActivity(search_intent);
                return true;
            case R.id.action_help:
                Intent help_intent = new Intent(SettingsActivity.this, HelpActivity.class);
                startActivity(help_intent);
                return true;
            case R.id.action_map:
                Intent intent = new Intent(SettingsActivity.this, MapActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        setResult(RESULT_OK, null);
        finish();
    }

    /*public void openHelp(View view){
        Intent intent = new Intent(SettingsActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    public void openSearch(View view){
        Intent intent = new Intent(SettingsActivity.this, SearchActivity.class);
        startActivity(intent);
    }*/

    public void goBack(View view){
        /* closes the activity */
        setResult(RESULT_OK, null);
        finish();
    }

    public void clearHistory(View view){
        File dir = getFilesDir();
        File file = new File(dir, "history");
        file.delete();
        Log.d("Clear History", "History cleared.");
        Toast.makeText(this,"Clear History", Toast.LENGTH_SHORT).show();
    }


    public void saveSettings(){





/*
        try{
            FileOutputStream fOut = openFileOutput("history", MODE_PRIVATE);
            fOut.write(database);
            fOut.flush();
            fOut.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            Log.e("Exception", "Failed to save history: " + e.toString());
        }
*/
    }

}

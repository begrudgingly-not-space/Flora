package com.asg.florafauna;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SettingsActivity extends AppCompatActivity {
    private String[] themeArray = new String[1];
    private String[] mileageArray = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeCreator.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (getSupportActionBar() != null) {
            FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_settings);
        }

        // SSL Certification for webcalls
        if (BuildConfig.DEBUG) {
            SSLCertificates.trustAll();
        }

        // Save the settings to file
        Spinner mileage = findViewById(R.id.miles);
        Spinner theme = findViewById(R.id.Style);

        // Sets the theme spinner
        try {
            // Opens the file to read its contents
            FileInputStream fis = this.openFileInput("theme");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            themeArray[0] = reader.readLine(); //adds the line to the temp mileageArray
            reader.close();
            isr.close();
            fis.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // Sets the mileage spinner
        try {
            // Opens the file to read its contents
            FileInputStream fis = this.openFileInput("mileage");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            mileageArray[0] = reader.readLine(); // Adds the line to the temp mileageArray
            reader.close();
            isr.close();
            fis.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // Sets the mileage spinner
        ArrayAdapter<String> mileageAdapter = (ArrayAdapter<String>) mileage.getAdapter();
        mileage.setSelection((mileageAdapter).getPosition(mileageArray[0]));

        // Sets the theme spinner
        ArrayAdapter<String> themeAdapter = (ArrayAdapter<String>) theme.getAdapter();
        theme.setSelection((themeAdapter).getPosition(themeArray[0]));

        // Method to save settings to file
        saveSettings(mileage, theme);
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
                Intent map_intent = new Intent(SettingsActivity.this, MapActivity.class);
                startActivity(map_intent);
                return true;
            case R.id.action_recording:
                Intent recording_intent = new Intent(SettingsActivity.this, PersonalRecordingsActivity.class);
                startActivity(recording_intent);
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

    public void goBack(View view){
        // Closes the activity
        setResult(RESULT_OK, null);
        finish();
    }

    public void clearHistory(View view){
        File dir = getFilesDir();
        File file = new File(dir, "history");
        file.delete();
        Log.d("Clear History", "History cleared.");
        Toast.makeText(this,"History Cleared", Toast.LENGTH_SHORT).show();
    }

    private void saveSettings(Spinner mileage, Spinner theme){
        // Save mileage
        mileage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String text = parent.getItemAtPosition(pos).toString();

                try{
                    FileOutputStream fOut = openFileOutput("mileage", MODE_PRIVATE); // open file 'mileage' to write to it
                    OutputStreamWriter osw = new OutputStreamWriter(fOut); // required to 'write'
                    osw.write(text);
                    //clean up
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

        // Save style/theme
        theme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String text = parent.getItemAtPosition(pos).toString();

                try{
                    FileOutputStream fOut = openFileOutput("theme", MODE_PRIVATE); //open file 'theme'
                    OutputStreamWriter osw = new OutputStreamWriter(fOut); // required to 'write' to file
                    osw.write(text);
                    // Clean up
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

                if (!text.equals(themeArray[0])){
                    Intent refresh = new Intent(SettingsActivity.this, SettingsActivity.class);
                    startActivity(refresh);
                    finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }
}

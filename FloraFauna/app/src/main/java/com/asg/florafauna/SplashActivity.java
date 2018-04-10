package com.asg.florafauna;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SplashActivity extends AppCompatActivity {
    private String[] themeArray = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String text = "Green";

        try{
            FileOutputStream fOut = openFileOutput("theme", MODE_PRIVATE); //open file 'theme'
            OutputStreamWriter osw = new OutputStreamWriter(fOut); // required to 'write' to file
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
            Log.e("Exception", "Failed to set theme: " + e.toString());
        }






        Intent intent = new Intent(SplashActivity.this, SearchActivity.class);
        //Intent intent = new Intent(SplashActivity.this, SpeciesInfoActivity.class);

        startActivity(intent);
        finish();
    }
}

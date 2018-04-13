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

        //this code checks to see if a theme has been set, then if it hasn't it sets the theme to Green
        try {
            //opens the file to read its contents
            FileInputStream fis = this.openFileInput("theme");
            fis.close();
        }
        catch (IOException e){
            Log.e("Exception", "Failed to set theme: " + e.toString());
            try{
                FileOutputStream fOut = openFileOutput("theme", MODE_PRIVATE); //open file 'theme'
                OutputStreamWriter osw = new OutputStreamWriter(fOut); // required to 'write' to file
                osw.write("Green");
                //clean up
                osw.flush();
                osw.close();
                fOut.close();
            }
            catch (FileNotFoundException f){
                f.printStackTrace();
            }
            catch (IOException g){
                Log.e("Exception", "Failed to set theme: " + g.toString());
            }
        }

        Intent intent = new Intent(SplashActivity.this, SearchActivity.class);
        //Intent intent = new Intent(SplashActivity.this, SpeciesInfoActivity.class);

        startActivity(intent);
        finish();
    }
}

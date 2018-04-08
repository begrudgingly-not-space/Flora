package com.asg.florafauna;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class SplashActivity extends AppCompatActivity {
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

        Intent intent = new Intent(SplashActivity.this, SearchActivity.class);
        //Intent intent = new Intent(SplashActivity.this, SpeciesInfoActivity.class);

        startActivity(intent);
        finish();
    }
}

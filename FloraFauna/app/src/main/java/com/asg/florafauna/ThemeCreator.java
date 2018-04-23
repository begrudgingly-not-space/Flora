package com.asg.florafauna;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by kkey on 2/16/2018.
 */

class ThemeCreator {
    private static String[] themeArray = new String[1];

    static int getTheme(Context context) {
        try {
            // Opens the file to read its contents
            FileInputStream fis = context.openFileInput("theme");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            themeArray[0] = reader.readLine(); // Adds the line to the temp array
            reader.close();
            isr.close();
            fis.close();
        }
        catch (IOException e){
                Log.e("IOException", "Failed to set theme: " + e.toString());
                try{
                    FileOutputStream fout = context.openFileOutput("theme", MODE_PRIVATE); // Open file 'theme'
                    OutputStreamWriter osw = new OutputStreamWriter(fout); // Required to 'write' to file
                    osw.write("Green");
                    // Clean up
                    osw.flush();
                    osw.close();
                    fout.close();
                }
                catch (FileNotFoundException f){
                    f.printStackTrace();
                }
                catch (IOException g){
                    Log.e("Exception", "Failed to set theme: " + g.toString());
                }
                themeArray[0] = "Green";
        }

        if (themeArray[0].equals("Green")) {
            return R.style.AppTheme;
        }
        else if (themeArray[0].equals("Blue")) {
            return R.style.AppThemeBlue;
        }
        else if (themeArray[0].equals("Mono")) {
            return R.style.AppThemeMono;
        }
        else {
            return R.style.AppThemeCherry;
        }
    }
}

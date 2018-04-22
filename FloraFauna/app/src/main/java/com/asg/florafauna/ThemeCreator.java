package com.asg.florafauna;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
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
    static int getTheme(Context context, String themeArray) {
        // Sets default theme if none found
        File themes = new File("theme");
        if(!themes.exists())
        {
            //sets default
            try{
                FileOutputStream fOut = context.openFileOutput("theme", MODE_PRIVATE); // Open file 'theme'
                OutputStreamWriter osw = new OutputStreamWriter(fOut); // Required to 'write' to file
                osw.write("Green");
                //clean up
                osw.flush();
                osw.close();
                fOut.close();
            }
            catch (FileNotFoundException x){
                x.printStackTrace();
            }
            catch (IOException x){
                Log.e("Exception", "Failed to save history: " + x.toString());
            }
        }

        try {
            //opens the file to read its contents
            FileInputStream fis = context.openFileInput("theme");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            themeArray = reader.readLine(); //adds the line to the temp array
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

        if (themeArray.equals("Green")) {
            return R.style.AppTheme;
        }
        else if (themeArray.equals("Blue")) {
            return R.style.AppThemeBlue;
        }
        else if (themeArray.equals("Mono")) {
            return R.style.AppThemeMono;
        }
        else {
            return R.style.AppThemeCherry;
        }
    }
}

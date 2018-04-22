package com.asg.florafauna;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

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

            themeArray[0] = reader.readLine(); //adds the line to the temp array
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

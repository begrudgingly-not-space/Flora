package com.asg.florafauna;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by brada on 3/2/2018.
 */

class CountyFinder {
    static String countyFinder(Context context, String state, String county){
        InputStream in = context.getResources().openRawResource(R.raw.countyfips);
        Scanner s = new Scanner(in);
        String countyFips = "";

        while (s.hasNextLine()) {
            String str = s.findInLine("[0-9][0-9][0-9][0-9][0-9] " + county + " " + state);

            if (str != null) {
                countyFips = str;
                break;
            }

            s.nextLine();
        }

        if (!countyFips.equals("")) {
            countyFips = countyFips.substring(0, 5);
        }

        Log.d("scanresults", countyFips);

        return countyFips;
    }
}

package com.asg.florafauna;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by brada on 3/2/2018.
 */

public class CountyFinder {

    public static String countyFinder(Context context, String state, String county){
        InputStream in = context.getResources().openRawResource(R.raw.countyfips);
        Scanner s = new Scanner(in);

        String result = "";

        while (s.hasNextLine()) {
            String str = s.findInLine("[0-9][0-9][0-9][0-9][0-9] " + county + " " + state);

            if(str != null) {
                result = str;
                break;
            }

            s.nextLine();
        }

        result = result.substring(0,5);
        Log.d("scanresults", result);

        return result;

        /*
        Log.d("county", state);
        Log.d("county", county);

        if (state.equals("Louisiana")) {
            if (county.equals("Bossier"))
                return "22015";
            else if (county.equals("Caddo"))
                return "22017";
            else
                return "22019";
        }
        else{
            if (county.equals("Anderson"))
                return "48001";
            else if (county.equals("Andrews"))
                return "48003";
            else
                return "48005";
        }

        //return countyFips;
        */
    }
}

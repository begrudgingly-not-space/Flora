package com.asg.florafauna;

import android.util.Log;

/**
 * Created by brada on 3/2/2018.
 */

public class CountyFinder {

    public static String countyFinder(String state, String county){
        //String countyFips;

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
    }
}

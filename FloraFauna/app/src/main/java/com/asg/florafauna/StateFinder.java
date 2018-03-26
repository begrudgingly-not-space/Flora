package com.asg.florafauna;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by brada on 3/17/2018.
 */

public class StateFinder {
    public static String stateFinder(Context context, String state){
        InputStream in = context.getResources().openRawResource(R.raw.stateabbr);
        Scanner s = new Scanner(in);

        String stateName = "";

        while (s.hasNextLine()) {
            String str = s.findInLine(".* " + state);

            if (str != null) {
                stateName = str;
                break;
            }

            s.nextLine();
        }

        if (!stateName.equals("")) {
            stateName = stateName.substring(0, stateName.length() - 3);
            stateName = stateName.substring(0,1) + stateName.substring(1).toLowerCase();
        }

        Log.d("scanresults", stateName);

        return stateName;
    }
}

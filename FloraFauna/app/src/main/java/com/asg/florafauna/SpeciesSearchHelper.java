package com.asg.florafauna;


import android.text.TextUtils;

/**
 * Created by shelby on 2/7/18.
 */

public class SpeciesSearchHelper
{
    protected String capitalizeName(String name)
    {
        String capitalizedName;
        String arr[] = name.split(" ");
        for(int i = 0; i < arr.length; i++)
        {
            arr[i] = arr[i].substring(0, 1).toUpperCase() + arr[i].substring(1);
        }

        capitalizedName = TextUtils.join(" ", arr);
        return capitalizedName;
    }

}

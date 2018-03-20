package com.asg.florafauna;


import android.text.TextUtils;

/**
 * Created by shelby on 2/7/18.
 */

public class SpeciesSearchHelper
{

    protected int getNameLength(String name)
    {
        int nameLength;

        // remove trailing whitespace
        name = name.trim();

        if(name.isEmpty())
        {
            return 0;
        }
        else
        {
            // this should ensure that multiple spaces together in a string are ignored
            nameLength = name.split("\\s+").length;
        }

        return nameLength;

    }

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

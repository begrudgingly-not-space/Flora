package com.asg.florafauna;

/**
 * Created by steven on 2/8/18.
 */

import java.util.*;
import java.io.*;
import java.net.*;
import org.json.*;

public class WebTest
{
    public static void main(String args[])
    {
        try
        {
            URL page = new URL("http://eol.org/api/search/1.0.json?q=Ursus+arctos&page=1&exact=true&filter_by_taxon_concept_id=&filter_by_hierarchy_entry_id=&filter_by_string=&cache_ttl=");

            URLConnection connection=page.openConnection();

            connection.setDoInput(true);
            InputStream inStream = connection.getInputStream();
            BufferedReader input =new BufferedReader(new InputStreamReader(inStream));
            String line;
            while ((line = input.readLine()) != null)
            {
                System.out.println(line);//this is a json
                JSONArray obj = new JSONArray(line);
                System.out.println("got here");
                //System.out.println(obj.getString("link"));
                //System.out.println(obj.toString());
            }
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
    }
}

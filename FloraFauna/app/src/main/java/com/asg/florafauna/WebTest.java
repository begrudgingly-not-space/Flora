package com.asg.florafauna;

/**
 * Created by steven on 2/8/18.
 */

import java.io.*;
import java.net.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.jsoup.Connection.Response;



public class WebTest
{

    private static Document doc;
    private static Response res;

    public static void main(String args[])
    {
        try
        {
            String query=eolQuery("Ursus arctos");
            //URL page = new URL("http://eol.org/api/search/1.0.json?q=Ursus+arctos&page=1&exact=true&filter_by_taxon_concept_id=&filter_by_hierarchy_entry_id=&filter_by_string=&cache_ttl=");
            System.out.println(query);
            URL page = new URL(query);

            URLConnection connection=page.openConnection();

            connection.setDoInput(true);
            InputStream inStream = connection.getInputStream();
            BufferedReader input =new BufferedReader(new InputStreamReader(inStream));
            String line;
            while ((line = input.readLine()) != null)
            {
                //System.out.println(line);//this is a json
                int start = line.indexOf("http");
                int stop = line.indexOf("?");
                String link= line.substring(start,stop);
                res = Jsoup.connect(link).timeout(10000).execute();
                doc=res.parse();
                //System.out.println(doc);
                String hold=doc.toString();
                //System.out.println(hold);
                start=hold.indexOf("<h4>Description</h4>")+21;
                stop=hold.indexOf("\n", start);
                //System.out.println(start);
                //System.out.println(stop);
                String description=hold.substring(start,stop);
                //System.out.println(description);
                //System.out.println(doc.select("h4"));
            }
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
    }
    private static String eolQuery(String name)
    {
        String first="http://eol.org/api/search/1.0.json?q=";
        String last="&page=1&exact=true&filter_by_taxon_concept_id=&filter_by_hierarchy_entry_id=&filter_by_string=&cache_ttl=";
        name=name.replaceAll(" ","+");
        return first+name+last;
    }
    private static String gbifQuery(String name)
    {
        String first = "http://api.gbif.org/v1/species?name=";
        name=name.replaceAll(" ","+");
        return first+name;
    }
}

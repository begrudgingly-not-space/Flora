package com.asg.florafauna;

/**
 * Created by steven on 2/8/18.
 */

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.Connection.Response;

public class WebTest
{

    private static Document doc;
    private static Response res;
    public static void main(String args[])
    {
        System.out.println(setEOLDescription("Panthera leo"));
    }
    private static String setEOLDescription(String name)
    {
        try
        {
            String query=eolQuery(name);

            String json = Jsoup.connect(query).ignoreContentType(true).execute().body();
            String link = json.substring(json.indexOf("http"),json.indexOf("?"));

            String page = Jsoup.connect(link).timeout(10000).execute().parse().toString();
            int start = page.indexOf("</h4>",page.indexOf("<h4>Description"))+6;
            int stop = page.indexOf("\n", start);
            String description=page.substring(start,stop);
            return description;
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
            return "error";
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

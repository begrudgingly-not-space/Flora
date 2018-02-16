package com.asg.florafauna;

/**
 * Created by steven on 2/8/18.
 */

import org.jsoup.*;

public class SpeciesInfo
{
    String name;
    String eolLink;
    String description;

    public SpeciesInfo(String name)
    {
        this.name=name;
    }
    private void setEOLDescription(String name)
    {
        try
        {
            String query=eolQuery(name);

            String json = Jsoup.connect(query).ignoreContentType(true).execute().body();
            eolLink = json.substring(json.indexOf("http"),json.indexOf("?"));

            String page = Jsoup.connect(eolLink).timeout(10000).execute().parse().toString();
            int start = page.indexOf("</h4>",page.indexOf("<h4>Description"))+6;
            int stop = page.indexOf("\n", start);
            description=page.substring(start,stop);
            //return description;
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
    }
    private String eolQuery(String name)
    {
        String first="http://eol.org/api/search/1.0.json?q=";
        String last="&page=1&exact=true&filter_by_taxon_concept_id=&filter_by_hierarchy_entry_id=&filter_by_string=&cache_ttl=";
        name=name.replaceAll(" ","+");
        return first+name+last;
    }
    private String gbifQuery(String name)
    {
        String first = "http://api.gbif.org/v1/species?name=";
        name=name.replaceAll(" ","+");
        return first+name;
    }
}

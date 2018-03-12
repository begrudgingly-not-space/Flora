package com.asg.florafauna;

/**
 * Created by steven on 2/8/18.
 */

import org.jsoup.*;

public class SpeciesInfo
{
    String scientificName;
    String commonName;
    String eolLink;
    String description;
    String imageLink;

    public SpeciesInfo(String name)
    {
        this.scientificName=name;
        setFromEOL(name);
    }
    private void setFromEOL(String name)
    {
        try
        {
            String query=eolQuery(name);

            String json = Jsoup.connect(query).ignoreContentType(true).execute().parse().toString();
            eolLink = json.substring(json.indexOf("http"),json.indexOf("?"));

            String page = Jsoup.connect(eolLink).timeout(10000).execute().parse().toString();
            int start = page.indexOf("</h4>",page.indexOf("<h4>Description"))+6;
            int stop = page.indexOf("\n", start);
            description = page.substring(start,stop);

            start = page.indexOf("<title>")+7;
            stop = page.indexOf("-",start);
            commonName = page.substring(start,stop);

            start = page.indexOf("<img alt");
            start = page.indexOf("src",start)+5;
            stop = page.indexOf("\"",start);
            imageLink = page.substring(start,stop);
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

    public String getScientificName()
    {
        return scientificName;
    }
    public String getCommonName()
    {
        return commonName;
    }
}

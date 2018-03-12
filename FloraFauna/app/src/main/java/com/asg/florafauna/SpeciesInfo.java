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

    //initializer for when only passed name(Search results from bison)
    public SpeciesInfo(String name)
    {
        this.scientificName=name;
        setFromEOL(name);
    }
    //initializer for when passed eol link, skips first search
    //unused for now
    /*
    public SpeciesInfo(String name, String link)
    {
        this.scientificName=name;
        this.eolLink=link;
    }*/

    //pull relevant info from the search page and from the eol information page
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

    //format query to search for an animal(exact name) on eol
    private String eolQuery(String name)
    {
        String first="http://eol.org/api/search/1.0.json?q=";
        String last="&page=1&exact=true&filter_by_taxon_concept_id=&filter_by_hierarchy_entry_id=&filter_by_string=&cache_ttl=";
        name=name.replaceAll(" ","+");
        return first+name+last;
    }

    //format query to search for an animal on gbif
    private String gbifQuery(String name)
    {
        String first = "http://api.gbif.org/v1/species?name=";
        name=name.replaceAll(" ","+");
        return first+name;
    }

    //getters to return values from the object
    public String getScientificName()
    {
        return scientificName;
    }
    public String getCommonName()
    {
        return commonName;
    }
    public String getDescription(){return description;}
    public String getImageLink(){return imageLink;}
    public String getEolLink(){return eolLink;}

}

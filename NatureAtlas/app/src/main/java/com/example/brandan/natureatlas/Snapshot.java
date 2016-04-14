package com.example.brandan.natureatlas;

import java.io.Serializable;

/**
 * Created by Brandan on 9/9/2015.
 */

//This class saves data if there is no internet connection to post data.
public class Snapshot implements Serializable
{
    public String species, organism, comm, phen, abund, nation, wild, name, lat, lon, ac;
    public Snapshot(String s, String o, String c, String p, String a, String na, String w, String la, String lo, String acc
                    )
    {
        species = s;
        organism = o;
        comm = c;
        phen = p;
        abund = a;
        nation = na;
        wild = w;
        lat = la;
        lon = lo;
        ac = acc;

    }
}

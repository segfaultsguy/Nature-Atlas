package com.example.brandan.natureatlas;


import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;


/**
 * Created by Brandan on 9/29/2015.
 */
/*
    What this class does:
    This is just to hold the individual data of each marker.
*/
public class MarkerData implements ClusterItem
{
    public String  species, organism,  commonName, recordID;
    public double lat, longitude;

    public MarkerData(String recordIDS, String typeS, Double latitudeS, Double longitudeS, String speciesS)
    {




        recordID = recordIDS;
        organism = typeS;
        lat = latitudeS ;
        longitude = longitudeS;
        species = speciesS;

    }
    @Override
    public LatLng getPosition()
    {
       return new LatLng(lat, longitude);
    }
}

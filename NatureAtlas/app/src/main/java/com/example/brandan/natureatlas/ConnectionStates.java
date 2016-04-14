package com.example.brandan.natureatlas;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;


/**
 * Created by Brandan Jablonski on 9/8/2015.
 */

/*
   What this class does:
   This class is an easy interface for me to access wifi, 3G/4G and location data without having
   to re-use code.
*/
public class ConnectionStates {
    private ConnectivityManager cm;
    private LocationManager lm;
    private Context context;

    //Default constructor. Passes the context.
    //Makes for less of a headache later.
    public ConnectionStates(Context context)
    {
        this.context = context;
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    //Does the user have the location services turned on?
    public boolean isLocationOn()
    {
        lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        try
        {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }


    //Check if mobile data is on - A.K.A. 3G/4G
    public boolean isMobileDataOn()
    {
        ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo =
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mobileInfo.isConnected();
    }

    public boolean isWifiOn()
    {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        return wifiManager.isWifiEnabled();
    }

}

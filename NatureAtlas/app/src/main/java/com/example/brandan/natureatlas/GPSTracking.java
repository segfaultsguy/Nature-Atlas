package com.example.brandan.natureatlas;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Brandan Jablonski on 9/8/2015.
 */

/*
    What this class does:
    This class does the GPS tracking for me using the phone's interface and Google APIs.
*/
public class GPSTracking implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private AtlasMap atlasM;
    private float accuracy;
    private Context context;
    //Flag for GPS status.
    private boolean isGPSEnabled = false;
    //Flag for requesting updates.
    private boolean requestUpdate = false;
    //Flag for network status
    private boolean isNetworkEnabled = false;
    private LocationManager locationManager;
    private Location location;
    private double latitude;
    private double longitude;

    private LocationListener locLis;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public GPSTracking(Context context, AtlasMap atlasMap)
    {
        this.context = context;
        atlasM = atlasMap;
        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        buildGoogleApiClient();
    }

    public GPSTracking(Context context)
    {
        this.context = context;
        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        buildGoogleApiClient();
    }
    public String getCountry()
    {
        List<Address> addresses = new ArrayList<Address>();
        if(isGPSEnabled())
        {
            GetMyLocation();
            Geocoder gc = new Geocoder(context, Locale.getDefault());

            try
            {
                addresses = gc.getFromLocation(GetLatitude(), GetLongitude(), 1);

            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        return addresses.get(0).getCountryName();
    }

    public String getState()
    {
        List<Address> addresses = new ArrayList<Address>();
        if(isGPSEnabled())
        {
            GetMyLocation();

            Geocoder gc = new Geocoder(context, Locale.getDefault());

            try
            {
                Log.d("Lat" ,Double.toString(latitude));
                Log.d("long", Double.toString(longitude));
                addresses = gc.getFromLocation(GetLatitude(), GetLongitude(), 1);

            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        return addresses.get(0).getAdminArea();
    }


    public boolean isGPSEnabled()
    {
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return (isGPSEnabled || isNetworkEnabled);
    }

    public void RequestUpdates(boolean request)
    {
        requestUpdate = request;
    }


    public void GetMyLocation()
    {
        //Browse through providers to find the last known location.
        //List<String> providers = locationManager.getProviders(true);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location l;
        l = locationManager.getLastKnownLocation(bestProvider);
        if (l != null)
        {
            accuracy = l.getAccuracy();
            latitude = l.getLatitude();
            longitude = l.getLongitude();
        }
    }

    public float GetAccuracy() {

            return accuracy;
    }


    public double GetLatitude() {
        return latitude;
    }




    public double GetLongitude() {
        return longitude;
    }

    @Override
    public void onConnected(Bundle connectionHint)
    {
        if(location != null)
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        if(requestUpdate)
        {
            StartLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result)
    {
        Toast.makeText(context, result.toString(), Toast.LENGTH_SHORT).show();
    }

    private void CreateLocationUpdates()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void StartLocationUpdates()
    {
        CreateLocationUpdates();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }
    @Override
    public void onProviderEnabled(String string)
    {

    }
    @Override
    public void onProviderDisabled(String string)
    {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {

    }

    @Override
    public void onLocationChanged(Location loc)
    {
        location = loc;
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();
        atlasM.LocationChanged(latitude, longitude);
        Log.d("LngLat", Double.toString(latitude) + " , " + Double.toString(longitude));
        //Toast.makeText(context, Double.toString(latitude) + " , " + Double.toString(longitude),
          //      Toast.LENGTH_SHORT).show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
}
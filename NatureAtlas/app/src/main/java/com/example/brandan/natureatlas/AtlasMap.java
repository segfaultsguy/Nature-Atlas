package com.example.brandan.natureatlas;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;


/*
   What this class does:
   This class is the map view for the app. It relies on other classes for it to work. It basically renders
   the display when data is present and when the coordinates aren't at the default.
*/



public class AtlasMap extends ActionBarActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback{


    Thread t1,t2;
    TextView speciesAmt;
    private boolean paused, pauseR1;
    private ArrayList<String> areaSpeciesList = new ArrayList<>();
    ArrayAdapter<String> areaAdapter;
    public ArrayList<MarkerData> names;
    private GPSTracking gpsTracking;
    private double currentLat, currentLong, currentCircRad;
    private String kmStringG = "10 KM";
    private String currSpecies = "0";
    private String currType = "";
    Spinner  speciesSpinner;
    final static int integerVal = 0;
    JSONArray dataRetrieved = null;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ClusterManager<MarkerData> cm;
    public Cluster currClusterClicked;
    SupportMapFragment mapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atlas_map);
        setUpMapIfNeeded();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        mapFragment.setHasOptionsMenu(true);
        paused = false;
        pauseR1 = false;


        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#916d43"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        if(mMap == null)
        {
            Toast.makeText(this, "Map can't fully initialize.", Toast.LENGTH_SHORT).show();
        }




        SharedPreferences p = getSharedPreferences("natureShared", MODE_PRIVATE);
        if(p.getString("mapMode", "").equals(""))
        {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        else
        {
            String mapMode = p.getString("mapMode", "");
            Log.d("Map Mode:", mapMode);
            switch(mapMode)
            {
                case "Hybrid":
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                case "Terrain":
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
                case "Satellite":
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case "Normal":
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
            }
        }



        cm = new ClusterManager<>(this, mapFragment.getMap());


        mMap.setOnCameraChangeListener(cm);
        mMap.setOnMarkerClickListener(cm);



        /*
            Check to see if a boolean named firstRun is in the sharedPrefs file.
            If it's not, then we initiate the startup process.
        */

        if(getSharedPreferences("sharedPrefs", MODE_PRIVATE).getString("usersName", "").equals(""))
        {

        }





        //Check if GPS is on, that way we can send Google Maps to where it needs to go.
        gpsTracking = new GPSTracking(this, this);
        //If GPS is enabled, do stuff. Otherwise, tell the user about it.
        if(gpsTracking.isGPSEnabled())
        {
            //Get the location
            gpsTracking.GetMyLocation();
            gpsTracking.RequestUpdates(true);
            currentLat = gpsTracking.GetLatitude();
            currentLong = gpsTracking.GetLongitude();
            Log.d("LatLong", Double.toString(currentLat) + " , " + Double.toString(currentLong));
        }
        else
        {
            Toast.makeText(this, "Your location data is not enabled. You need this to be turned on for Nature Atlas",
                    Toast.LENGTH_SHORT).show();
        }

        //Check if wifi or mobile data is on. Otherwise, we have a problem...
        ConnectionStates connectionStates = new ConnectionStates(this);
        if(!connectionStates.isWifiOn() && !connectionStates.isMobileDataOn())
        {
            Toast.makeText(this, "There are currently no connections turned on. Please turn on WiFi or mobile data",
                    Toast.LENGTH_SHORT).show();
        }


        t1 = new Thread(r1);
        t1.run();
        t2 = new Thread(r2);
        t2.run();
        InitLoadMapPoints();
        mMap.addCircle(new CircleOptions().center(new LatLng(currentLat, currentLong)).radius(GetKMDouble(kmStringG) * 1000).strokeWidth(0f).fillColor(0x550000FF));
        cm.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerData>() {
            @Override
            public boolean onClusterItemClick(MarkerData markerData) {


                Log.d("clickedMarkerData", "Not null");
                Globals g = Globals.getInstance();
                g.SetMarkerInstance(markerData);
                Intent intent = new Intent(getApplicationContext(), MarkerIndividualView.class);
                startActivity(intent);

                return false;
            }
        });
        cm.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MarkerData>() {
            @Override
            public boolean onClusterClick(Cluster<MarkerData> myCluster) {
                Intent showTable = new Intent(getApplicationContext(), MakeDataTable.class);

                currClusterClicked = myCluster;
                names = new ArrayList<MarkerData>();

                Log.d("ClusterSize", Integer.toString(currClusterClicked.getItems().size()));
                for (int i = 0; i < currClusterClicked.getItems().size(); i++) {
                    //Log.d("Cluster Clicked", myCluster.getItems().iterator().next().species);
                    names.add(myCluster.getItems().iterator().next());
                }

                Globals g = new Globals();
                g.SetMarkerList(names);
                startActivity(showTable);
                return false;
            }
        });


    }



    Runnable r1 = new Runnable() {
        @Override
        public void run()
        {

            //This runnable is what checks if the data has been loaded. Once it has been loaded, it
            //will cease.


            Log.d("isRunning", "true");
            Log.d("r1Paused", Boolean.toString(pauseR1));
            if(dataRetrieved != null && dataRetrieved.length() != 0 )
            {
                Log.d("dataretrievedSize", Integer.toString(dataRetrieved.length()));
                Location loc = new Location("");
                loc.setLatitude(currentLat);
                loc.setLongitude(currentLong);
                Location loc2 = new Location("");
                Log.d("dataretrieved", "not null");

                for(int i = 0; i < dataRetrieved.length(); i++)
                {

                    try
                    {
                        loc2.setLatitude(Double.parseDouble(dataRetrieved.getJSONObject(i).getString("latitude")));
                        loc2.setLongitude(Double.parseDouble(dataRetrieved.getJSONObject(i).getString("longitude")));
                        // Log.d("Distance", Double.toString(loc.distanceTo(loc2) / 1000));
                        if(loc.distanceTo(loc2)/1000 < GetKMDouble(kmStringG))
                        {

                            cm.addItem(new MarkerData(dataRetrieved.getJSONObject(i).getString("recordId"), dataRetrieved.getJSONObject(i).getString("typeName"),
                                    Double.parseDouble(dataRetrieved.getJSONObject(i).getString("latitude")), Double.parseDouble(dataRetrieved.getJSONObject(i).getString("longitude")),
                                    dataRetrieved.getJSONObject(i).getString("scientific")));


                            if(!areaSpeciesList.contains(dataRetrieved.getJSONObject(i).getString("scientific")))
                            {
                                areaSpeciesList.add(dataRetrieved.getJSONObject(i).getString("scientific"));
                            }


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // mapPoints.add(newM.getId());
                }



                Collections.sort(areaSpeciesList, String.CASE_INSENSITIVE_ORDER);
                areaAdapter.notifyDataSetChanged();
                speciesAmt.setText(Integer.toString(dataRetrieved.length()) + " record(s) found.");
                mMap.setOnMarkerClickListener(cm);

                cm.cluster();



                t1.interrupt();
                pauseR1 = true;
            }
            else
            {
                if(!pauseR1)
                {
                    Toast.makeText(getApplicationContext(), "Fetching data...", Toast.LENGTH_SHORT).show();

                }
                h1.postDelayed(this, 2000);
            }

        }
    };
    Runnable r2=new Runnable() {
        @Override
        public void run()
        {
            //This runnable gets the user's location.

            if(paused)
            {
                h1.removeCallbacks(r1);
                h2.removeCallbacks(r2);
            }
            ActivityManager am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

            LatLng home = new LatLng(0.0,0.0);



            Log.d("cn", cn.getShortClassName());

            if(cn.getShortClassName().equals(".AtlasMap") )
            {
                paused = false;
            }
            else
            {
                paused = true;
            }

            if(paused)
            {

                Log.d("paused", "true");
                h2.removeCallbacks(r2);
            }

            if(!paused)
            {

                //accuracy = gpsTracking.GetAccuracy();
                //Log.d("Accuracy", Float.toString(accuracy));
                LatLng currPos = new LatLng(currentLat, currentLong);
                Log.d("LatLong", Double.toString(currentLat) + " , " + Double.toString(currentLong));
                gpsTracking.GetMyLocation();
                gpsTracking.RequestUpdates(true);
                currentLat = gpsTracking.GetLatitude();
                currentLong = gpsTracking.GetLongitude();
                mMap.setMyLocationEnabled(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currPos,10));
                Toast.makeText(getApplicationContext(), "Searching for coordinates...", Toast.LENGTH_SHORT).show();
                LatLng currLoc = null;
                if(mMap.getMyLocation() != null)
                {
                    currLoc = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
                }
                if(currLoc != home)
                {
                    paused = true;
                }
                h2.postDelayed(r2, 20000);
            }


        }
    };



    public void Refresh(View view)
    {
        if(t1.isInterrupted())
        {
            t1 = new Thread(r1);
            t1.run();
        }
    }

    Handler h1 = new Handler();
    Handler h2 = new Handler();



    @Override
    public void onPause()
    {
        super.onPause();
        paused = true;
        pauseR1 = true;
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        h1.removeCallbacks(r1);
        h2.removeCallbacks(r2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_itemdetail, menu);
        super.onCreateOptionsMenu(menu);

        MenuItem species = menu.findItem(R.id.speciesSpin);
        MenuItem speciesText = menu.findItem(R.id.speciesAmnt);
        speciesAmt = (TextView)MenuItemCompat.getActionView(speciesText);
        speciesSpinner = (Spinner)MenuItemCompat.getActionView(species);
        areaAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, areaSpeciesList);

        speciesSpinner.setAdapter(areaAdapter);

        speciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Object object = speciesSpinner.getItemAtPosition(i);
                currType = object.toString();
                try {
                    SortBySpecies();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId())
        {
            case R.id.find:
                Intent intentF = new Intent(this, AtlasMap.class);
                startActivity(intentF);
                return true;
            case R.id.share:
                Intent intentSu = new Intent(this, SightingSubmit.class);
                startActivity(intentSu);
                return true;
            case R.id.about:
                Toast.makeText(this, "Programmed by Brandan Jablonski for Millersville University.",
                        Toast.LENGTH_LONG).show();
                return true;
            case R.id.account:
                Intent intent = new Intent(this, loginregister.class);
                startActivity(intent);
                return true;
            case R.id.drafts:
                Intent intentD = new Intent(this, Drafts.class);
                startActivity(intentD);
                return true;
            case R.id.action_settings:
                Intent intentS = new Intent(this, SettingsPage.class);
                startActivity(intentS);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case (integerVal) :
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    try
                    {
                        if(data.getStringExtra("currentUser").equals("true"))
                        {
                            SharedPreferences sharedPreferences = getSharedPreferences("userName", MODE_PRIVATE);
                            SortData(GetKMDouble(data.getStringExtra("radius")), data.getStringExtra("type"), data.getStringExtra("dateObserved"), sharedPreferences.getString("usersName", ""));
                        }
                        else if(data.getStringExtra("type").equals("All"))
                        {

                            SortData();
                        }
                        else
                        {
                            SortData(GetKMDouble(data.getStringExtra("radius")), data.getStringExtra("type"), data.getStringExtra("dateObserved"));
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onResume()
    {

        super.onResume();
        setUpMapIfNeeded();
        //If the current latitude and longitude are (0.0,0.0), we know that things haven't loaded.
        if(currentLat == 0.0 && currentLong == 0.0)
        {
            h2.postDelayed(r2, 2000);
        }
        else
        {
            h2.removeCallbacks(r2);
        }
    }


    private void setUpMapIfNeeded()
    {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null)
        {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null)
            {

                setUpMap();
            }
        }
    }


    private void setUpMap()
    {
        //This code came with the class, not sure if it is needed though.

    }

    public void LocationChanged(double lat, double lon)
    {
        Toast.makeText(this, "Developer: loc changed" + " " + Double.toString(lat) + " , " + Double.toString(lon) , Toast.LENGTH_SHORT).show();
        LatLng currPos = new LatLng(lat, lon);
        Log.d("LatLong", Double.toString(lat) + " , " + Double.toString(lon));
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currPos, 13));
        mMap.addMarker(new MarkerOptions()
                .title("You")
                .snippet("You are here.")
                .position(currPos));

    }


    @Override
    public void onMapReady(GoogleMap map)
    {

        mMap = map;
        LatLng currPos = new LatLng(currentLat, currentLong);
        Log.d("LatLong", Double.toString(currentLat) + " , " + Double.toString(currentLong));
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currPos, 13));
        mMap.addMarker(new MarkerOptions()
                .title("You")
                .snippet("You are here.")
                .position(currPos));




    }

    @Override
    public void onMapLoaded()
    {
        LatLng currPos = new LatLng(currentLat, currentLong);
        Log.d("LatLong", Double.toString(currentLat) + " , " + Double.toString(currentLong));
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currPos, 13));
        mMap.addMarker(new MarkerOptions()
                .title("You")
                .snippet("You are here.")
                .position(currPos));

    }


    private void InitLoadMapPoints()
    {
        ConnectionStates cs = new ConnectionStates(this);

        dataRetrieved = new JSONArray();
        if((cs.isWifiOn() && cs.isLocationOn()) || (cs.isMobileDataOn()&& cs.isLocationOn()))
        {




            DataRetriever dc = new DataRetriever(new DataRetriever.CallBack() {
                public void GetJSON(JSONArray array) {


                    SetData(array);

                }
            });


            dc.SetConnectionTypes( currentLat, currentLong);
            dc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            String kmString = "10 KM";


            mMap.addCircle(new CircleOptions().center(new LatLng(currentLat, currentLong)).radius(GetKMDouble(kmString) * 1000).strokeWidth(0f).fillColor(0x550000FF));


            r1.run();
        }
    }

    private Double GetKMDouble(String kmString)
    {
        Double kmVal = 0.0;
        switch (kmString)
        {
            case "10 KM":
                kmVal = 10.0;
                break;
            case "3 KM":
                kmVal = 3.0;
                break;
            case "1 KM":
                kmVal = 1.0;
                break;
            case "0.5 KM":
                kmVal = 0.5;
                break;
            case "0.1 KM":
                kmVal = 0.1;
                break;


        }
        return kmVal;
    }

    public void SetData(JSONArray array)
    {
        dataRetrieved = array;
        Log.d("dataRetrieved", Integer.toString(dataRetrieved.length()));

    }

    public void ChangeFilterOptions(View view)
    {
        Intent intentFO = new Intent(this, FilterOptions.class);
        startActivityForResult(intentFO, integerVal);
    }



    // Overloaded function
    public void SortData(double kmString, String currSpecies, String dateObserved, String useUser) throws JSONException
    {
        int speciesInt = 0;
        switch (currSpecies) {
            case "Birds":
                speciesInt = 2;
                break;
            case "Fishes":
                speciesInt = 3;
                break;
            case "Fungi":
                speciesInt = 4;
                break;
            case "Herps":
                speciesInt = 5;
                break;
            case "Invertebrates":
                speciesInt = 1;
                break;
            case "Mammals":
                speciesInt = 6;
                break;
            case "Plants":
                speciesInt = 8;
                break;
            case "Zooplankton":
                speciesInt = 9;
                break;

        }
        Log.d("SpeciesInt", Integer.toString(speciesInt));

        areaAdapter.clear();
        Log.d("Size of Clusters", Integer.toString(cm.getClusterMarkerCollection().getMarkers().size()));

        Log.d("SortData", "Sorting data...");
            //Log.d("DataRetrievedLength", Integer.toString(dataRetrieved.length()));


            Location loc = new Location("");
            loc.setLatitude(currentLat);
        loc.setLongitude(currentLong);
            Location loc2 = new Location("");
        cm.clearItems();
        mMap.clear();


            for (int i = 0; i < dataRetrieved.length(); i++)
            {


                loc2.setLatitude(Double.parseDouble(dataRetrieved.getJSONObject(i).getString("latitude")));
                loc2.setLongitude(Double.parseDouble(dataRetrieved.getJSONObject(i).getString("longitude")));


                if (loc.distanceTo(loc2) / 1000 < kmString)
                {

                    Log.d("Type:", dataRetrieved.getJSONObject(i).getString("typeName"));
                    Log.d("CurrType:", currSpecies);
                    Log.d("currType:", Integer.toString(speciesInt));
                    if (dataRetrieved.getJSONObject(i).getString("typeName").equals(Integer.toString(speciesInt)))
                    {
                        if(dataRetrieved.getJSONObject(i).getString("dateObs").equals(dateObserved) &&
                                dataRetrieved.getJSONObject(i).getString("userId").equals(useUser))
                        {


                            cm.addItem(new MarkerData(dataRetrieved.getJSONObject(i).getString("recordId"), dataRetrieved.getJSONObject(i).getString("typeName"), Double.parseDouble(dataRetrieved.getJSONObject(i).getString("latitude")), Double.parseDouble(dataRetrieved.getJSONObject(i).getString("longitude")), dataRetrieved.getJSONObject(i).getString("scientific")));
                            if (!areaSpeciesList.contains(dataRetrieved.getJSONObject(i).getString("scientific")))
                                areaSpeciesList.add(dataRetrieved.getJSONObject(i).getString("scientific"));

                        }

                    }

                    if(currSpecies.equals("All") )
                    {
                        cm.addItem(new MarkerData(dataRetrieved.getJSONObject(i).getString("recordId"), dataRetrieved.getJSONObject(i).getString("typeName"), Double.parseDouble(dataRetrieved.getJSONObject(i).getString("latitude")), Double.parseDouble(dataRetrieved.getJSONObject(i).getString("longitude")), dataRetrieved.getJSONObject(i).getString("scientific")));
                        areaSpeciesList.add(dataRetrieved.getJSONObject(i).getString("scientific"));
                    }



                }


            }
        Log.d("SizeAfterSort", Integer.toString(areaSpeciesList.size()));
            Collections.sort(areaSpeciesList, String.CASE_INSENSITIVE_ORDER);
            areaAdapter.addAll(areaSpeciesList);
            areaAdapter.notifyDataSetChanged();
            speciesAmt.setText(Integer.toString(dataRetrieved.length()) + " record found.");
            mMap.addCircle(new CircleOptions().center(new LatLng(currentLat, currentLong)).radius(kmString * 1000).strokeWidth(0f).fillColor(0x550000FF));
            cm.cluster();

    }

    //Another overloaded function.
    void SortData()
    {
        Location loc = new Location("");
        loc.setLatitude(currentLat);
        loc.setLongitude(currentLong);
        Location loc2 = new Location("");

        for(int i = 0; i < dataRetrieved.length(); i++)
        {

            try
            {
                loc2.setLatitude(Double.parseDouble(dataRetrieved.getJSONObject(i).getString("latitude")));
                loc2.setLongitude(Double.parseDouble(dataRetrieved.getJSONObject(i).getString("longitude")));
                // Log.d("Distance", Double.toString(loc.distanceTo(loc2) / 1000));
                if(loc.distanceTo(loc2)/1000 < GetKMDouble(kmStringG))
                {

                    cm.addItem(new MarkerData(dataRetrieved.getJSONObject(i).getString("recordId"), dataRetrieved.getJSONObject(i).getString("typeName"),
                            Double.parseDouble(dataRetrieved.getJSONObject(i).getString("latitude")), Double.parseDouble(dataRetrieved.getJSONObject(i).getString("longitude")),
                            dataRetrieved.getJSONObject(i).getString("scientific")));


                    if(!areaSpeciesList.contains(dataRetrieved.getJSONObject(i).getString("scientific")))
                    {
                        areaSpeciesList.add(dataRetrieved.getJSONObject(i).getString("scientific"));
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // mapPoints.add(newM.getId());
        }



        Collections.sort(areaSpeciesList, String.CASE_INSENSITIVE_ORDER);
        areaAdapter.notifyDataSetChanged();
        speciesAmt.setText(Integer.toString(dataRetrieved.length()) + " record(s) found.");
        mMap.setOnMarkerClickListener(cm);

        cm.cluster();
    }


    public void SortData(double kmString, String currSpecies, String dateObserved) throws JSONException
    {
        int speciesInt = 0;
        switch (currSpecies) {
            case "Birds":
                speciesInt = 2;
                break;
            case "Fishes":
                speciesInt = 3;
                break;
            case "Fungi":
                speciesInt = 4;
                break;
            case "Herps":
                speciesInt = 5;
                break;
            case "Invertebrates":
                speciesInt = 1;
                break;
            case "Mammals":
                speciesInt = 6;
                break;
            case "Plants":
                speciesInt = 8;
                break;
            case "Zooplankton":
                speciesInt = 9;
                break;

        }
        Log.d("SpeciesInt", Integer.toString(speciesInt));

        areaAdapter.clear();
        Log.d("Size of Clusters", Integer.toString(cm.getClusterMarkerCollection().getMarkers().size()));

        Log.d("SortData", "Sorting data...");
        //Log.d("DataRetrievedLength", Integer.toString(dataRetrieved.length()));


        Location loc = new Location("");
        loc.setLatitude(currentLat);
        loc.setLongitude(currentLong);
        Location loc2 = new Location("");
        cm.clearItems();
        mMap.clear();


        for (int i = 0; i < dataRetrieved.length(); i++)
        {


            loc2.setLatitude(Double.parseDouble(dataRetrieved.getJSONObject(i).getString("latitude")));
            loc2.setLongitude(Double.parseDouble(dataRetrieved.getJSONObject(i).getString("longitude")));


            if (loc.distanceTo(loc2) / 1000 < kmString)
            {

                Log.d("Type:", dataRetrieved.getJSONObject(i).getString("typeName"));
                Log.d("CurrType:", currSpecies);
                Log.d("currType:", Integer.toString(speciesInt));
                if (dataRetrieved.getJSONObject(i).getString("typeName").equals(Integer.toString(speciesInt)))
                {
                    if(dataRetrieved.getJSONObject(i).getString("dateObs").equals(dateObserved))
                    {


                        cm.addItem(new MarkerData(dataRetrieved.getJSONObject(i).getString("recordId"), dataRetrieved.getJSONObject(i).getString("typeName"), Double.parseDouble(dataRetrieved.getJSONObject(i).getString("latitude")), Double.parseDouble(dataRetrieved.getJSONObject(i).getString("longitude")), dataRetrieved.getJSONObject(i).getString("scientific")));
                        if (!areaSpeciesList.contains(dataRetrieved.getJSONObject(i).getString("species")))
                            areaSpeciesList.add(dataRetrieved.getJSONObject(i).getString("species"));

                    }

                }

                if(currSpecies.equals("All") )
                {
                    cm.addItem(new MarkerData(dataRetrieved.getJSONObject(i).getString("recordId"), dataRetrieved.getJSONObject(i).getString("typeName"), Double.parseDouble(dataRetrieved.getJSONObject(i).getString("latitude")), Double.parseDouble(dataRetrieved.getJSONObject(i).getString("longitude")), dataRetrieved.getJSONObject(i).getString("scientific")));
                    areaSpeciesList.add(dataRetrieved.getJSONObject(i).getString("scientific"));
                }



            }


        }
        Log.d("SizeAfterSort", Integer.toString(areaSpeciesList.size()));
        Collections.sort(areaSpeciesList, String.CASE_INSENSITIVE_ORDER);
        areaAdapter.addAll(areaSpeciesList);
        areaAdapter.notifyDataSetChanged();
        speciesAmt.setText(Integer.toString(cm.getClusterMarkerCollection().getMarkers().size()) + " record(s) found.");
        mMap.addCircle(new CircleOptions().center(new LatLng(currentLat, currentLong)).radius(kmString * 1000).strokeWidth(0f).fillColor(0x550000FF));
        cm.cluster();

    }

    public void SortBySpecies() throws JSONException
    {

        Location loc = new Location("");
        loc.setLatitude(currentLat);
        loc.setLongitude(currentLong);
        Location loc2 = new Location("");
        cm.clearItems();
        mMap.clear();


        for (int i = 0; i < dataRetrieved.length(); i++)
        {


            loc2.setLatitude(Double.parseDouble(dataRetrieved.getJSONObject(i).getString("latitude")));
            loc2.setLongitude(Double.parseDouble(dataRetrieved.getJSONObject(i).getString("longitude")));


            if (loc.distanceTo(loc2) / 1000 < GetKMDouble(kmStringG))
            {
                if(currType.equals(dataRetrieved.getJSONObject(i).getString("scientific")))
                {
                    cm.addItem(new MarkerData(dataRetrieved.getJSONObject(i).getString("recordId"), dataRetrieved.getJSONObject(i).getString("typeName"), Double.parseDouble(dataRetrieved.getJSONObject(i).getString("latitude")), Double.parseDouble(dataRetrieved.getJSONObject(i).getString("longitude")), dataRetrieved.getJSONObject(i).getString("scientific")));

                }

            }
        }
        speciesAmt.setText(Integer.toString(cm.getClusterMarkerCollection().getMarkers().size()) + " record(s) found.");
        mMap.addCircle(new CircleOptions().center(new LatLng(currentLat, currentLong)).radius(GetKMDouble(kmStringG) * 1000).strokeWidth(0f).fillColor(0x550000FF));
        cm.cluster();

    }


}





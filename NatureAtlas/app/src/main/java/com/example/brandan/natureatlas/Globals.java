package com.example.brandan.natureatlas;

import android.app.Application;

import com.amazonaws.services.s3.AmazonS3;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Brandan on 10/13/2015.
 */

/*
    What this class does:
    This class lets me get data from other classes without it being a hassle.
*/
public class Globals extends Application
{
    private static Globals instance = new Globals();
    private static ArrayList<MarkerData> sharedMarker;
    private static Snapshot  snapshots;
    private static boolean signedIn;
    private static Map<String, String> pairs;
    private static AmazonS3 gS3;

    private static MarkerData data;
    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
    }

    public static Globals getInstance()
    {
        return instance;
    }

    public void SetMarkerInstance(MarkerData mData)
    {
        data = mData;
    }

    public MarkerData GetMarkerInstance()
    {
        return data;
    }

    public ArrayList<MarkerData> GetMarkerList()
    {
        return sharedMarker;
    }

    public void SetMarkerList(ArrayList<MarkerData> markers)
    {
        sharedMarker = markers;
    }

    public void SetSnapshotInstance(Snapshot snapshot)
    {
        snapshots = snapshot;
    }

    public Snapshot GetSnapshotInstance()
    {
        return snapshots;
    }

    public void SetSignedIn(boolean flag)
    {
        signedIn = flag;
    }

    public boolean GetSignedIn()
    {
        return signedIn;
    }

    public void SetPairsArray(Map<String, String> pArray)
    {
        pairs  = pArray;
    }
    public Map<String, String> GetPairsArray(){return pairs;}

    public void SetS3Instance(AmazonS3 s3)
    {
        gS3 = s3;
    }

    public AmazonS3 GetS3Instance()
    {
        return gS3;
    }
}

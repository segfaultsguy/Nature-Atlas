package com.example.brandan.natureatlas;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


/**
 * Created by Brandan on 9/12/2015.
 */

/*
    What this class does:
    This class sends the species name along with the current latitude and longitude of the user
    and pulls back data from the server.
*/
public class DataRetriever extends AsyncTask<Void, Void, JSONArray> {


    double lat, lon;


    public DataRetriever(CallBack callBack)
    {
        this.callBack = callBack;
    }
    @Override
    protected JSONArray doInBackground(Void...params)
    {
        Log.d("cancelled", Boolean.toString(isCancelled()));
        JSONArray data = new JSONArray();

        if(Thread.interrupted())
        {

            Log.d("cancelled", "true");
        }

        HttpClient client = new DefaultHttpClient();


        //Fill in string here...
        //Currently, this points to the test server.
        String connect = "http://natureatlas.org/appServices/fetchMapData.php";
        HttpPost httpPost = new HttpPost(connect);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("Latitude", Double.toString(lat)));
        nameValuePairs.add(new BasicNameValuePair("Longitude", Double.toString(lon)));

        try
        {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }


        try
        {
            HttpResponse response = client.execute(httpPost);
            HttpEntity get = response.getEntity();

            if (get != null)
            {
                String ret = EntityUtils.toString(get);
                data = new JSONArray(ret);



            }
            else
            {
                //To do...
            }




        }
        catch (IOException e)
        {
            Log.d("IO", e.toString());
            e.printStackTrace();
        }
        catch (Throwable t)
        {
            Log.d("throwable", t.toString());

        }




        return data;



    }





    public interface CallBack
    {
        void GetJSON(JSONArray array);
    }
    private CallBack callBack;
    public void onPostExecute(JSONArray array) {
        callBack.GetJSON(array);
    }



    public void SetConnectionTypes( double latU, double lonU)
    {

        lat = latU;
        lon = lonU;

    }



}

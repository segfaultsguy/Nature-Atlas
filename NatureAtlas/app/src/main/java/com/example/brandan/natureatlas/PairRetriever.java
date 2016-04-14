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
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Brandan on 3/3/2016.
 */
public class PairRetriever extends AsyncTask<Void, Void, JSONArray>
{



    public PairRetriever(CallBack callBack)
    {
        this.callBack = callBack;
    }
    protected JSONArray doInBackground(Void...params)
    {
        Log.d("cancelled", Boolean.toString(isCancelled()));
        JSONArray data = new JSONArray();

        if(isCancelled())
        {
            Log.d("cancelled", "true");
        }

        HttpClient client = new DefaultHttpClient();


        //Fill in string here...
        //Currently, this points to the test server.
        String connect = "http://natureatlas.org/appServices/getPairs.php";
        HttpPost httpPost = new HttpPost(connect);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();


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


    private PairRetriever.CallBack callBack;
    public void onPostExecute(JSONArray array) {
        callBack.GetJSON(array);
    }



}

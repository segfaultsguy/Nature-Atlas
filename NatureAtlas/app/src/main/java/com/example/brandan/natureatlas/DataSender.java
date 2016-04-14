package com.example.brandan.natureatlas;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Brandan on 9/8/2015.
 */
public class DataSender
{
    private static String test = "http://natureatlas.org/appServices/newSubmission.php";


    public void SendData(String postedBy, String name, String lat, String lon, String wild, String nation,
                         String species, String organism, String subSpecific, String phen, String abund, String acc,
                         String state, String isEditable, String oPhotoOneT, String oPhotoOneOp, String oPhotoOneO,
                         String oPhotoTwoT, String oPhotoTwoOp, String oPhotoTwoO, Context context) throws JSONException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        try {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
            HttpConnectionParams.setSoTimeout(httpParameters, 10000);
            HttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpPost getMethod=new HttpPost(test);
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("postedBy", postedBy));
            nameValuePairs.add(new BasicNameValuePair("species",species));
            nameValuePairs.add(new BasicNameValuePair("subspecific", subSpecific));
            nameValuePairs.add(new BasicNameValuePair("phenology",phen));
            nameValuePairs.add(new BasicNameValuePair("state",state));
            nameValuePairs.add(new BasicNameValuePair("township", ""));
            nameValuePairs.add(new BasicNameValuePair("county", ""));
            nameValuePairs.add(new BasicNameValuePair("accuracy", acc));
            nameValuePairs.add(new BasicNameValuePair("abundance", abund));
            nameValuePairs.add(new BasicNameValuePair("type",organism));
            nameValuePairs.add(new BasicNameValuePair("nation",nation));
            nameValuePairs.add(new BasicNameValuePair("wild",wild));
            nameValuePairs.add(new BasicNameValuePair("yourname",name));
            nameValuePairs.add(new BasicNameValuePair("latitude", lat));
            nameValuePairs.add(new BasicNameValuePair("longitude", lon));
            nameValuePairs.add(new BasicNameValuePair("editable", isEditable));
            nameValuePairs.add(new BasicNameValuePair("photoOneOrig", oPhotoOneO));
            nameValuePairs.add(new BasicNameValuePair("photoOneThumb", oPhotoOneT));
            nameValuePairs.add(new BasicNameValuePair("photoOneOpt", oPhotoOneOp));
            nameValuePairs.add(new BasicNameValuePair("photoTwoOrig", oPhotoTwoO));
            nameValuePairs.add(new BasicNameValuePair("photoTwoThumb", oPhotoTwoT));
            nameValuePairs.add(new BasicNameValuePair("photoTwoOpt", oPhotoTwoOp));
            getMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));

            Log.d("name", name);
            Log.d("lat", lat);
            Log.d("long", lon);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response =  httpClient.execute(getMethod, responseHandler);
            Toast.makeText(context, "New record created successfully.", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Throwable t) {
            Toast.makeText(context, "Request failed: " + t.toString(),
                    Toast.LENGTH_LONG).show();
        }

    }


}

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


import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Brandan on 10/27/2015.
 */
public class RegisterRetriever
{

    public RegisterRetriever(String emails, String password, String name, Context context) {


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);


        Log.d("Email", emails);


        try
        {
            Log.d("Email", emails);
            String connect = "http://natureatlas.org/appServices/newAccount.php";
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
            HttpConnectionParams.setSoTimeout(httpParameters, 10000);
            HttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpPost getMethod = new HttpPost(connect);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("name", name));
            nameValuePairs.add(new BasicNameValuePair("email", emails));
            nameValuePairs.add(new BasicNameValuePair("password", password));

            getMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
            String response = httpClient.execute(getMethod, responseHandler);


            Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {

            e.printStackTrace();
            Log.d("IOException", e.toString());

        }

    }
}




















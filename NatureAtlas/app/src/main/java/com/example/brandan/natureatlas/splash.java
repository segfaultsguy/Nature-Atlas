package com.example.brandan.natureatlas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;


public class splash extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), com.example.brandan.natureatlas.SightingSubmit.class);
                startActivity(intent);
                finish();
            }
        }, 3000);

        Globals g = Globals.getInstance();


        CognitoCachingCredentialsProvider cccp = new CognitoCachingCredentialsProvider(getApplicationContext(),
                "us-east-1:c1b10d66-496c-4e5f-8b82-7361e2183dfe", Regions.US_EAST_1);
        AmazonS3 s3 = new AmazonS3Client(cccp);
        g.SetS3Instance(s3);
        SharedPreferences getSP = getSharedPreferences("userName", MODE_PRIVATE);
        boolean firstRun = getSharedPreferences("sharedPrefs", MODE_PRIVATE).getBoolean("firstRun", true);
        Log.d("firstRun", Boolean.toString(firstRun));
        ConnectionStates cs = new ConnectionStates(this);
        if(!cs.isLocationOn())
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            // set title

            alertDialogBuilder.setTitle("Location Services Off");


            // set dialog message
            alertDialogBuilder
                    .setMessage("Your location services are off. Continuing with this off will make it" +
                            " difficult to use this app. Press OK to turn them on, or press cancel.")
                    .setCancelable(false)
                    .setPositiveButton(R.string.OK,new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog,int id)
                        {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
        if(firstRun)
        {
            try
            {

                FileOutputStream os = new FileOutputStream(this.getFilesDir().getPath()+ "/snapshots.txt");
                Log.d("Filepath: ", this.getFilesDir().getPath()+ "/snapshots.txt");
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeInt(0);
                oos.flush();
                oos.close();
                os.flush();
                os.close();
            }
            catch(IOException i)
            {
                i.printStackTrace();
            }

            SharedPreferences.Editor gse = getSharedPreferences("sharedPrefs", MODE_PRIVATE).edit();
            gse.putBoolean("firstRun", false);
            gse.apply();
            firstRun = getSharedPreferences("sharedPrefs", MODE_PRIVATE).getBoolean("firstRun", true);
            Log.d("FirstRun", Boolean.toString(firstRun));
        }
        if(!getSP.getString("usersName","").equals(""))
        {
            Globals gl = Globals.getInstance();
            gl.SetSignedIn(true);

        }

    }



}

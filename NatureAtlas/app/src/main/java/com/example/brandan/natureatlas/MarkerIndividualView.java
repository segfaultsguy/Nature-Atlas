package com.example.brandan.natureatlas;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

public class MarkerIndividualView extends AppCompatActivity {

    JSONArray individualArray = new JSONArray();
    TextView speciesLabel;
    TextView postedLabel;
    TextView organismLabel;
    TextView wildLabel;
    TextView speciesListLabel;
    TextView commonLabel;
    TextView phenLabel;
    TextView abundanceLabel;
    TextView latLabel;
    TextView longLabel;
    TextView accuracyLabel;
    TextView townshipLabel;
    TextView countyLabel;
    TextView stateLabel;
    TextView nationLabel;
    TextView dateLabel;
    MarkerData retrievedData;
    private static String bucketName = "natureatlas";
    ImageView imageOne, imageTwo;
    Bitmap iOneBM, iTwoBM;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_individual_view);
        retrievedData = Globals.getInstance().GetMarkerInstance();
        IndividualDataRetriever idr = new IndividualDataRetriever(retrievedData.recordID,this);

        individualArray = idr.GetJSON();


         speciesLabel = (TextView)findViewById(R.id.setSpeciesName);
         postedLabel = (TextView)findViewById(R.id.postedBy);
         organismLabel = (TextView)findViewById(R.id.organismType);
         wildLabel = (TextView)findViewById(R.id.status);
         speciesListLabel = (TextView)findViewById(R.id.scientificNameLabel);
         commonLabel = (TextView)findViewById(R.id.commonNameLabel);
         phenLabel = (TextView)findViewById(R.id.phenLabel);
         abundanceLabel = (TextView)findViewById(R.id.abundanceLabel);
         latLabel = (TextView)findViewById(R.id.latitudeLabel);
         longLabel = (TextView)findViewById(R.id.longitudeLabel);
         accuracyLabel = (TextView)findViewById(R.id.accuracyLabel);
         townshipLabel = (TextView)findViewById(R.id.townshipLabel);
         countyLabel = (TextView)findViewById(R.id.countyLabel);
         stateLabel = (TextView)findViewById(R.id.stateLabel);
         nationLabel = (TextView)findViewById(R.id.nationLabel);
         dateLabel = (TextView)findViewById(R.id.dateLabel);
         imageOne = (ImageView)findViewById(R.id.pictureOne);
         imageTwo = (ImageView)findViewById(R.id.pictureTwo);

        checkForNewData.run();



    }

    Runnable checkForNewData = new Runnable()
    {
        @Override
        public void run()
        {
            if(individualArray != null)
            {
                Globals g = Globals.getInstance();

                try
                {
                    speciesLabel.setText(retrievedData.species);
                    postedLabel.setText(individualArray.getJSONObject(0).getString("userid"));
                    organismLabel.setText(retrievedData.organism);
                    wildLabel.setText(individualArray.getJSONObject(0).getString("wildName"));
                    speciesListLabel.setText(retrievedData.species);
                    commonLabel.setText(retrievedData.commonName);
                    phenLabel.setText(individualArray.getJSONObject(0).getString("phenologyName"));
                    abundanceLabel.setText(individualArray.getJSONObject(0).getString("abundance"));
                    latLabel.setText(Double.toString(retrievedData.lat));
                    longLabel.setText(Double.toString(retrievedData.longitude));
                    accuracyLabel.setText(individualArray.getJSONObject(0).getString("accuracy"));
                    townshipLabel.setText(individualArray.getJSONObject(0).getString("townshipName"));
                    countyLabel.setText(individualArray.getJSONObject(0).getString("countyName"));
                    stateLabel.setText(individualArray.getJSONObject(0).getString("stateName"));
                    nationLabel.setText(individualArray.getJSONObject(0).getString("nationName"));
                    dateLabel.setText(individualArray.getJSONObject(0).getString("dateObs"));
                    TransferUtility transferUtility = new TransferUtility(g.GetS3Instance(), getApplicationContext());
                    File file = new File("downloadPhotoOne.png");
                    File file2 = new File("downloadPhotoTwo.png");
                    transferUtility.download(bucketName, individualArray.getJSONObject(0).getString("photoOneThumb"), file);
                    transferUtility.download(bucketName, individualArray.getJSONObject(0).getString("photoTwoThumb"), file2);
                    iOneBM = BitmapFactory.decodeFile(file.getPath());
                    iTwoBM = BitmapFactory.decodeFile(file2.getPath());
                    if(iOneBM != null)
                    {
                        imageOne.setImageBitmap(iOneBM);
                    }

                    if(iTwoBM != null)
                    {
                        imageTwo.setImageBitmap(iTwoBM);
                    }

                }
                catch (JSONException je)
                {
                    je.printStackTrace();
                }
            }
            else
            {
                Log.d("Individual Array", "Null");
            }
        }
    };

}

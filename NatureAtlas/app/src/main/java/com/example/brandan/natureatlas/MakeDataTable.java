package com.example.brandan.natureatlas;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


/*
    What this class does:
    This class is for the data table when a cluster is clicked on.
*/
public class MakeDataTable extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_data_table);


        Globals sharedData = Globals.getInstance();

        final ArrayList<MarkerData> getMarkerData = sharedData.GetMarkerList();

        ArrayList<String> getMarkerNames = new ArrayList<>();
        for (int i = 0; i < getMarkerData.size(); i++)
        {
            if(!getMarkerData.get(i).commonName.equals("null"))
            {
                getMarkerNames.add(getMarkerData.get(i).species + "\n" + getMarkerData.get(i).commonName);
            }
            else
            {
                getMarkerNames.add(getMarkerData.get(i).species);
            }
        }

        final ListView lv = (ListView)findViewById(R.id.dataTable);

        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,getMarkerNames);

        lv.setAdapter(aa);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                //Object choice = lv.getItemAtPosition(i);

                Globals g = Globals.getInstance();
                g.SetMarkerInstance(getMarkerData.get(i));
                Intent intent = new Intent(getApplicationContext(), MarkerIndividualView.class);
                startActivity(intent);

            }
        });


    }

}

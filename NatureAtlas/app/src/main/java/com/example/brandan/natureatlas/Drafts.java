package com.example.brandan.natureatlas;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

public class Drafts extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drafts);
        final ArrayList<Snapshot> cachedSnapshots = new ArrayList<>();

        ArrayList<String> snapNames = new ArrayList<>();
        ListView draftListView = (ListView)findViewById(R.id.draftsLV);


        String fileName = this.getFilesDir().getPath() + "/snapshots.txt";
        try
        {
            FileInputStream inStream = new FileInputStream(fileName);
            ObjectInputStream objectInStream = new ObjectInputStream(inStream);
            int count = objectInStream.readInt();
            Log.d("count",Integer.toString(count));

            for(int c = 0; c < count; c++)
            {
                cachedSnapshots.add((Snapshot)objectInStream.readObject());
                snapNames.add(cachedSnapshots.get(c).species);
            }

            ArrayAdapter<String> draftList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, snapNames);
            draftListView.setAdapter(draftList);

            draftListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    Globals g = Globals.getInstance();
                    g.SetSnapshotInstance(cachedSnapshots.get(i));
                    Intent newView = new Intent(getApplicationContext(), SightingSubmit.class);
                    newView.putExtra("snapshot", "yes");
                    startActivity(newView);
                }
            });



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Log.d("cachedSnapSize", Integer.toString(cachedSnapshots.size()));

        for(int i = 0; i < cachedSnapshots.size(); i++)
        {
            Log.d("CachedSnap" , cachedSnapshots.get(i).organism);
        }
    }
}

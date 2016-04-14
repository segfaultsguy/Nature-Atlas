package com.example.brandan.natureatlas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class SettingsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);

        final Spinner mapTypes = (Spinner)findViewById(R.id.mapTypes);
        mapTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                Object choice = mapTypes.getItemAtPosition(i);
                SharedPreferences.Editor e = getSharedPreferences("natureShared", MODE_PRIVATE).edit();
                e.putString("mapMode", choice.toString());
                e.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });
    }

    public void Apply(View view)
    {
        Intent intent = new Intent(this,AtlasMap.class);
        startActivity(intent);
    }

}

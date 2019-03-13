package com.example.boattrackerv2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Main4Activity extends AppCompatActivity {

    double longitudeBoat, latitudeBoat;
    MapView mapView;
    GoogleMap gmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        longitudeBoat = intent.getDoubleExtra("locationBoatLongitude", 0.00);
        latitudeBoat = intent.getDoubleExtra("locationBoatLatitude",0.00);

        mapView = findViewById(R.id.mapView);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gmap = googleMap;
                LatLng point = new LatLng(latitudeBoat, longitudeBoat);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                gmap.addMarker(markerOptions);
                gmap.moveCamera(CameraUpdateFactory.newLatLng(point));
            }
        });

        try {
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
        }catch (Exception e){
            Crashlytics.logException(new Exception("erreur Main4Activity : creation de la MapView : " + e));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.googleConnection) {
            Intent activityGoogleConnection = new Intent(this, Main5Activity.class);
            startActivity(activityGoogleConnection);
        }
        return super.onOptionsItemSelected(item);
    }
}
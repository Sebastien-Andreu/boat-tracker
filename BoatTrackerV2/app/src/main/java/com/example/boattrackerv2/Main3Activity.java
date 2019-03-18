package com.example.boattrackerv2;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class Main3Activity extends AppCompatActivity {

    private TextView textViewNameBoat, textViewTypeBoat, textViewPort, textViewCaptain, textViewDistance;
    private Button buttonShowDistance, buttonShowMap, buttonShowContainer;
    private Boat boat;
    private FirebaseFirestore db;

    private Location locationBoat, locationPort;
    private GeoPoint coordonneePort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        setObjectView();

        for (Boat boat : Main2Activity.myListBoat){
            if (boat.getId().equals(intent.getStringExtra("idBoat"))){
                this.boat = boat;
            }
        }


        db = FirebaseFirestore.getInstance();

        textViewNameBoat.setText("Name of Boat : " + boat.getBoatName());
        textViewCaptain.setText("Name of Captain : " + boat.getCaptainName());
        setTypeOBoat();
        setPortOfBoat();

        buttonShowDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewDistance.setVisibility(View.VISIBLE);
                locationBoat.setLongitude(boat.getCoordonneeBoat().getLongitude());
                locationBoat.setLatitude(boat.getCoordonneeBoat().getLatitude());
                locationPort.setLongitude(coordonneePort.getLongitude());
                locationPort.setLatitude(coordonneePort.getLatitude());
                float distance = locationBoat.distanceTo(locationPort);
                textViewDistance.setText(distance/1000 + " km");
            }
        });

        buttonShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActivityMap();
            }
        });

        buttonShowContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActivityContainer();
            }
        });
    }

    private void setObjectView(){
        textViewNameBoat = findViewById(R.id.textView);
        textViewTypeBoat = findViewById(R.id.textView2);
        textViewPort = findViewById(R.id.textView3);
        textViewCaptain = findViewById(R.id.textView4);
        textViewDistance = findViewById(R.id.textView5);

        locationBoat = new Location("boat");
        locationPort = new Location("port");
        coordonneePort = new GeoPoint(0, 0);

        buttonShowDistance = findViewById(R.id.buttonShowDistance);
        buttonShowMap = findViewById(R.id.buttonShowCarte);
        buttonShowContainer = findViewById(R.id.buttonShowContainer);
    }

    private void setTypeOBoat(){
        Toast.makeText(Main3Activity.this, "Name of Boat : " + boat.getIdPort(),Toast.LENGTH_SHORT).show();

        final DocumentReference docType = db.collection("ContainershipType").document(boat.getIdType());
        docType.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        textViewTypeBoat.setText("Type Of Boat : " + document.getData().get("name").toString());
                    } else {
                        Crashlytics.logException(new Exception("error Main3Activity : setPortOfBoat -> document doesn't exist"));
                    }
                } else {
                    Crashlytics.logException(new Exception("erreor Main3Activity:  setTypeOfBoat -> task is not successfull"));                }
            }
        });
    }

    private void setPortOfBoat(){
        final DocumentReference docPort = db.collection("Port").document(boat.getIdPort());
        docPort.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        coordonneePort = (GeoPoint) document.getData().get("localization");
                        textViewPort.setText("Name of Port : " + document.getData().get("name").toString());
                    } else {
                        Crashlytics.logException(new Exception("error Main3Activity : setPortOfBat -> document doesn't exist"));
                    }
                } else {
                    Crashlytics.logException(new Exception("error Main3Activity : setPortOfBoat -> task is not successfull"));
                }
            }
        });
    }

    public void showActivityMap(){
        Intent activityShowMap = new Intent(this.getBaseContext(), Main4Activity.class);
        activityShowMap.putExtra("locationBoatLatitude", boat.getCoordonneeBoat().getLatitude());
        activityShowMap.putExtra("locationBoatLongitude", boat.getCoordonneeBoat().getLongitude());
        startActivity(activityShowMap);
    }

    public void showActivityContainer(){
        Intent activityShowContainer = new Intent(this.getBaseContext(), Main7Activity.class);
        activityShowContainer.putExtra("idBoat", boat.getId());
        startActivity(activityShowContainer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_modifyboat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.googleConnection) {
            Intent activityGoogleConnection = new Intent(this, Main5Activity.class);
            startActivity(activityGoogleConnection);
        }
        if (id == R.id.modifyBoat) {
            Intent activityModifyBoat = new Intent(this, Main6Activity.class);
            activityModifyBoat.putExtra("idBoat", boat.getId());
            startActivity(activityModifyBoat);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Main3Activity.this, Main2Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("idBoat", boat.getId());
        startActivity(intent);
        super.onBackPressed();
    }
}

package com.example.boattrackerv2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    ListView listBoat;
    FirebaseFirestore db;
    static List<Boat> myListBoat = new ArrayList<Boat>();

    static final String TAG = "DocSnippets";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        myListBoat.clear();
        listBoat = findViewById(R.id.list_view);

        db = FirebaseFirestore.getInstance();

        setListBoat();

        listBoat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item != null) {
                    Boat boat = (Boat)item;
                    Toast.makeText(Main2Activity.this, "Name of Boat : " + boat.getBoatName(),Toast.LENGTH_SHORT).show();
                    showActivityBoatCarac(boat.getId());
                }
            }
        });

    }

    private void setListBoat(){
        db.collection("Containership")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference IDType = (DocumentReference)document.getData().get("type");
                                DocumentReference IDPort = (DocumentReference)document.getData().get("port");


                                Boat boat = new Boat(document.getData().get("captainName").toString(), document.getData().get("name").toString(),
                                        document.getId(), IDType.getId(), IDPort.getId(),(GeoPoint) document.getData().get("localization"));
                                myListBoat.add(boat);
                            }
                            final ArrayAdapter<Boat> adapter = new ArrayAdapter<Boat>(Main2Activity.this, android.R.layout.simple_list_item_1, myListBoat);
                            listBoat.setAdapter(adapter);
                        } else {
                            Crashlytics.logException(new Exception("error Main2Activity : setListBoat -> task is not successfull"));
                        }
                    }
                });

    }

    public void showActivityBoatCarac(String idBoat){
        Intent activityBoatCarac = new Intent(this.getBaseContext(), Main3Activity.class);
        activityBoatCarac.putExtra("idBoat", idBoat);
        startActivity(activityBoatCarac);
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

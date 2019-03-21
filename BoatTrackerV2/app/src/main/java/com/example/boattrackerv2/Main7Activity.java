package com.example.boattrackerv2;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Main7Activity extends AppCompatActivity {

    private double RANGE = 0.300;

    private GeoPoint coordinateAllPort,coordinateAllBoat;
    private FirebaseFirestore db;
    private Button buttonContainerBoat;
    private Spinner spinnerListOfContainerInBoat;
    private TextView textViewContainerPort, textViewContainerBoat;
    private Dialog exchangeContainerOtherBoat;
    private Boat boat;

    private List listOfBoatInSamePort;
    private Location locationBoat, locationPort, locationOtherBoat;

    private ListView listViewOfContainerOtherBoat, listViewOfContainerBoat;

    private List<String> listOfContainerBoat = new ArrayList<>();
    private List<String> listOfContainerOtherBoat = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        listOfBoatInSamePort = new ArrayList();

        for (Boat boat : Main2Activity.myListBoat){
            if (boat.getId().equals(intent.getStringExtra("idBoat"))){
                this.boat = boat;
            }
        }
        locationBoat = new Location("boat");
        locationPort = new Location("port");
        locationOtherBoat = new Location("otherBoat");


        lookIfPortIsInRange();
        setListOfContainerInBoat(List.class);

        buttonContainerBoat = findViewById(R.id.buttonContainerOtherBoat);
        spinnerListOfContainerInBoat = findViewById(R.id.spinnerAllContainer);
        textViewContainerPort = findViewById(R.id.textViewContainerPort);
        textViewContainerBoat = findViewById(R.id.textViewContainerOtherBoat);

        exchangeContainerOtherBoat = new Dialog(this);

        buttonContainerBoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogueGetContainerBoat();
            }
        });
    }

    private void setListOfContainerInBoat(final Object object) {
        final ArrayAdapter<String> adapterBoat = new ArrayAdapter<String>(Main7Activity.this, android.R.layout.simple_spinner_item, listOfContainerBoat);
        adapterBoat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final ArrayAdapter<String> adapterListViewBoat = new ArrayAdapter<String>(Main7Activity.this, android.R.layout.simple_list_item_checked, listOfContainerBoat);

        listOfContainerBoat.clear();
        adapterBoat.clear();

        db.collection("Containership").document(boat.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        List<DocumentReference> list = (List<DocumentReference>) document.get("container");
                        for (DocumentReference documentReference : list) {
                            listOfContainerBoat.add(documentReference.getId());
                        }
                        if (object.equals(List.class))
                            spinnerListOfContainerInBoat.setAdapter(adapterBoat);
                        else
                            listViewOfContainerBoat.setAdapter(adapterListViewBoat);
                    } else {
                        Crashlytics.logException(new Exception("error Main7Activity : getListOfContainerInBoat -> document doesn't exist"));
                    }
                } else {
                    Crashlytics.logException(new Exception("erreor Main7Activity:  getListOfContainerInBoat -> task is not successfull"));
                }
            }
        });
    }
    private void setListViewOfContainerOtherBoat(Boat otherBoat){

        final ArrayAdapter<String> adapterOtherBoat = new ArrayAdapter<String>(exchangeContainerOtherBoat.getContext(), android.R.layout.simple_list_item_checked, listOfContainerOtherBoat);
        adapterOtherBoat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        listOfContainerOtherBoat.clear();
        adapterOtherBoat.clear();

        db.collection("Containership").document(otherBoat.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        List<DocumentReference> list = (List<DocumentReference>) document.get("container");
                        for (DocumentReference documentReference : list) {
                            listOfContainerOtherBoat.add(documentReference.getId());
                        }
                        listViewOfContainerOtherBoat.setAdapter(adapterOtherBoat);
                    } else {
                        Crashlytics.logException(new Exception("error Main7Activity : getListOfContainerOtherBoat -> document doesn't exist"));
                    }
                } else {
                    Crashlytics.logException(new Exception("error Main7Activity:  getListOfContainerOtherBoat -> task is not successfull"));
                }
            }
        });

    }

    private void lookIfPortIsInRange(){
        locationBoat.setLongitude(boat.getCoordonneeBoat().getLongitude());
        locationBoat.setLatitude(boat.getCoordonneeBoat().getLatitude());

        db.collection("Port")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                coordinateAllPort = (GeoPoint) document.getData().get("localization");

                                locationPort.setLongitude(coordinateAllPort.getLongitude());
                                locationPort.setLatitude(coordinateAllPort.getLatitude());
                                float distance = locationBoat.distanceTo(locationPort);

                                if (distance/1000 < RANGE){
                                    textViewContainerPort.setVisibility(View.GONE);
                                    break;
                                }
                                else {
                                    textViewContainerPort.setVisibility(View.VISIBLE);
                                }
                            }
                            lookIfOherBoatIsInSamePort();
                        } else {
                            Crashlytics.logException(new Exception("error Main7Activity : lookIfPortIsInRange -> task is not successfull"));
                        }
                    }
                });
    }

    private void lookIfOherBoatIsInSamePort(){
        db.collection("Containership")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (!boat.getId().equals(document.getId())){
                                    coordinateAllBoat = (GeoPoint)document.getData().get("localization");
                                    locationOtherBoat.setLongitude(coordinateAllBoat.getLongitude());
                                    locationOtherBoat.setLatitude(coordinateAllBoat.getLatitude());
                                    float distance = locationOtherBoat.distanceTo(locationPort);

                                    if (distance/1000 < RANGE){
                                        listOfBoatInSamePort.add(document.getId());
                                        if (textViewContainerPort.getVisibility() == View.GONE){
                                            buttonContainerBoat.setVisibility(View.VISIBLE);
                                            textViewContainerBoat.setVisibility(View.GONE);
                                        }
                                        else {
                                            textViewContainerBoat.setVisibility(View.VISIBLE);
                                            buttonContainerBoat.setVisibility(View.GONE);
                                        }
                                    }
                                    else{
                                        textViewContainerBoat.setVisibility(View.GONE);
                                        buttonContainerBoat.setVisibility(View.GONE);
                                    }
                                }
                            }
                        } else {
                            Crashlytics.logException(new Exception("error Main7Activity : lookIfOherBoatIsInSamePort -> task is not successfull"));
                        }
                    }
                });
    }

    private void addContainerInOtherBoat(Boat otherBoat, String idContainer){
        db.collection("Containership").document(otherBoat.getId())
                .update( "container", FieldValue.arrayUnion(db.collection("Container").document(idContainer)))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.logException(new Exception("error Main7Activity : addContainerInOtherBoat -> Error updating document"));
                    }
                });
    }
    private void removeContainerInBoat(String idContainer){
        db.collection("Containership").document(boat.getId())
                .update( "container", FieldValue.arrayRemove(db.collection("Container").document(idContainer)))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.logException(new Exception("error Main7Activity : removeContainerInBoat -> Error updating document"));
                    }
                });
    }

    private void addContainerInBoat(String idContainer){
        db.collection("Containership").document(boat.getId())
                .update( "container", FieldValue.arrayUnion(db.collection("Container").document(idContainer)))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.logException(new Exception("error Main7Activity : addContainerInBoat -> Error updating document"));
                    }
                });
    }
    private void removeContainerInOtherBoat(Boat otherBoat, String idContainer){
        db.collection("Containership").document(otherBoat.getId())
                .update( "container", FieldValue.arrayRemove(db.collection("Container").document(idContainer)))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.logException(new Exception("error Main7Activity : removeContainerInOtherBoat -> Error updating document"));
                    }
                });
    }

    private void showDialogueGetContainerBoat(){
        exchangeContainerOtherBoat.setContentView(R.layout.popup_getcontainer_port);

        final Spinner spinnerListOfOtherBoat = exchangeContainerOtherBoat.findViewById(R.id.spinnerSelectBoat);
        listViewOfContainerOtherBoat = exchangeContainerOtherBoat.findViewById(R.id.listViewContainerOtherBoat);
        listViewOfContainerBoat = exchangeContainerOtherBoat.findViewById(R.id.listViewContainerBoat);
        final Button buttonDepositOtherBoat = exchangeContainerOtherBoat.findViewById(R.id.buttonUp);
        final Button buttonGetOtherBoat = exchangeContainerOtherBoat.findViewById(R.id.buttonDown);

        final List<Boat> listOfOtherBoat = new ArrayList<Boat>();

        for (Object o : listOfBoatInSamePort){
            for (Boat boat : Main2Activity.myListBoat){
                if (o.equals(boat.getId())){
                    listOfOtherBoat.add(boat);
                }
            }
        }

        final ArrayAdapter<Boat> adapter = new ArrayAdapter<Boat>(exchangeContainerOtherBoat.getContext(), android.R.layout.simple_spinner_item, listOfOtherBoat);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerListOfOtherBoat.setAdapter(adapter);

        setListOfContainerInBoat(ListView.class);

        spinnerListOfOtherBoat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final Boat otherBoat = (Boat) parent.getSelectedItem();
                setListViewOfContainerOtherBoat(otherBoat);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                /* nothing */
            }
        });

        buttonDepositOtherBoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checked = listViewOfContainerBoat.getCheckedItemPositions();
                Boat otherBoat = (Boat)spinnerListOfOtherBoat.getSelectedItem();
                for (int i = 0; i < checked.size(); ++i) {
                    if (checked.valueAt(i)) {
                        addContainerInOtherBoat(otherBoat, listOfContainerBoat.get(i));
                        removeContainerInBoat( listOfContainerBoat.get(i));
                    }
                }
                setListOfContainerInBoat(ListView.class);
                setListViewOfContainerOtherBoat(otherBoat);
            }
        });

        buttonGetOtherBoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checked = listViewOfContainerOtherBoat.getCheckedItemPositions();
                Boat otherBoat = (Boat)spinnerListOfOtherBoat.getSelectedItem();
                for (int i = 0; i < checked.size(); ++i) {
                    if (checked.valueAt(i)) {
                        addContainerInBoat(listOfContainerOtherBoat.get(i));
                        removeContainerInOtherBoat(otherBoat, listOfContainerOtherBoat.get(i));
                    }
                }
                setListOfContainerInBoat(ListView.class);
                setListViewOfContainerOtherBoat(otherBoat);
            }
        });

        listViewOfContainerOtherBoat.setTextFilterEnabled(true);
        listViewOfContainerBoat.setTextFilterEnabled(true);

        setViewDialog(exchangeContainerOtherBoat);
        exchangeContainerOtherBoat.show();
    }



    private void setViewDialog(Dialog dialog){
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setListOfContainerInBoat(List.class);
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
}
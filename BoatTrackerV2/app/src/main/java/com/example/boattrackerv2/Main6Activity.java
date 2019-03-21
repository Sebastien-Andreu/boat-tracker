package com.example.boattrackerv2;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main6Activity extends AppCompatActivity{

    private Boat boat;
    private EditText editTextNameOfBoat, editTextNameOfCaptain;
    private Spinner spinnerTypeOfBoat, spinnerNameOfPort;
    private Button buttonAddType, buttonAddPort, buttonEnter, buttonCancel, buttonModifyCoordinate;

    private List listOfPort, listOfType;
    private FirebaseFirestore db;
    static final String TAG = "DocSnippets";
    private Dialog myDialogType, myDialogPort;

    private MapView mapView, mapViewBoat;
    private GoogleMap gmap, gmapBoat;
    private GeoPoint coordinateNewPort, newCoordinateBoat;
    private boolean canModify = false;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Main5Activity.isConnected){
            Intent activityGoogleConnection = new Intent(this, Main5Activity.class);
            activityGoogleConnection.putExtra("isConnected", true);
            startActivity(activityGoogleConnection);
        }

        setContentView(R.layout.activity_main6);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        setObjectView();

        try {
            db = FirebaseFirestore.getInstance();
        }catch (Exception e){
            Crashlytics.logException(new Exception("erreur Main6Activity : db is not initialize"));
        }

        for (Boat boat : Main2Activity.myListBoat) {
            if (boat.getId().equals(intent.getStringExtra("idBoat"))) {
                this.boat = boat;
            }else {
                Crashlytics.logException(new Exception("erreur Main6Activity : boat is not available"));
            }
        }

        editTextNameOfBoat.setText(this.boat.getBoatName());
        editTextNameOfCaptain.setText(this.boat.getCaptainName());
        newCoordinateBoat = boat.getCoordonneeBoat();


        setListType();
        setListPort();

        mapViewBoat = findViewById(R.id.mapViewBoat);

        mapViewBoat.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gmapBoat = googleMap;
                gmapBoat.setMinZoomPreference(3);

                LatLng point = new LatLng(boat.getCoordonneeBoat().getLatitude(), boat.getCoordonneeBoat().getLongitude());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                gmapBoat.addMarker(markerOptions);
                gmapBoat.moveCamera(CameraUpdateFactory.newLatLng(point));
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
                    public void onMapClick(LatLng point){
                        if (canModify){
                            newCoordinateBoat = new GeoPoint(point.latitude, point.longitude);
                            gmapBoat.clear();
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(point);
                            gmapBoat.addMarker(markerOptions);
                        }
                    }
                });
            }
        });

        mapViewBoat.onCreate(savedInstanceState);
        mapViewBoat.onResume();

        buttonAddType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupAddTypeOfBoat();
            }
        });

        buttonAddPort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupAddPort();
            }
        });

        buttonModifyCoordinate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canModify = true;
            }
        });

        buttonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBoat(editTextNameOfCaptain.getText().toString(), editTextNameOfBoat.getText().toString(), spinnerNameOfPort.getSelectedItem().toString(), spinnerTypeOfBoat.getSelectedItem().toString());
                if (!editTextNameOfBoat.getText().toString().equals(""))
                    boat.setBoatName(editTextNameOfBoat.getText().toString());
                if (!editTextNameOfCaptain.getText().toString().equals(""))
                    boat.setCaptainName(editTextNameOfCaptain.getText().toString());
                boat.setIdPort("ID_" + spinnerNameOfPort.getSelectedItem().toString());
                boat.setIdType("ID_" + spinnerTypeOfBoat.getSelectedItem().toString());
                boat.setCoordonneeBoat(newCoordinateBoat);

                /*Return with reload !!*/
                Intent intent = new Intent(Main6Activity.this, Main3Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("idBoat", boat.getId());
                startActivity(intent);
                Main6Activity.this.finish();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Main6Activity.this.finish();
            }
        });
    }

    private void setObjectView(){
        myDialogType = new Dialog(this);
        myDialogPort = new Dialog(this);

        editTextNameOfBoat = findViewById(R.id.NameOfBoat);
        editTextNameOfCaptain = findViewById(R.id.NameOfCaptain);
        spinnerTypeOfBoat = findViewById(R.id.spinnerTypeBoat);
        spinnerNameOfPort = findViewById(R.id.spinnerNameOfPort);

        buttonAddType = findViewById(R.id.ButtonAddTypeBoat);
        buttonAddPort = findViewById(R.id.ButtonAddPort);
        buttonEnter = findViewById(R.id.ButtonEnter);
        buttonCancel = findViewById(R.id.ButtonCancel);
        buttonModifyCoordinate = findViewById(R.id.buttonModifyCoordinateBoat);

        listOfType = new ArrayList();
        listOfPort = new ArrayList();
    }

    private void setListType(){
        listOfType.clear();
        db.collection("ContainershipType")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (boat.getIdType().equals(document.getId()))
                                    listOfType.add(0, document.getData().get("name"));
                                else {
                                    listOfType.add(document.getData().get("name"));
                                }
                            }
                            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Main6Activity.this, android.R.layout.simple_spinner_item, listOfType);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerTypeOfBoat.setAdapter(adapter);
                        } else {
                            Crashlytics.logException(new Exception("error Main6Activity : setListType -> error getting document"));
                        }
                    }
                });
    }

    private void setListPort(){
        listOfPort.clear();
        db.collection("Port")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (boat.getIdPort().equals(document.getId())){
                                    listOfPort.add(0, document.getData().get("name"));
                                }
                                else {
                                    listOfPort.add(document.getData().get("name"));
                                }
                            }
                            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Main6Activity.this, android.R.layout.simple_spinner_item, listOfPort);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerNameOfPort.setAdapter(adapter);
                        } else {
                            Crashlytics.logException(new Exception("error Main6Activity : setListType -> error getting document"));
                        }
                    }
                });
    }

    public void showPopupAddTypeOfBoat() {
        myDialogType.setContentView(R.layout.popup_add_type);

        setViewDialog(myDialogType);

        final EditText EditTextNameType = myDialogType.findViewById(R.id.NameOfTypeOfBoat);
        final EditText EditTextHeightType = myDialogType.findViewById(R.id.heightTypeOfBoat);
        final EditText EditTextLenghtType = myDialogType.findViewById(R.id.lenghtTypeOfBoat);
        final EditText EditTextWidthType = myDialogType.findViewById(R.id.widthTypeOfBoat);

        final Button buttonCancelType = myDialogType.findViewById(R.id.ButtonCancelTypeOfBoat);
        final Button buttonEnterType = myDialogType.findViewById(R.id.ButtonEnterTypeOfBoat);

        buttonEnterType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EditTextHeightType.getText().toString().equals(""))
                    EditTextHeightType.setText(0);
                if (!EditTextLenghtType.getText().toString().equals(""))
                    EditTextLenghtType.setText(0);
                if (EditTextNameType.getText().toString().equals(""))
                    EditTextNameType.setText("Type Name not informed");
                if (EditTextWidthType.getText().toString().equals(""))
                    EditTextWidthType.setText(0);

                addTypeFireBaseBD(EditTextNameType.getText().toString(), Double.parseDouble(EditTextHeightType.getText().toString()),
                        Double.parseDouble(EditTextLenghtType.getText().toString()), Double.parseDouble(EditTextWidthType.getText().toString()));
                myDialogType.dismiss();
            }
        });

        buttonCancelType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialogType.dismiss();
            }
        });

        myDialogType.show();
    }

    public void showPopupAddPort() {
        myDialogPort.setContentView(R.layout.popup_add_port);

        setViewDialog(myDialogPort);

        mapView = myDialogPort.findViewById(R.id.mapView2);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gmap = googleMap;
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
                    public void onMapClick(LatLng point){
                        coordinateNewPort = new GeoPoint(point.latitude, point.longitude);
                        gmap.clear();
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(point);
                        gmap.addMarker(markerOptions);
                    }
                });
            }
        });

        final TextView textNamePort = myDialogPort.findViewById(R.id.NameOfPort);
        final Button buttonCancelPort = myDialogPort.findViewById(R.id.ButtonCancelPort);
        final Button buttonEnterPort = myDialogPort.findViewById(R.id.ButtonEnterPort);

        buttonEnterPort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textNamePort.getText().toString().equals(""))
                    textNamePort.setText("Port Name not informed");
                addPortFireBaseBD(coordinateNewPort.getLatitude(), coordinateNewPort.getLongitude(), textNamePort.getText().toString());
                myDialogPort.dismiss();
            }
        });
        buttonCancelPort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialogPort.dismiss();
            }
        });


        mapView.onCreate(myDialogPort.onSaveInstanceState());
        mapView.onResume();
        myDialogPort.show();
    }

    private void setViewDialog(Dialog dialog){
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
    }

    private void addPortFireBaseBD(double latitude,double longitude, String name){
        Map<String, Object> object = new HashMap<>();
        object.put("localization", new GeoPoint(latitude,longitude));
        object.put("name", name);


        db.collection("Port").document("ID_" + name)
                .set(object)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        setListPort();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void addTypeFireBaseBD(String name, Double height, Double lenght, Double width){
        Map<String, Object> object = new HashMap<>();
        object.put("height", height);
        object.put("lenght", lenght);
        object.put("name", name);
        object.put("width", width);

        db.collection("ContainershipType").document("ID_" + name)
                .set(object)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        setListType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void updateBoat(String captainName, String name, String namePort, String nameType){
        DocumentReference docBoat = db.collection("Containership").document(boat.getId());
        docBoat
                .update("captainName", captainName, "name", name,"localization", newCoordinateBoat, "port", db.collection("Port").document("ID_" + namePort),
                        "type", db.collection("ContainershipType").document("ID_" + nameType))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.logException(new Exception("error Main6Activity : updateBoat -> Error updating document"));
                    }
                });
    }
}
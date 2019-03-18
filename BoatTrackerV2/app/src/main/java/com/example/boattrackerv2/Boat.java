package com.example.boattrackerv2;

import com.google.firebase.firestore.GeoPoint;

public class Boat {
    private String captainName;
    private String boatName;
    private String idBoat;
    private String idType;
    private String idPort;

    private GeoPoint coordonneeBoat;

    Boat(String captainName, String boatName, String id, String idType, String idPort, GeoPoint coordonneeBoat){
        this.captainName = captainName;
        this.boatName = boatName;
        this.idBoat = id;
        this.idType = idType;
        this.idPort = idPort;
        this.coordonneeBoat = coordonneeBoat;

    }


    public String getCaptainName() {
        return captainName;
    }

    public String getBoatName() {
        return boatName;
    }

    public String getId() {
        return idBoat;
    }

    public String getIdType() {
        return idType;
    }

    public String getIdPort() {
        return idPort;
    }

    public GeoPoint getCoordonneeBoat() {
        return coordonneeBoat;
    }

    public void setCaptainName(String captainName) {
        this.captainName = captainName;
    }

    public void setBoatName(String boatName) {
        this.boatName = boatName;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public void setIdPort(String idPort) {
        this.idPort = idPort;
    }

    public void setCoordonneeBoat(GeoPoint coordonneeBoat) {
        this.coordonneeBoat = coordonneeBoat;
    }

    @Override
    public String toString() {
        return this.captainName + "  " + this.boatName;
    }
}

package com.android2.taxidrivershelpeachother.model;

import com.google.firebase.firestore.GeoPoint;

import java.security.PrivateKey;

public class ShuttleIDAndGeoPoint {
    private String id;
    private GeoPoint location;

    public ShuttleIDAndGeoPoint(String id, GeoPoint location){
        this.id = id;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}

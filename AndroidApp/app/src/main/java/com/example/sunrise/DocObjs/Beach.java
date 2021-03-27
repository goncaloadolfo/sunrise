package com.example.sunrise.DocObjs;

import com.google.firebase.firestore.GeoPoint;

public class Beach {

    private String imgName;
    private String name;
    private String locationZone;
    private GeoPoint location;

    public Beach(){}

    public Beach(String imgName, String name, String locationZone, GeoPoint location){
        this.imgName = imgName;
        this.name = name;
        this.locationZone = locationZone;
        this.location = location;
    }

    public String getImgName() {
        return imgName;
    }

    public String getName() {
        return name;
    }

    public String getLocationZone() {
        return locationZone;
    }

    public GeoPoint getLocation() {
        return location;
    }
}

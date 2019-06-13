package com.anbg.sanjeev.friendtracer.model;

/**
 * Created by BEST BUY on 10-11-2017.
 */

public class Tracking {
    private String email;
    private String uid;

    private String lat;
    private String lng;

    public Tracking(String email, String uid, String lat, String lng) {
        this.email = email;
        this.uid = uid;
        this.lat = lat;
        this.lng = lng;
    }

    public Tracking() {
//        super();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLat() {
        return lat;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLng() {
        return lng;
    }
}

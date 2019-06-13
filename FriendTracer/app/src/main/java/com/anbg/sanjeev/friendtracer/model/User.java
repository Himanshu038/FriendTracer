package com.anbg.sanjeev.friendtracer.model;

/**
 * Created by BEST BUY on 09-11-2017.
 */

public class User {
    private String email,status;

    public User() {

    }

    public User(String email, String status) {
        this.email = email;
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }
}

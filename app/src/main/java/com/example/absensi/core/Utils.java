package com.example.absensi.core;

public class Utils {

    boolean result;

    //to get userId from email
    public String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }


}

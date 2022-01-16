package com.example.absen.core;

import android.util.Log;

public class Utils {
    public String usernameFromEmail(String email) {
        Log.d("UtilsEmail",email);
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }
}

package com.humudtech.paynama.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;

public class DetectConnection {
    @SuppressLint("MissingPermission")
    public static boolean checkInternetConnection(Context context) {
        // detect internet connection
        ConnectivityManager con_manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (con_manager.getActiveNetworkInfo() != null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    public static String getUrl(){
        return "https://humudapps.com/app/dao/";
    }
}

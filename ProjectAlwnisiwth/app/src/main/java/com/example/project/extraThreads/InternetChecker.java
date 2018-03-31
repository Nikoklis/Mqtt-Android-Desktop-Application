package com.example.project.extraThreads;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Created by nikoklis on 13/2/2018.
 */

public class InternetChecker extends AsyncTask<Void, Void, String> {
    Object systemService;
    Context context;
    boolean connected;
//    boolean checkingToast;


    public InternetChecker(Object systemService, Context context) {
        this.systemService = systemService;
        this.context = context;
        connected = false;
//        checkingToast = false;
    }


    @Override
    protected String doInBackground(Void... voids) {
        while (true) {
            if (!isNetworkAvailable()) {
                connected = false;
                publishProgress();
//                checkingToast = false;
            } else {
                connected = true;
                publishProgress();
//                checkingToast = true;
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    protected void onProgressUpdate(Void... values) {
        if (connected == false)
            Toast.makeText(context, "There is no internet connection ... Please connect to the internet to access an MQTT broker", Toast.LENGTH_SHORT).show();
//        else
//            if (checkingToast == false)
//                Toast.makeText(context,"You are connected to the internet",Toast.LENGTH_SHORT).show();
    }


    //function for checking if we have internet connection
    //available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) systemService;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;

    }
}

package com.beamotivator.beam;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //to allow firebase offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //after enabling it data loaded will be available offline
        
    }
}

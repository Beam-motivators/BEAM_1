package com.beamotivator.beam.No_Internet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.beamotivator.beam.DashboardActivity;


public class MyReceiver_Splash extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String status = NetworkUtil.getConnectivityStatusString(context);
        if (status.isEmpty()) {
            status = "No Internet Connection";
        }
        if(status.equals("Wifi enabled"))
        {

        }
        else  if(status.equals("Mobile data enabled"))
        {
            final Intent log12 = new Intent(context, DashboardActivity.class);
            context.startActivity(log12);

        }
        else
        {




        }

    }
}
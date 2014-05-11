package com.bignerdranch.RunTracker;

import android.content.Context;
import android.location.Location;
import android.util.Log;

/**
 * Created by badgateway on 11.05.14.
 */
public class TrackingLocationReceiver extends LocationReceiver {

    private static final String TAG = "TrackingLocationReceiver";

    @Override
    protected void onLocationReceived(Context context, Location loc) {
        RunManager.get(context).insetrLocation(loc);
        Log.e(TAG, "Location received to msql " + loc.getTime());
    }
}

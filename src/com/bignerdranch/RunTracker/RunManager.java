package com.bignerdranch.RunTracker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import com.bignerdranch.RunTracker.db.RunDatabaseHelper;

/**
 * Created by badgateway on 11.05.14.
 */
public class RunManager {

    private static final String TAG = "RunManager";

    private static final String PREFS_FILE = "runs";
    private static final String PREF_CURRENT_RUN_ID = "RunManager.class";

    public static final String ACTION_LOCATION = "com.bignerdranch.RunTracker.ACTION_LOCATION";

    private static RunManager sRunManager;
    private Context mAppContext;
    private LocationManager mLocationManager;

    private RunDatabaseHelper mHelper;
    private SharedPreferences mPrefs;
    private long mCurrentRunId;



    private RunManager(Context appContext) {
        mAppContext = appContext;
        mLocationManager = (LocationManager) mAppContext
                .getSystemService(Context.LOCATION_SERVICE);
        mHelper = new RunDatabaseHelper(mAppContext);
        mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mCurrentRunId = mPrefs.getLong(PREF_CURRENT_RUN_ID, -1);
    }



    public static RunManager get(Context context) {

        if (sRunManager == null) {
            sRunManager = new RunManager(context.getApplicationContext());
        }
        return sRunManager;
    }



    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(mAppContext, 0, broadcast,flags);
    }



    public void startLocationUpdates() {
        String provider = LocationManager.GPS_PROVIDER;

        Location lastKnown = mLocationManager.getLastKnownLocation(provider);
        if (lastKnown != null) {
            lastKnown.setTime(System.currentTimeMillis());
            broadcastLocation(lastKnown);
        }

        PendingIntent pi = getLocationPendingIntent(true);
        mLocationManager.requestLocationUpdates(provider, 0, 0, pi);
    }




    private void broadcastLocation(Location location) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        mAppContext.sendBroadcast(broadcast);
    }




    public void stopLocationUpdates() {
        PendingIntent pi = getLocationPendingIntent(false);
        if (pi != null) {
            mLocationManager.removeUpdates(pi);
            pi.cancel();
        }
    }



    public boolean isTrackingRun() {
        return getLocationPendingIntent(false) != null;
    }




    public Run startNewRun() {
        Run run = insertRun();
        startTrackingRun(run);
        return run;
    }



    public void startTrackingRun(Run run) {
        mCurrentRunId = run.getId();
        mPrefs.edit().putLong(PREF_CURRENT_RUN_ID, mCurrentRunId).commit();
        startLocationUpdates();
    }



    public void stopRain() {
        stopLocationUpdates();
        mCurrentRunId = -1;
        mPrefs.edit().remove(PREF_CURRENT_RUN_ID).commit();
    }



    private Run insertRun() {
        Run run = new Run();
        run.setId(mHelper.insertRun(run));
        return run;
    }



    public RunDatabaseHelper.RunCursor queryRuns() {
        return mHelper.queryRuns();
    }



    public void insetrLocation(Location location) {
        if (mCurrentRunId != -1) {
            mHelper.insertLocation(mCurrentRunId, location);
        } else {
            Log.e(TAG, "Location received with no tracking run; ignoring.");
        }
    }
}

package ua.genesis.sasha.breadcrumbs;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.Manifest;

import android.os.Bundle;

import android.support.annotation.Nullable;

import android.support.v4.content.LocalBroadcastManager;


import com.google.android.gms.common.ConnectionResult;



class LocationMonitoringService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = LocationMonitoringService.class.getSimpleName();
    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();


    public static final String ACTION_LOCATION_BROADCAST = LocationMonitoringService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000L;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 15000L;
    double latitudePoint;
    double longetudePoint;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

         latitudePoint= (intent.getDoubleExtra("latPoint",0.0));
         longetudePoint=(intent.getDoubleExtra("lonPoint",0.0));

        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);


        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;

        mLocationRequest.setPriority(priority);
        mLocationClient.connect();


        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     * LOCATION CALLBACKS
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling



            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

    }


    @Override
    public void onConnectionSuspended(int i) {
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            //Відправляем результат в Активіті
            sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }
    }

    private void sendMessageToUI(String lat, String lng) {

        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
        intent.putExtra("latPoint",latitudePoint);
        intent.putExtra("lonPoint",longetudePoint);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}


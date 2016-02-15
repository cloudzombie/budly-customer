package com.budly.android.CustomerApp.td.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by EdwinSL on 4/6/2015.
 */
public class LocationHelper {

    private static LocationHelper locationHelper;
    private GoogleApiClient mLocationClient;
    Location mCurrentLocation;
    LocationManager locationManager;
    LocationHelperCallback callback;
    private boolean updates = false;

    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000) // 5 seconds
//            .setFastestInterval(16) // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    public static void findLocationManager(Context context){
        locationHelper.locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static boolean isLocationServiceEnabled(){
        return locationHelper.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationHelper.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static synchronized void initLocationHelper(Context context){
        if(locationHelper == null)
            locationHelper = new LocationHelper(context);
    }

    public static synchronized LocationHelper getInstance() {
        if (locationHelper == null) {
            throw new IllegalStateException(PreferenceHelper.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return locationHelper;
    }

    public Location getLastLocation(){
        return  LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
    }

    private LocationHelper(Context context){
        if (mLocationClient == null) {
            ConnectionCallback cc = new ConnectionCallback();
            mLocationClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(cc)
                    .addOnConnectionFailedListener(cc)
                    .build();
        }
    }

    public void startConnection(LocationHelperCallback callback){
        this.callback = callback;
        mLocationClient.connect();
    }

    public void startConnection(LocationHelperCallback callback, boolean updates){
        this.updates = updates;
        this.callback = callback;
        mLocationClient.connect();
    }

    public void stopConnection(){
        mLocationClient.disconnect();
    }



    private class ConnectionCallback implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
            LocationListener{

        @Override
        public void onConnected(Bundle bundle) {
            Log.e("LocationConection", "loaction connected!!!");
            Location l = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
            if(l == null) return;
            PreferenceHelper.getInstance().setLocation(new LatLng(l.getLatitude(), l.getLongitude()));

            callback.connected();

            if (updates) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, REQUEST, this);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.e("LocationConection", "loaction connection suspended");
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.e("LocationConection", "loaction connection failed");
        }

        @Override
        public void onLocationChanged(Location location) {
            callback.locationChanged(location);
        }
    }
}

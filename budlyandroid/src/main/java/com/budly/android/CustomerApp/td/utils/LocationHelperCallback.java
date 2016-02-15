package com.budly.android.CustomerApp.td.utils;

import android.location.Location;

/**
 * Created by EdwinSL on 4/6/2015.
 */
public abstract class LocationHelperCallback {
    abstract public void connected();
    public void locationChanged(Location location){}
}

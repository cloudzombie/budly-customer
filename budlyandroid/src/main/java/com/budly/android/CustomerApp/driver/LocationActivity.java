package com.budly.android.CustomerApp.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.android.CustomerApp.Common;
import com.budly.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationActivity extends BaseActivity implements OnClickListener, OnMarkerDragListener{
	
	Button btn_next;
	GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Common.setScreenFlag(this);

		setContentView(R.layout.driver_activity_location);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_next.setOnClickListener(this);
		setUpMapIfNeeded();
	}
	
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    
    private void setUpMap() {
    	mMap.setOnMarkerDragListener(this);
        // We will provide our own zoom controls.
        mMap.getUiSettings().setZoomControlsEnabled(false);
        //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.873454, 151.207030), 16));
        mMap.addMarker(new MarkerOptions()
        .position(new LatLng(-33.873218, 151.207722))
        .title("Hotel Coronation").snippet("Sydney NSW 2000, Australia").draggable(true));
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
			startActivity(new Intent(this, AddSupplierActivity.class));
			break;

		default:
			break;
		}
	}

	@Override
	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		LatLng position = marker.getPosition();
		marker.setTitle("New point");
		marker.setSnippet("Lat: "+position.latitude + " - Lng: "+position.longitude);
	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub
		
	}
}

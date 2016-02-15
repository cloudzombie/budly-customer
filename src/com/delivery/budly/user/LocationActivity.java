package com.budly.android.CustomerApp.user;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.BaseActivity;
import com.budly.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;

public class LocationActivity extends BaseActivity implements
		View.OnClickListener, GoogleMap.OnMarkerDragListener,
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener,
		OnMyLocationButtonClickListener {
	Button btn_next;
	GoogleMap mMap;
	EditText txt_address;
	TextView txt_address2;
	private LocationClient mLocationClient;
	PreferenceHelper preferenceHelper;
	private String address="";

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	private void setUpMap() {
		this.mMap.setOnMarkerDragListener(this);
		this.mMap.getUiSettings().setZoomControlsEnabled(false);
	}

	private void setUpMapIfNeeded() {
		if (this.mMap == null) {
			this.mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			if (this.mMap != null)
				setUpMap();
		}
	}

	public void onClick(View paramView) {
		switch (paramView.getId()) {
		default:
			break;
		case R.id.btn_next:
			if(mCurrentLocation!=null) {
				try {
					finish();
					preferenceHelper.setLocation(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
					preferenceHelper.setValue("location_address", address);
					startActivity(new Intent(this, ListServiceInAreaActivity.class));
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(this, "Cannot get current location", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, "Cannot get current location", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		preferenceHelper = new PreferenceHelper(this);
		setContentView(R.layout.user_activity_location);
		this.btn_next = ((Button) findViewById(R.id.btn_next));
		this.btn_next.setOnClickListener(this);
		txt_address = (EditText) findViewById(R.id.txt_address);
		txt_address2 = (TextView) findViewById(R.id.txt_address2);
		setUpMapIfNeeded();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpLocationClientIfNeeded();
		mLocationClient.connect();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	Location mCurrentLocation;
	Handler mHandler = new Handler();

	public void showMyLocation() {
		if(mCurrentLocation!=null) return;
		if (mLocationClient != null && mLocationClient.isConnected()) {
			mCurrentLocation = mLocationClient.getLastLocation();
			// Toast.makeText(getApplicationContext(), msg,
			// Toast.LENGTH_SHORT).show();
			// Log.i("Tuan",
			// mCurrentLocation.getLatitude()+","+mCurrentLocation.getLongitude());
			if (mCurrentLocation == null) {
				Toast.makeText(this, "Waiting for location...",Toast.LENGTH_SHORT).show();
				return;
            }
			this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation
							.getLongitude()), 16.0F));

			new Thread(new Runnable() {

				@Override
				public void run() {
					Geocoder geocoder = new Geocoder(LocationActivity.this,
							Locale.getDefault());
					try {
						List<Address> addresses = geocoder.getFromLocation(
								mCurrentLocation.getLatitude(),
								mCurrentLocation.getLongitude(), 1);
						if(addresses.size()==0) return;
						address = addresses.get(0).getAddressLine(0);
						final String city = addresses.get(0).getAddressLine(1);
						final String country = addresses.get(0).getAddressLine(2);
						runOnUiThread(new Runnable() {
							public void run() {
								txt_address.setText(address);
								txt_address2.setText(city+", "+country);
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();

			this.mMap.addMarker(new MarkerOptions()
					.position(
							new LatLng(mCurrentLocation.getLatitude(),
									mCurrentLocation.getLongitude()))
					.title("Current Location").alpha(0.7F).draggable(true));

		} else {
			mHandler.removeCallbacks(checkLocation);
			mHandler.postDelayed(checkLocation, 500);
		}
	}

	Runnable checkLocation = new Runnable() {

		@Override
		public void run() {
			showMyLocation();
		}
	};

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getApplicationContext(), this, // ConnectionCallbacks
					this); // OnConnectionFailedListener
		}
	}

	public void onMarkerDrag(Marker paramMarker) {
	}

	public void onMarkerDragEnd(Marker paramMarker) {
		final LatLng localLatLng = paramMarker.getPosition();
		mCurrentLocation.setLatitude(localLatLng.latitude);
		mCurrentLocation.setLongitude(localLatLng.longitude);
		paramMarker.setTitle("Current location");
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				Geocoder geocoder = new Geocoder(LocationActivity.this,
						Locale.getDefault());
				try {
					List<Address> addresses = geocoder.getFromLocation(
							localLatLng.latitude,
							localLatLng.longitude, 1);
					if(addresses.size()==0) return;
					address = addresses.get(0).getAddressLine(0);
					final String city = addresses.get(0).getAddressLine(1);
					final String country = addresses.get(0).getAddressLine(2);
					runOnUiThread(new Runnable() {
						public void run() {
							txt_address.setText(address);
							txt_address2.setText(city+", "+country);
						}
					});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void onMarkerDragStart(Marker paramMarker) {
	}

	@Override
	public boolean onMyLocationButtonClick() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLocationChanged(Location arg0) {
		if (mCurrentLocation == null) {
			showMyLocation();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
		showMyLocation();
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}
}
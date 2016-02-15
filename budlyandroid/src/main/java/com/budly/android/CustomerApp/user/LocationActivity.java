package com.budly.android.CustomerApp.user;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.R;
import com.budly.android.CustomerApp.TransactionActivity;
//import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.budly.android.CustomerApp.td.utils.LocationHelper;
import com.budly.android.CustomerApp.td.utils.LocationHelperCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;

public class LocationActivity extends BaseActivity implements
		View.OnClickListener, GoogleMap.OnMarkerDragListener,
		OnMyLocationButtonClickListener {
	Button btn_next;
    ImageView btn_left;
	EditText txt_address;
	TextView txt_address2;
    GoogleMap mMap;
    Marker currentMarker;

//	private LocationClient mLocationClient;
//    private GoogleApiClient mLocationClient;
	PreferenceHelper preferenceHelper;
	private Address address;
    boolean realAddress;

//	private static final LocationRequest REQUEST = LocationRequest.create()
//			.setInterval(5000) // 5 seconds
//			.setFastestInterval(16) // 16ms = 60fps
//			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        preferenceHelper = PreferenceHelper.getInstance();
        setContentView(R.layout.user_activity_location);

        this.btn_next = ((Button) findViewById(R.id.btn_next));
        this.btn_next.setOnClickListener(this);
        this.btn_left = ((ImageView) findViewById(R.id.btn_left));
        this.btn_left.setOnClickListener(this);
        this.txt_address = (EditText) findViewById(R.id.txt_address);
        this.txt_address.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode()==KeyEvent.KEYCODE_ENTER){
                    Geocoder geocoder = new Geocoder(LocationActivity.this, Locale.getDefault());
                    try {
                        String search = txt_address.getText() +", "
                                + (address.getLocality()!=null?address.getLocality()+", ":"")
                                + (address.getAdminArea()!=null?address.getAdminArea()+", ":"");
                        Log.e("Edwin", "Search -> "+search);
                        List<Address> addresses = geocoder.getFromLocationName(search, 1);
                        final Address a;
                        if(addresses.size() > 0) {
                            a = addresses.get(0);
                            realAddress = true;
                        }
                        else {
                            realAddress = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LocationActivity.this,"Address not found",Toast.LENGTH_SHORT).show();
                                }
                            });
                            return false;
                        }
                        mCurrentLocation.setLatitude(a.getLatitude());
                        mCurrentLocation.setLongitude(a.getLongitude());
                        runOnUiThread(new Runnable() {
                            public void run() {
                                currentMarker.setPosition(new LatLng(a.getLatitude(), a.getLongitude()));
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(currentMarker.getPosition()));
                            }
                        });
                    }catch(IOException e){ }
                }
                return false;
            }
        });
        txt_address2 = (TextView) findViewById(R.id.txt_address2);
        setUpMapIfNeeded();
    }

    private void checkLocationServices(){
        LocationHelper.findLocationManager(this);
        if(!LocationHelper.isLocationServiceEnabled()){
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Please enable Location Services");
            dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
//                    finish();
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        }


    }

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
			if(mCurrentLocation!=null && realAddress) {
				try {
					finish();
                    Log.i("Edwin", "Address -> "+address.getAddressLine(0));
					preferenceHelper.setLocation(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
					preferenceHelper.setValue("location_address",
//                            address.getAddressLine(0)
                            txt_address.getText()+", "+txt_address2.getText());
					startActivity(new Intent(this, ListServiceInAreaActivity.class));
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(this, "Cannot get current location", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, "Cannot get current location", Toast.LENGTH_SHORT).show();
			}
			break;
            case R.id.btn_left:
                final String[] time = { "Transactions", "Update profile" };
                AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);
                builder.setTitle("Budly");
                builder.setItems(time, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int positon) {
                        if(positon==0) {
                            startActivity(new Intent(LocationActivity.this, TransactionActivity.class));
                        } else {
                            startActivity(new Intent(LocationActivity.this, ProfileUpdateActivity.class));
                        }
                    }
                });
                builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                break;
		}
	}



	@Override
	protected void onResume() {
		super.onResume();
        checkLocationServices();
		setUpLocationClientIfNeeded();
//		mLocationClient.connect();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null || !activeNetworkInfo.isConnected()){
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("No internet connection");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            dialog.setCancelable(true);
            dialog.show();
            txt_address.setEnabled(false);
        } else {
            txt_address.setEnabled(true);
        }
	}

	@Override
	public void onPause() {
		super.onPause();
//		if (mLocationClient != null) {
//			mLocationClient.disconnect();
//		}
        LocationHelper.getInstance().stopConnection();
	}

	Location mCurrentLocation;
	Handler mHandler = new Handler();

	public void showMyLocation() {
		if(mCurrentLocation!=null) return;
//		if (mLocationClient != null && mLocationClient.isConnected()) {
//			mCurrentLocation = mLocationClient.getLastLocation();
//            mCurrentLocation= LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        mCurrentLocation = LocationHelper.getInstance().getLastLocation();
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
						address = addresses.get(0);
                        realAddress = true;
                        final String street = address.getAddressLine(0);
                        final String city = address.getLocality();
                        final String state = address.getAdminArea();
                        final String country =address.getCountryName();

						runOnUiThread(new Runnable() {
							public void run() {
                                currentMarker.setTitle(
                                        street+", "
                                                +(city!=null?city+", ":"")
                                                +state+", "
                                                +country
                                );
								txt_address.setText(street);
                                txt_address2.setText((city!=null?city+", ":"")+state);
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();

			currentMarker= this.mMap.addMarker(new MarkerOptions()
					.position(
							new LatLng(mCurrentLocation.getLatitude(),
									mCurrentLocation.getLongitude()))
                    .draggable(true)
                    .title(
                            "Current Location"
                    ));

//		} else {
//			mHandler.removeCallbacks(checkLocation);
//			mHandler.postDelayed(checkLocation, 500);
//		}
	}

	Runnable checkLocation = new Runnable() {

		@Override
		public void run() {
			showMyLocation();
		}
	};

	private void setUpLocationClientIfNeeded() {
        LocationHelper.getInstance().startConnection(new LocationHelperCallback() {
            @Override
            public void connected() {
                showMyLocation();
            }
        });
//		if (mLocationClient == null) {
//            mLocationClient = new GoogleApiClient.Builder(this)
//                    .addApi(LocationServices.API)
//                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                        @Override
//                        public void onConnected(Bundle bundle) {
//                            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, REQUEST,
//                                    new LocationListener() {
//                                        @Override
//                                        public void onLocationChanged(Location location) {
//                                            if (mCurrentLocation == null) {
//                                                showMyLocation();
//                                            }
//                                        }
//                                    }); // LocationListener
//                            showMyLocation();
//                        }
//
//                        @Override
//                        public void onConnectionSuspended(int i) {
//
//                        }
//                    })
//                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
//                        @Override
//                        public void onConnectionFailed(ConnectionResult connectionResult) {
//                            Log.e("Tuan2", "loaction connected==============");
//                        }
//                    })
//                    .build();
//			mLocationClient = new LocationClient(getApplicationContext(), this, // ConnectionCallbacks
//					this); // OnConnectionFailedListener
//		}
	}

	public void onMarkerDrag(Marker paramMarker) {	}

	public void onMarkerDragEnd(Marker paramMarker) {
        Log.i("Edwin", "Drag Marker End");
		final LatLng localLatLng = paramMarker.getPosition();
		mCurrentLocation.setLatitude(localLatLng.latitude);
		mCurrentLocation.setLongitude(localLatLng.longitude);
//		paramMarker.setTitle(
//                address.getAddressLine(0)+", "
//                +address.getLocality()+", "
//                +address.getAdminArea()+", "
//                +address.getCountryName());
		
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
                    Log.i("Edwin", "Country -> "+addresses.get(0).getCountryName());
                    Log.i("Edwin", "Admin -> "+addresses.get(0).getAdminArea());
                    Log.i("Edwin", "SubAdmin -> "+addresses.get(0).getSubAdminArea());
                    Log.i("Edwin", "Locality -> "+addresses.get(0).getLocality());
                    Log.i("Edwin", "SubLocality -> "+addresses.get(0).getSubLocality());

                    address = addresses.get(0);
                    realAddress = true;
//					final String city = addresses.get(0).getAddressLine(addresses.get(0).getMaxAddressLineIndex()-2);
                    final String street = address.getAddressLine(0);
                    final String city = address.getLocality();
                    final String state = address.getAdminArea();
                    final String country =address.getCountryName();
//                    final String country = addresses.get(0).getAddressLine(addresses.get(0).getMaxAddressLineIndex()-1);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            currentMarker.setTitle(
                                    street + ", "
                                            + (city != null ? city + ", " : "")
                                            + state + ", "
                                            + country
                            );
                            txt_address.setText(street);
                            txt_address2.setText((city!=null?city+", ":"")+state);
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
        Log.i("Edwin", "Drag Marker Start");
	}

	@Override
	public boolean onMyLocationButtonClick() {
		// TODO Auto-generated method stub
		return false;
	}

//	@Override
//	public void onLocationChanged(Location arg0) {
//		if (mCurrentLocation == null) {
//			showMyLocation();
//		}
//	}

//	@Override
//	public void onConnectionFailed(ConnectionResult arg0) {
//		// TODO Auto-generated method stub
//        Log.e("Tuan2", "loaction connected==============");
//	}
//
//	@Override
//	public void onConnected(Bundle arg0) {
////		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
//		showMyLocation();
//	}
//
//	@Override
//	public void onDisconnected() {
//		// TODO Auto-generated method stub
//
//	}
}
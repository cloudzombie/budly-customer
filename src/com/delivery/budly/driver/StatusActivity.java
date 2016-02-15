package com.budly.android.CustomerApp.driver;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.budly.BaseActivity;
import com.budly.Common;
import com.budly.R;
import com.budly.TransactionActivity;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.user.PlaceOrderActivity;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

public class StatusActivity extends BaseActivity implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	
	ImageView btn_left, btn_available, btn_unavailable, rate;
	PreferenceHelper preferenceHelper;
	User mUser;
	public static boolean IS_FIRST = false;
	TextView txt_first_name,txt_status, time_on_rank;
	
	final public static int STATUS_ORERLOST = 0x0;
	final public static int STATUS_ORDERHAS = 0x1;
	final public static int STATUS_ORDERSUBMIT = 0x2;
	final public static int STATUS_ORDERWAIT = 0x3;
	final public static int STATUS_ORERCOMPLETE = 0x4;
	final public static int STATUS_ORERCONFIRM = 0x5;
	
	public static int CURRENT_STATUS = STATUS_ORDERWAIT; 
	
	private KeyguardManager.KeyguardLock mKeyguardLock;
	
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(10000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	private LocationClient mLocationClient;
	Location mCurrentLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferenceHelper = new PreferenceHelper(this);
		mUser = preferenceHelper.getUserInfo();
		setContentView(R.layout.driver_activity_status);
		btn_left = (ImageView) findViewById(R.id.btn_left);
		btn_available = (ImageView) findViewById(R.id.btn_i_am_available); 
		btn_unavailable = (ImageView) findViewById(R.id.btn_i_am_unavailable);
		rate = (ImageView) findViewById(R.id.rate);
		time_on_rank = (TextView) findViewById(R.id.time_on_rank);
		btn_left.setOnClickListener(this);
		btn_available.setOnClickListener(this);
		btn_unavailable.setOnClickListener(this);
		IS_FIRST = true;
		txt_first_name = (TextView) findViewById(R.id.txt_first_name);
		txt_first_name.setText(mUser.first_name);
		txt_status = (TextView) findViewById(R.id.txt_status);
		registerGCM();
		getCurrentRank();
		updateRankAnRate();
		
		KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
	    mKeyguardLock = km.newKeyguardLock("budly");
	    mKeyguardLock.disableKeyguard();
	}
	
	void registerGCM() {
		final HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback());
//		if (Build.FINGERPRINT.startsWith("generic")) {
//		    // running on an emulator
//			Log.e("Tuan", "Emulator======================");
//		} else {
		    // running on a device
			// Make sure the device has the proper dependencies.
			GCMRegistrar.checkDevice(this);

			// Make sure the manifest was properly set - comment out this line
			// while developing the app, then uncomment it when it's ready.
			GCMRegistrar.checkManifest(this);
	        
			String regId = GCMRegistrar.getRegistrationId(this);
			Log.e("Tuan", "GCMID="+regId);

			// Check if regid already presents
			if (regId.equals("")) {
				GCMRegistrar.register(this, Common.PROJECT_NUMBER);
			}
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					String regId="";
					
					while (regId.equals("")) {
						regId = GCMRegistrar.getRegistrationId(StatusActivity.this);
						Log.i("Tuan", "Registered with id "+regId);
						preferenceHelper.setGCMID(regId);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					client.updateDevice(mUser.id, regId);
				}
			}).start();
				
//		}
	}
	
	Handler mHandler = new Handler();
	void updateStatus(final int status_update) {
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onFailure(Exception error) {
				super.onFailure(error);
			}

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				Log.i("Tuan", re.toString());
				try {
					int status = re.getInt("status");
					if(status==200) {
						runOnUiThread(new Runnable() {
							public void run() {
								if(status_update==1) {
									runOnUiThread(new Runnable() {
										
										@Override
										public void run() {
											// TODO Auto-generated method stub
											btn_available.setImageResource(R.drawable.btn_i_am_available_pressed);
											btn_unavailable.setImageResource(R.drawable.btn_i_am_unavailable);
											txt_status.setText("Active");
										}
									});
								} else {
									runOnUiThread(new Runnable() {
										
										@Override
										public void run() {
											txt_status.setText("Not Active");
											btn_available.setImageResource(R.drawable.btn_i_am_available);
											btn_unavailable.setImageResource(R.drawable.btn_i_am_unavailable_pressed);
										}
									});
								}		
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
		client.updateStatus(mUser.id, status_update);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			//StatusActivity.this.openOptionsMenu();
			final String[] time = { "Manager Suppliers", "Update profile", "Deliveries" };
	        AlertDialog.Builder builder = new AlertDialog.Builder(StatusActivity.this);
	        builder.setTitle("Budly");
	        builder.setItems(time, new DialogInterface.OnClickListener() {
	          public void onClick(DialogInterface dialog, int positon) {
	        	  if(positon==0) {
	        		  startActivity(new Intent(StatusActivity.this, ManagerSupplierActivity.class));
	        	  } else if(positon==1){
	        		  startActivity(new Intent(StatusActivity.this, ProfileUpdateActivity.class));
	        	  } else {
	        		  startActivity(new Intent(StatusActivity.this, TransactionActivity.class));
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
		case R.id.btn_i_am_available:
			//startActivity(new Intent(this, WaitingActivity.class));
			updateStatus(1);
			break;
		case R.id.btn_i_am_unavailable:
			updateStatus(0);
			break;

		default:
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.driver, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.manager_suppliers:
	        	startActivity(new Intent(this, ManagerSupplierActivity.class));
	            return true;
	        case R.id.update_profile:
	        	startActivity(new Intent(this, ProfileUpdateActivity.class));
	            return true;
	        case R.id.delivery:
	        	startActivity(new Intent(this, TransactionActivity.class));
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	AlertDialog alert;
	@Override
	protected void onResume() {
		super.onResume();
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getApplicationContext(), this, this); // OnConnectionFailedListener
		}
		mLocationClient.connect();
		CURRENT_STATUS = STATUS_ORDERWAIT;
		int dem = 0;
		try {
			dem = Integer.parseInt(preferenceHelper.getValue("missing_order"));
		} catch (Exception e) { }
		if(dem>0) {
			alert = new AlertDialog.Builder(StatusActivity.this)
		    .setTitle("Budly")
		    .setMessage(dem+" missed order")
		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            preferenceHelper.setValue("missing_order", "0");
		        }
		     })
		    .setIcon(android.R.drawable.ic_dialog_alert)
		    .show();
		}
	}
	
	void getCurrentRank() {
		if(mUser!=null) {
			HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

				@Override
				public void onSuccess(HttpResponse httpResponse, JSONObject re) {
					try {
						int status = re.getInt("status");
						if(status == 200) {
							JSONObject data = re.getJSONObject("data");
							preferenceHelper.setValue("avg_rate", String.valueOf(data.getInt("avg_rate")));
							preferenceHelper.setValue("on_time_rank", String.valueOf(data.getDouble("on_time_rank")));
							updateRankAnRate();
						}
					} catch (Exception e) { e.printStackTrace(); }
				}

				@Override
				public void onFailure(Exception e) {
					super.onFailure(e);
				}
				
			});
			client.getCurrentRank(mUser.id);
		}
	}
	
	void updateRankAnRate() {
		try {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Log.e("Tuan", preferenceHelper.getValue("avg_rate")+"==="+preferenceHelper.getValue("time_on_rank"));
					try {
						int resID = getResources().getIdentifier("star"+preferenceHelper.getValue("avg_rate"), "drawable", getPackageName());
						rate.setImageDrawable(getResources().getDrawable(resID));
					} catch (Exception e) { }
					try {
						time_on_rank.setText(((int)Double.parseDouble(preferenceHelper.getValue("on_time_rank")))+"%");
					} catch (Exception e) { }
				}
			});
		} catch (Exception e) { }
	}
	
	@Override
	protected void onPause() {
		try {
			if(alert!=null)
				alert.dismiss();
		} catch (Exception e) { }
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
		super.onPause();
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		Log.e("Tuan2", "loaction change==============");
		if(mCurrentLocation==null) {
			mCurrentLocation = mLocationClient.getLastLocation();
			HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){
				@Override
				public void onSuccess(HttpResponse httpResponse, JSONObject re) {
					try {
						JSONObject data = re.getJSONObject("data");
						JSONArray list = data.getJSONArray("list_suppliers");
						Common.DISTANCE_DEFAULT = data.getInt("distance_default");
						final String status = data.getString("status");
						try {

							
							try {
								if(status.equals("freeze")) {
									runOnUiThread(new Runnable() {
										
										@Override
										public void run() {
											AlertDialog dialog = new AlertDialog.Builder(StatusActivity.this)
										    .setTitle("Budly")
										    .setMessage("Your account has been frozen by Budly")
										    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
										        public void onClick(DialogInterface dialog, int which) { 
										        	dialog.dismiss();
										        	finish();
										        }
										     })
										    .setIcon(android.R.drawable.ic_dialog_alert)
										    .setCancelable(false)
										    .show();
										}
									});
									return;
								}
								
								if(list.length()<=0) {
									runOnUiThread(new Runnable() {
										
										@Override
										public void run() {
											AlertDialog dialog = new AlertDialog.Builder(StatusActivity.this)
										    .setTitle("Budly")
										    .setMessage("You are not verified by any suppliers")
										    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
										        public void onClick(DialogInterface dialog, int which) { 
										        	dialog.dismiss();
										        }
										     })
										    .setIcon(android.R.drawable.ic_dialog_alert)
										    .show();
										}
									});
								}
							} catch (Exception e) { }
						} catch (Exception e) { }
						return;
					} catch (Exception e) { }
					mCurrentLocation = null;
				}
				
				@Override
				public void onFailure(Exception e) {
					super.onFailure(e);
					mCurrentLocation = null;
				}
			});
			if(mCurrentLocation==null) return;
			preferenceHelper.setLocation(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
			client.on_start(mUser.id, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		Log.e("Tuan2", "loaction connect failed==============");
	}

	@Override
	public void onConnected(Bundle arg0) {
		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
		Log.e("Tuan2", "loaction connected==============");
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		Log.e("Tuan2", "loaction disconnected==============");
	}
}

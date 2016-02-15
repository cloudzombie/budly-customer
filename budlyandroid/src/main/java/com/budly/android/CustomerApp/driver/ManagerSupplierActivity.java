package com.budly.android.CustomerApp.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.android.CustomerApp.Common;
import com.budly.R;
import com.budly.android.CustomerApp.beans.Supplier;
import com.budly.android.CustomerApp.beans.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

public class ManagerSupplierActivity extends BaseActivity implements
		OnClickListener, OnMyLocationButtonClickListener {
	
//	private LocationClient mLocationClient;
private GoogleApiClient mLocationClient;

    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    
	DisplayImageOptions options = new DisplayImageOptions.Builder()
	.showStubImage(R.drawable.generic_logo)
	.showImageForEmptyUri(R.drawable.generic_logo)
	.showImageOnFail(R.drawable.generic_logo)
	//.displayer(new RoundedBitmapDisplayer(200))
	.cacheInMemory(false).cacheOnDisc(true).build();
    
	ImageView btn_next;
	ListView listView;
	ImageView btn_minus, btn_plus;
	
	HashMap<Integer, Supplier> checked = new HashMap<Integer, Supplier>();
	
	int valueMile = 50;
	
	TextView txt_value_mile;
	MyAdapter adapter;

	PreferenceHelper preferenceHelper;
	User mUser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Common.setScreenFlag(this);

		setContentView(R.layout.driver_activity_manager_supplier);
		preferenceHelper = PreferenceHelper.getInstance();
		mUser = preferenceHelper.getUserInfo();
		btn_next = (ImageView) findViewById(R.id.btn_next);
		btn_minus = (ImageView) findViewById(R.id.btn_minus);
		btn_plus = (ImageView) findViewById(R.id.btn_plus);
		btn_next.setOnClickListener(this);
		btn_minus.setOnClickListener(this);
		btn_plus.setOnClickListener(this);
		
		txt_value_mile = (TextView) findViewById(R.id.txt_value_mile);
		txt_value_mile.setText(String.valueOf(valueMile));
		
		listView = (ListView) findViewById(R.id.list);
		adapter = new MyAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position>=0 && position<mList.size()) {
					Supplier item = mList.get(position);
					if(checked.get(item.id)==null) {
						checked.put(item.id, item);
					} else {
						checked.remove(item.id);
					}
					adapter.notifyDataSetChanged();
				}
			}
		});
	}
	
	void saveChecked() {
		try {
			JSONArray jsa = new JSONArray();
			for(Entry<Integer, Supplier> entry : checked.entrySet()) {
			    //int key = entry.getKey();
			    Supplier value = entry.getValue();
			    if(value!=null) {
			    	jsa.put(value.toString());
			    }
			}
			preferenceHelper.setChecked(jsa.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void selectSupplier() {
		saveChecked();
		JSONArray ja = new JSONArray();
		for(Entry<Integer, Supplier> entry : checked.entrySet()) {
		    int key = entry.getKey();
		    Supplier value = entry.getValue();
		    if(value!=null) {
		    	ja.put(key);
		    }
		}
		HttpBasicClientHelper client_select_supplier = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onFailure(Exception error) {
				super.onFailure(error);
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						try {
							Toast.makeText(getApplication(), "Update supplier unsuccess. Please try again", Toast.LENGTH_SHORT).show();
						} catch (Exception e) { }
					}
				});
			}

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				Log.i("Tuan", re.toString());
				try {
					int status = re.getInt("status");
					if(status==200) {
						try {
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
//									new AlertDialog.Builder(ManagerSupplierActivity.this)
//								    .setTitle("Budly")
//								    .setMessage("Add a supplier and wait confirm from supplier")
//								    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//								        public void onClick(DialogInterface dialog, int which) { 
//								            finish();
//								        }
//								     })
//								    .setIcon(android.R.drawable.ic_dialog_alert)
//								    .show();
									try {
										Toast.makeText(getApplication(), "Update supplier success!", Toast.LENGTH_SHORT).show();
									} catch (Exception e) { }
								}
							});
							finish();
						} catch (Exception e) {
							e.printStackTrace();
						}
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(getApplication(), "Update supplier unsuccess. Please try again", Toast.LENGTH_SHORT).show();
						}
					});
				} catch (Exception e) { }
			}
			
		});
		Log.i("Tuan", ja.toString()+"/"+mUser.id);
		client_select_supplier.selectSupplier(mUser.id, ja);
	}
	
	Runnable update = new Runnable() {
		
		@Override
		public void run() {
			mPage = 0;
			Toast.makeText(ManagerSupplierActivity.this, "Waiting for update...", Toast.LENGTH_LONG).show();
			getData(mPage);
		}
	};
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
			selectSupplier();
			//startActivity(new Intent(this, StatusActivity.class));
			break;
		case R.id.btn_minus:
			if(valueMile>25) {
				valueMile-=25;
				txt_value_mile.setText(String.valueOf(valueMile));
				mHandler.removeCallbacks(update);
				mHandler.postDelayed(update, 1000);
			}
			break;
		case R.id.btn_plus:
			if(valueMile<100) {
				valueMile+=25;
				txt_value_mile.setText(String.valueOf(valueMile));
				mHandler.removeCallbacks(update);
				mHandler.postDelayed(update, 1000);
			}
			break;

		default:
			break;
		}
	}
	
	ArrayList<Supplier> mList = new ArrayList<Supplier>();
	class MyAdapter extends BaseAdapter {

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		};

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			MyHolder holder = null;

			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.driver_service_item, parent, false);
				holder = new MyHolder();
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.ic_check = (ImageView) row.findViewById(R.id.ic_check);
				holder.thumb = (ImageView) row.findViewById(R.id.thumb);
				holder.rate = (ImageView) row.findViewById(R.id.rate);
				holder.layout = (RelativeLayout) row.findViewById(R.id.service_item_layout);
				row.setTag(holder);
			} else {
				holder = (MyHolder) row.getTag();
			}

			Supplier item = mList.get(position);
			holder.title.setText(item.name);
			//holder.thumb.setImageResource(item.thumb);
			ImageLoader.getInstance().displayImage(item.image, holder.thumb, options);
			try {Log.e("Tuan", "star"+item.avg_rate);
				int resID = getResources().getIdentifier("star"+item.avg_rate, "drawable", getPackageName());
				Log.e("Tuan", "star"+item.avg_rate+" "+resID);
				holder.rate.setImageDrawable(getResources().getDrawable(resID));
			} catch (Exception e) { e.printStackTrace(); }
		
			
			if(checked.get(item.id)!=null) {
				holder.ic_check.setImageResource(R.drawable.check);
			} else {
				holder.ic_check.setImageResource(R.drawable.uncheck);
			}
			
			if(position%2==0) {
				holder.layout.setBackgroundColor(Color.parseColor("#aaffffff"));
			} else {
				holder.layout.setBackgroundColor(Color.parseColor("#aaf1f0ec"));
			}
			
			return row;
		}
		

	}
	static class MyHolder {
		RelativeLayout layout;
		TextView title;
		ImageView thumb;
		ImageView ic_check;
		ImageView rate;
	}
	
	Location mCurrentLocation;
	Handler mHandler = new Handler();
    public void showMyLocation() {
        if (mLocationClient != null && mLocationClient.isConnected()) {
//             mCurrentLocation = mLocationClient.getLastLocation();
            mCurrentLocation= LocationServices.FusedLocationApi.getLastLocation(mLocationClient);

            //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            //Log.i("Tuan", mCurrentLocation.getLatitude()+","+mCurrentLocation.getLongitude());
             if (mCurrentLocation == null) {
            	 Toast.makeText(this, "Waiting for location...", Toast.LENGTH_SHORT).show();
            	 return;
             }
             getData(0);
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
	
	@Override
	public boolean onMyLocationButtonClick() {
		// TODO Auto-generated method stub
		return false;
	}

//	@Override
//	public void onLocationChanged(Location location) {
//		//Log.i("Tuan", "Location = " + location);
//		if (mCurrentLocation == null) {
//			showMyLocation();
//		}
//	}
//
//	@Override
//	public void onConnectionFailed(ConnectionResult arg0) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void onConnected(Bundle arg0) {
//		// TODO Auto-generated method stub
//		 mLocationClient.requestLocationUpdates(
//	                REQUEST,
//	                this);  // LocationListener
//		 showMyLocation();
//	}
//
//	@Override
//	public void onDisconnected() {
//		// TODO Auto-generated method stub
//
//	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
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
    
    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, REQUEST,
                                    new LocationListener() {
                                        @Override
                                        public void onLocationChanged(Location location) {
                                            if (mCurrentLocation == null) {
                                                showMyLocation();
                                            }
                                        }
                                    }); // LocationListener
                            showMyLocation();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            Log.e("Tuan2", "loaction connected==============");
                        }
                    })
                    .build();
//            mLocationClient = new LocationClient(
//                    getApplicationContext(),
//                    this,  // ConnectionCallbacks
//                    this); // OnConnectionFailedListener
        }
    }
    
    void getData(final int page) {
    	HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

    		@Override
    		public void onFailure(Exception error) {
    	    	if(page==0) {
    	    		mList.clear();
    	    	}
    		}

    		@Override
    		public void onSuccess(HttpResponse httpResponse, JSONObject re) {
    			Log.i("Tuan", re.toString());
    			try {
    				if(page==0) {
    		    		mList.clear();
    		    	}
    				int status = re.getInt("status");
    				if(status==200) {
    					JSONObject data = re.getJSONObject("data");
    					try {
    						if(data.has("list_suppliers")) {
    							JSONArray list_suppliers = data.getJSONArray("list_suppliers");
    							preferenceHelper.setChecked(list_suppliers.toString());
    							checked.clear();
    							for (int i = 0; i < list_suppliers.length(); i++) {
    								Supplier item = Supplier.parse(list_suppliers.getJSONObject(i));
    								checked.put(item.id, item);
    							}
    						}	
    					} catch (Exception e) {
    						e.printStackTrace();
    					}
    					
    					try {
    						if(data.has("lists")) {
    							JSONArray list = data.getJSONArray("lists");
    							for (int i = 0; i < list.length(); i++) {
    								JSONObject ji = list.getJSONObject(i);
    								mList.add(new Supplier(ji.getString("name"), ji.getInt("id"), ji.getInt("avg_rate"), ji.getString("image")));
    							}
    						}	
    					} catch (Exception e) {
    						e.printStackTrace();
    					}
    					runOnUiThread(new Runnable() {
    						
    						@Override
    						public void run() {
    							try {
    								Toast.makeText(getApplication(), "Get supplier done!", Toast.LENGTH_SHORT).show();
        							adapter.notifyDataSetChanged();
								} catch (Exception e) { }
    						}
    					});
    				}
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    		
    	});
    	if(mCurrentLocation==null) {
	    	try {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), "Can not get your location. Please check your setting.", Toast.LENGTH_SHORT).show();
					}
				});
			} catch (Exception e) { }
	    	return;
    	}
    	client.getSuppliersWithDriver(mUser.id, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), valueMile, page);
    }
    
    int mPage = 0;
}
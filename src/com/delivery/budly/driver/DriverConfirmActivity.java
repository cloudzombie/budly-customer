package com.budly.android.CustomerApp.driver;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.BaseActivity;
import com.budly.Common;
import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
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
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.budly.android.CustomerApp.td.widget.MyRecorder;
import com.budly.android.CustomerApp.td.widget.ProgressButton;
import com.turbomanage.httpclient.HttpResponse;

public class DriverConfirmActivity extends BaseActivity implements
		View.OnClickListener, GoogleMap.OnMarkerDragListener,
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener,
		OnMyLocationButtonClickListener {
	Button btn_next;
	GoogleMap mMap;
	private LocationClient mLocationClient;
	PreferenceHelper preferenceHelper;
	int customer_id = -1;
	ProgressButton progressButton;	
	int currentProgress = 0;
	User mUser;
	View report, viewOrderDetail;
	boolean isOk = false;
	TextView unit; 
		
	MyRecorder mRecorder ;
	final int TIME_STEP = 100;
	
	TextView txt_min;

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
			this.mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			if (this.mMap != null)
				setUpMap();
		}
	}
	
	void replyConfirm() {
		new AlertDialog.Builder(this)
	    .setTitle("Budly")
	    .setMessage("Are you sure you want to confirm to complete this order?")
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	acceptConfirm();
	        }
	     })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	denyConfirm();
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_alert)
	     .show();
	}
	
	ProgressDialog progressDialog;
	void requestConfirm() {
		showMyDialog("Sending request...");
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				try {
					int status = re.getInt("status");
					if(status==200) {
						showMyDialog("Waiting accept...");
						return;
					}
				} catch (Exception e) { }
				hiddenDialog();
				showToast("Request failed");
			}

			@Override
			public void onFailure(Exception e) {
				hiddenDialog();
				showToast("Request failed");
				super.onFailure(e);
			}
		});
		
		client.requestConfirm(preferenceHelper.getUserInfo().id, order_id);
	}
	
	void acceptConfirm() {
		showMyDialog("Sending accept request...");
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				try {
					int status = re.getInt("status");
					if(status==200) {
						hiddenDialog();
						finish();
						try {
							startActivity(new Intent(getApplicationContext(), OrderCompleteActivity.class));	
						} catch (Exception e) { }
						return;
					}
				} catch (Exception e) { }
				hiddenDialog();
				showToast("Request failed");
			}

			@Override
			public void onFailure(Exception e) {
				hiddenDialog();
				showToast("Request failed");
				super.onFailure(e);
			}
			
		});
		client.acceptConfirm(preferenceHelper.getUserInfo().id, order_id);
	}
	
	void denyConfirm() {
		showMyDialog("Sending deny request...");
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				try {
					int status = re.getInt("status");
					if(status==200) {
						hiddenDialog();
						return;
					}
				} catch (Exception e) { }
				hiddenDialog();
				showToast("Request failed");
			}

			@Override
			public void onFailure(Exception e) {
				// TODO Auto-generated method stub
				super.onFailure(e);
			}
			
		});
		client.denyConfirm(preferenceHelper.getUserInfo().id, order_id);
	}
	
	void hiddenDialog() {
		try {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						if(progressDialog!=null && progressDialog.isShowing()) progressDialog.dismiss();
					} catch (Exception e) { }
				}
			});
		} catch (Exception e) { }
	}
	
	void showMyDialog(final String msg) {
		try {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						if(progressDialog!=null && progressDialog.isShowing()) {
							progressDialog.setMessage(msg);
						} else {
							progressDialog = ProgressDialog.show(DriverConfirmActivity.this, "", msg);
						}
					} catch (Exception e) { }
					mHandler.removeCallbacks(rHiddenDlg);
					mHandler.postDelayed(rHiddenDlg, 120000);
				}
			});
		} catch (Exception e) { e.printStackTrace(); }
		
	}
	
	Runnable rHiddenDlg = new Runnable() {
		
		@Override
		public void run() {
			hiddenDialog();
		}
	};
	
	void showToast(final String msg) {
		try {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
					} catch (Exception e) { }
				}
			});
		} catch (Exception e) { }
	}

	public void onClick(View paramView) {
		Log.e("Tuan2", "2222222222222222222");
		switch (paramView.getId()) {
		default:
			break;
		case R.id.btn_next:
			requestConfirm();
			break;
		case R.id.report:
			finish();
			startActivity(new Intent(this, ReportActivity.class));
			break;
		case R.id.viewOrderDetail:
			Log.e("Tuan2", "11111111111111111111111");
			startActivity(new Intent(this, OrderDetailActivity.class));
			break;
		}
	}

	int order_id = -1;
	int time=0, time_tmp=0;
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		preferenceHelper = new PreferenceHelper(this);
		mUser = preferenceHelper.getUserInfo();
		setContentView(R.layout.driver_activity_confirm);
		viewOrderDetail = findViewById(R.id.viewOrderDetail);
		viewOrderDetail.setOnClickListener(this);
		txt_min = (TextView) findViewById(R.id.txt_min);
		unit = (TextView) findViewById(R.id.unit);
		this.btn_next = ((Button) findViewById(R.id.btn_next));
		this.btn_next.setOnClickListener(this);
		progressButton = (ProgressButton) findViewById(R.id.pin_progress_1);
		progressButton.setInnerSize(getResources().getDimensionPixelSize(R.dimen.progress2_inner_size));
		try {
			time = getIntent().getIntExtra("estimate_time", 0);
			progressButton.setMax(time*60000);
			time_tmp = time*60000;
			txt_min.setText(""+time);
			unit.setText("Minutes");
		} catch (Exception e) { }
		
		progressButton.setProgress(currentProgress);
		report = findViewById(R.id.report);
		report.setOnClickListener(this);
		setUpMapIfNeeded();
		btnRecoder = (ImageView) findViewById(R.id.btn_recorder);
		try {
			order_id = getIntent().getIntExtra("order_id", -1);
		} catch (Exception e) { }
		try {
			customer_id = getIntent().getIntExtra("customer_id", -1);
		} catch (Exception e) { }
		setUpRecord();
		registerBroadcast();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					int n = (int) Math.round(time*60000/TIME_STEP+0.5);
					for (int i = 1; i <= n; i++) {
						if(!isOk) {
							try {
								Thread.sleep(TIME_STEP);	
							} catch (Exception e) { }
							currentProgress+=TIME_STEP;
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									try {
										runOnUiThread(new Runnable() {
											
											@Override
											public void run() {
												try {
													time_tmp -= TIME_STEP;
													if(time_tmp<0) time_tmp = 0;
													if(time_tmp<60000) {
														txt_min.setText(""+(int)(time_tmp/1000));
														unit.setText("Seconds");
													} else {
														unit.setText("Minutes");
														txt_min.setText(String.valueOf((int)(time_tmp*1f/60000+0.5f)));	
													}
												} catch (Exception e) { }
											}
										});
									} catch (Exception e) { }
									
									if(currentProgress>=time*60000) currentProgress = time*60000;
									progressButton.setProgress(currentProgress);
								}
							});
						} else {
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
            Log.i("Edwin", "Receive Broadcast");
			String action = intent.getAction();
			if(action.equals(REQUEST_CONFIRM)) {
				
			} else if(action.equals(ACCEPT_CONFIRM)) {
				hiddenDialog();
				showToast("Request confirm has accepted");
				finish();
				try {
					startActivity(new Intent(getApplicationContext(), OrderCompleteActivity.class));	
				} catch (Exception e) { }
			} else if(action.equals(DENY_CONFIRM)) {
				hiddenDialog();
				showToast("Request confirm has deny");
			}
		}
	};
	
	
	public final static String REQUEST_CONFIRM = "request_confirm";
	public final static String ACCEPT_CONFIRM = "accept_confirm";
	public final static String DENY_CONFIRM = "deny_confirm";
	
	void registerBroadcast() {
		IntentFilter intent = new IntentFilter();
		//intent.addAction(REQUEST_CONFIRM);
		intent.addAction(ACCEPT_CONFIRM);
		intent.addAction(DENY_CONFIRM);
		registerReceiver(mReceiver, intent);
	}
	
	void unRegisterBroadcast() {
		unregisterReceiver(mReceiver);
	}
	
	ImageView btnRecoder;
	String fileName = "";
	void setUpRecord() {
		btnRecoder.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				try {
					Intent intent = new Intent(Intent.ACTION_CALL);
					intent.setData(Uri.parse("tel:" + Common.MOBILE_PHONE));
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(DriverConfirmActivity.this, "Try to call failed. Please try again", Toast.LENGTH_SHORT).show();
				}
			}
		});
		/*
		btnRecoder.setOnTouchListener(new OnTouchListener() {
			private long startTimeRecord = 0;
			private long lastActionDown = 0;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					long currentTime = System.currentTimeMillis();
					if((currentTime-lastActionDown)<400) return true;
					lastActionDown = currentTime;
					startTimeRecord = System.currentTimeMillis();
					
					Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					// Vibrate for 100 milliseconds
					vib.vibrate(80);
					
					btnRecoder.setImageResource(R.drawable.recoder_pressed);
																									// popup
					SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
					fileName = Common.SDCARD_AUDIO + "audio_" + mUser.id + "_" + timeStampFormat.format(new Date()) + ".m4a";
					new Thread(new Runnable() {
						public void run() {
							// make sure the SD card is present for the recording
							if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
								try {
									startRecord(fileName);
									startTimeRecord = System.currentTimeMillis();
									if (mRecorder.getState() == MyRecorder.State.ERROR) {
										fileName = null;
										Log.e("Tuan", "Ghi am bị lỗi");
									}
								} catch (Exception e) { startTimeRecord = System.currentTimeMillis(); fileName = null; }
							}
						}
					}).start();
					return true;

				case MotionEvent.ACTION_MOVE:
					return true;
				case MotionEvent.ACTION_UP:
					btnRecoder.setImageResource(R.drawable.recoder_normal);
					Log.i("", "Reset");
					long currentTimeUp = System.currentTimeMillis();
					if((currentTimeUp-lastActionDown)<400) return true;
					lastActionDown = currentTimeUp;
					if(mRecorder==null || !mRecorder.isRecording()) {
						Log.e("Tuan", "Khong ghi am");
						return false;
					}
					Log.e("Tuan", "Co ghi am");
					if ((System.currentTimeMillis() - this.startTimeRecord) > 1000) {
						Log.e("Tuan", "Vao day==========");
						new Thread(new Runnable() {
							
							@Override
							public void run() {
								try {
									Log.e("Tuan", "2==============");
									if(fileName==null) {
										Log.e("Tuan", "File name null");
										return;
									}
									try {
										Thread.sleep(300);
									} catch (InterruptedException e1) { }
									try {
										stopRecord();
									} catch (Exception e) { e.printStackTrace(); }
									try {
										Thread.sleep(300);
									} catch (InterruptedException e1) { }
									int dem = 0;
									while (true) {
										Log.e("Tuan", "4==============" + mRecorder.getState() + " " + String.valueOf(mRecorder.isRecording()));
										if(mRecorder==null || !mRecorder.isRecording()) {
											Log.e("Tuan", "3==============");
											LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
											data.put("from_id", String.valueOf(mUser.id));
											data.put("to_id", String.valueOf(customer_id));
											String res = UploadFile.uploadFile("http://mybudly.com/DAPP/api/user/sendVoice", fileName, getApplicationContext(), data);
											Log.i("Tuan", res);
											break;
										} else {
											stopRecord();
										}
										try {
											Thread.sleep(300);
										} catch (InterruptedException e1) { }
										if(dem>=5) {
											break;
										}
										dem++;
									}
								} catch (Exception e) { e.printStackTrace(); }
							}
						}).start();
						return true;
					} else {
						try {
							stopRecord();
						} catch (Exception e) { e.printStackTrace(); }
					}
					break;
				}
				return true;
			}
		});
		*/
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpLocationClientIfNeeded();
		mLocationClient.connect();
		StatusActivity.CURRENT_STATUS = StatusActivity.STATUS_ORERCONFIRM;
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

//			new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					Geocoder geocoder = new Geocoder(DriverConfirmActivity.this,
//							Locale.getDefault());
//					try {
//						List<Address> addresses = geocoder.getFromLocation(
//								mCurrentLocation.getLatitude(),
//								mCurrentLocation.getLongitude(), 1);
//						if(addresses.size()==0) return;
//						final String address = addresses.get(0).getAddressLine(0);
//						final String city = addresses.get(0).getAddressLine(1);
//						final String country = addresses.get(0).getAddressLine(2);
//						runOnUiThread(new Runnable() {
//							public void run() {
//								txt_address.setText(address);
//								txt_address2.setText(city+", "+country);
//							}
//						});
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}).start();

			this.mMap.addMarker(new MarkerOptions()
					.position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
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
		
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				Geocoder geocoder = new Geocoder(DriverConfirmActivity.this,
//						Locale.getDefault());
//				try {
//					List<Address> addresses = geocoder.getFromLocation(
//							localLatLng.latitude,
//							localLatLng.longitude, 1);
//					if(addresses.size()==0) return;
//					final String address = addresses.get(0).getAddressLine(0);
//					final String city = addresses.get(0).getAddressLine(1);
//					final String country = addresses.get(0).getAddressLine(2);
//					runOnUiThread(new Runnable() {
//						public void run() {
//							txt_address.setText(address);
//							txt_address2.setText(city+", "+country);
//						}
//					});
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}).start();
	}
	
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unRegisterBroadcast();
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
	
	void startRecord(String output) {
		try {
			mRecorder = new MyRecorder(0, 0, 0, 0);
			mRecorder.reset();
			mRecorder.setOutputFile(output);
			mRecorder.prepare();
			mRecorder.start(); // Recording is now started	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void stopRecord() {
		try {
			if(mRecorder!=null) {
				mRecorder.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
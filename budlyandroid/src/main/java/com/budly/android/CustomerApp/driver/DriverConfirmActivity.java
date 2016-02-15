package com.budly.android.CustomerApp.driver;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.android.CustomerApp.Common;
import com.budly.android.CustomerApp.Globals;
import com.budly.R;
import com.budly.android.CustomerApp.TransactionActivity;
import com.budly.android.CustomerApp.beans.User;
//import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.LocationHelper;
import com.budly.android.CustomerApp.td.utils.LocationHelperCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.budly.android.CustomerApp.td.widget.MyRecorder;
import com.budly.android.CustomerApp.td.widget.ProgressButton;
import com.turbomanage.httpclient.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DriverConfirmActivity extends BaseActivity implements
		View.OnClickListener, GoogleMap.OnMarkerDragListener,
		OnMyLocationButtonClickListener, DriverInterface,Progress {
    Button btn_next;
    ImageView btn_back, report;
	GoogleMap mMap;
//    private GoogleApiClient mLocationClient;
    private LocationHelper mLocationClient;
    PreferenceHelper preferenceHelper;
	int customer_id = -1;
	ProgressButton progressButton;	
	int currentProgress = 0;
	User mUser;
//	View report;
	boolean isOk = false;
    boolean orderExist = true;
	TextView unit;
    TextView timeEstimatedText,destiantionTxtView,orderCountTextView;
    FrameLayout btn_completedOrders,btn_activeOrders,btn_orderDetails;
    boolean calledByNotify = false;

	MyRecorder mRecorder ;
	final int TIME_STEP = 100;
    final int TIME_WAIT = 20000;

    boolean hasAccepted = false;
	
	TextView txt_min;

    Globals globals;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
//			.setFastestInterval(16) // 16ms = 60fps
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
		}else {
//            mMap.clear();
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

    void forceConfirmOrder(){
        hiddenDialog();
        showMyDialog("Accepting order...");
        HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){
            @Override
            public void onSuccess(HttpResponse httpResponse, JSONObject re) {
                try {
                    int status = re.getInt("status");
                    if(status==200) {
                        hiddenDialog();
                        Intent intent = new Intent();
                        intent.setAction(DriverConfirmActivity.ACCEPT_CONFIRM);
                        intent.putExtra("order_id", order_id);
                        DriverConfirmActivity.this.sendBroadcast(intent);
                        return;
                    }
                } catch (Exception e) {

                }
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

        client.driverConfirmOrder(order_id, mUser.id);

    }

	ProgressDialog progressDialog;
    Timer timer = new Timer();
	void requestConfirm() {
		showMyDialog("Sending request...");
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				try {
					int status = re.getInt("status");
					if(status==200) {
                        Log.e("Edwin", "20s to confirm");
                        timer.purge();
                        confirmOrderTask=createTastk();
                        timer.schedule(confirmOrderTask, TIME_WAIT);

						showMyDialog("Waiting accept...");
						return;
					}
				} catch (Exception e) {
                    showToast("Request failed");
                }
				hiddenDialog();

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

    TimerTask confirmOrderTask;
    TimerTask createTastk() {
        return new TimerTask() {
            @Override
            public void run() {
                Log.e("Edwin", "Now confirm");
                forceConfirmOrder();
            }
        };
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
            case R.id.btn_next:
                requestConfirm();
                break;
            case R.id.btn_report:
//                finish();
                Intent i = new Intent(this, ReportActivity.class);
                        i.putExtra("orderId",order_id);
                startActivity(i);
                break;
            case R.id.btn_left:
                //StatusActivity.this.openOptionsMenu();
                final String[] time = { "Manage Suppliers", "Update profile", "Deliveries", "Main Menu" };
                AlertDialog.Builder builder = new AlertDialog.Builder(DriverConfirmActivity.this);
                builder.setTitle("Budly");
                builder.setItems(time, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        if(position==0) {
                            startActivity(new Intent(DriverConfirmActivity.this, ManagerSupplierActivity.class));
                        } else if(position==1){
                            startActivity(new Intent(DriverConfirmActivity.this, ProfileUpdateActivity.class));
                        } else  if(position==2){
                            startActivity(new Intent(DriverConfirmActivity.this, TransactionActivity.class));
                        } else if(position==3){
                            startActivity(new Intent(DriverConfirmActivity.this, StatusActivity.class));
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
            case R.id.btn_orderDetails:
                Log.e("Tuan2", "Click Order Details");
                startActivity(new Intent(this, OrderDetailActivity.class));

                break;
            case R.id.btn_CompletedOrders:
                Log.e("Tuan2", "Click Completed Orders");
                startActivity(new Intent(this,CompletedOrdersActivity.class));

                break;
            case R.id.btn_activeOrders:
                Log.e("Tuan2", "Click Active Orders");
                startActivity(new Intent(this,ActiveOrdersActivity.class));

                break;
            default:
                break;
        }
	}

	int order_id = -1;
	int time=0, time_tmp=0;
    Marker currentMarker, destinationMarker;
    String destination;
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

        Common.turnOnScreen(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		preferenceHelper = PreferenceHelper.getInstance();
		mUser = preferenceHelper.getUserInfo();
		setContentView(R.layout.driver_activity_confirm);
        destiantionTxtView = (TextView)findViewById(R.id.destiantionTxtView);

        destination = getIntent().getStringExtra("address");

        destiantionTxtView.setText(destination);
		txt_min = (TextView) findViewById(R.id.txt_min);
		unit = (TextView) findViewById(R.id.unit);
        this.orderCountTextView = (TextView) findViewById(R.id.orderCounttextView);
		this.btn_next = ((Button) findViewById(R.id.btn_next));
		this.btn_next.setOnClickListener(this);
        this.btn_back = ((ImageView) findViewById(R.id.btn_left));
        this.btn_back.setOnClickListener(this);
        this.btn_completedOrders = (FrameLayout) findViewById(R.id.btn_CompletedOrders);
        this.btn_completedOrders.setOnClickListener(this);
        this.btn_activeOrders = (FrameLayout) findViewById(R.id.btn_activeOrders);
        this.btn_activeOrders.setOnClickListener(this);
        this.btn_orderDetails = (FrameLayout) findViewById(R.id.btn_orderDetails);
        this.btn_orderDetails.setOnClickListener(this);
//        this.timeEstimatedText = (TextView) findViewById(R.id.timeEstimatedText);
		progressButton = (ProgressButton) findViewById(R.id.pin_progress_1);
		progressButton.setInnerSize(getResources().getDimensionPixelSize(R.dimen.progress2_inner_size));


        calledByNotify = getIntent().getBooleanExtra("notification",false);
        try {
            order_id = getIntent().getIntExtra("order_id", -1);
            preferenceHelper.removeNotification(getIntent().getStringExtra("notify_id"));
        } catch (Exception e) { }
        try {
            customer_id = getIntent().getIntExtra("customer_id", -1);
        } catch (Exception e) { }

//        getOrdersCount();
        Common.getOrdersCount(preferenceHelper.getUserInfo().id, this);
        globals = new Globals();
        if(order_id>0)
            globals.setProgressTimer(order_id,order_id+"",getIntent().getIntExtra("estimate_time", 0),getIntent().getStringExtra("start_time"),this);
//        getOrder();
//        timeEstimatedText.setText("Estimated drive time is " + getIntent().getIntExtra("estimate_time", 0) + " minutes");

		progressButton.setProgress(currentProgress);
		report = (ImageView)findViewById(R.id.btn_report);
		report.setOnClickListener(this);


        btnRecoder = (ImageView) findViewById(R.id.btn_recorder);

        setUpRecord();
        registerBroadcast();

        if (order_id ==0){
            Intent i = new Intent(this,ActiveOrdersActivity.class);
            i.putExtra("order_id",order_id);
            startActivity(i);
            finish();
        }

        setUpMapIfNeeded();
	}

    @Override
    public void onNewIntent(Intent intent){
        Log.e("edwin", "enter new intent!!!!!");
        orderExist = true;
        destination = intent.getStringExtra("address");
        destiantionTxtView.setText(destination);
//        destiantionTxtView.setText(intent.getStringExtra("address"));
        try {
            order_id = intent.getIntExtra("order_id", -1);


        } catch (Exception e) { }
        try {
            customer_id = intent.getIntExtra("customer_id", -1);
        } catch (Exception e) { }
        calledByNotify = intent.getBooleanExtra("notification",false);
//        getOrdersCount();
        Common.getOrdersCount(preferenceHelper.getUserInfo().id, this);
        globals = new Globals();
        Log.e("edwin", Common.ordersCount+" Orders");
        if(order_id>0)
            globals.setProgressTimer(order_id,order_id+"",intent.getIntExtra("estimate_time", 0),intent.getStringExtra("start_time"),this);
        getOrder();
//        timeEstimatedText.setText("Estimated drive time is " + intent.getIntExtra("estimate_time", 0) + " minutes");
        Log.e("edwin", "Intent "+order_id+" order");
        if (order_id ==0){
            Intent i = new Intent(this,ActiveOrdersActivity.class);
            i.putExtra("order_id",order_id);
            startActivity(i);
            finish();
        }
        progressButton.setProgress(currentProgress);

        report = (ImageView)findViewById(R.id.btn_report);
        report.setOnClickListener(this);
        setUpMapIfNeeded();
//        mLocationClient.connect();
        btnRecoder = (ImageView) findViewById(R.id.btn_recorder);

        setUpRecord();
        registerBroadcast();

    }

	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

            if (intent.getIntExtra("order_id",-1) != order_id){
                return;
            }

            if(action.equals(REQUEST_CONFIRM)) {
                Common.turnOnScreen(DriverConfirmActivity.this);
            } else if(action.equals(ACCEPT_CONFIRM)) {
                if(confirmOrderTask!=null)
                    confirmOrderTask.cancel();
                hiddenDialog();
                Common.turnOnScreen(DriverConfirmActivity.this);
				showToast("Request confirm has accepted");
                orderExist = false;
				if(Common.ordersCount<=0)
                    finish();
				try {
                    Intent i = new Intent(getApplicationContext(), OrderCompleteActivity.class);
                    i.putExtra("orderId",order_id);
					startActivity(i);
				} catch (Exception e) { }
			} else if(action.equals(DENY_CONFIRM)) {
                if(confirmOrderTask!=null)
                    confirmOrderTask.cancel();
                Common.turnOnScreen(DriverConfirmActivity.this);
				hiddenDialog();
				showToast("Request confirm has deny");
			} else if(action.equals(CANCELLED_ORDER)){

                hiddenDialog();
                Common.turnOnScreen(DriverConfirmActivity.this);
                orderExist = false;
//                showToast("Order cancelled by the customer");
                if(Common.ordersCount<=0)
                    finish();
                try {
                    Intent i = new Intent(getApplicationContext(), OrderCancelledActivity.class);
                    i.putExtra("orderId",order_id);
                    startActivity(i);
                } catch (Exception e) { }
            }
		}
	};
	
	
	public final static String REQUEST_CONFIRM = "request_confirm";
	public final static String ACCEPT_CONFIRM = "accept_confirm";
	public final static String DENY_CONFIRM = "deny_confirm";
    public final static String CANCELLED_ORDER = "cancelled_order";
	
	void registerBroadcast() {
		IntentFilter intent = new IntentFilter();
		//intent.addAction(REQUEST_CONFIRM);
		intent.addAction(ACCEPT_CONFIRM);
		intent.addAction(DENY_CONFIRM);
        intent.addAction(CANCELLED_ORDER);
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
		/*ya 
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

    final HttpBasicClientHelper clientMaps= new HttpBasicClientHelper(new MyJsonAsyncCallback(){

        @Override
        public void onSuccess(HttpResponse httpResponse, JSONObject re) {
            super.onSuccess(httpResponse, re);
            try {
                if (!re.getString("status").equals("OK")) {
                    Log.e("GoogleMaps", re.getString("status"));
                    return;
                }
                final String points = re.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                Log.d("RoutePoints", points);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawRoute(PolyUtil.decode(points));
                    }
                });

            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Exception e) {
            super.onFailure(e);
        }
    });

	@Override
	protected void onResume() {
		super.onResume();
        Log.e("edwin", "On Resume!!!!! " + destination);

        if(!checkLocationServices())
            return;

        showMyDialog("Checking pending orders");
		setUpLocationClientIfNeeded();
//		mLocationClient.connect();
        mLocationClient.startConnection(new DriverMap(), true);
		StatusActivity.CURRENT_STATUS = StatusActivity.STATUS_ORERCONFIRM;
        Common.getOrdersCount(preferenceHelper.getUserInfo().id, this);

        showMyLocation();

        new Thread(new Runnable() {

            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(DriverConfirmActivity.this, Locale.getDefault());
                try {
                    String search = destination;
                    Log.e("Edwin", "Search -> " + search);
                    List<Address> addresses = geocoder.getFromLocationName(search, 1);
                    final Address a;
                    if (addresses.size() > 0)
                        a = addresses.get(0);
                    else
                    {
                        a = new Address(Locale.getDefault());
                        a.setLongitude(0);
                        a.setLatitude(0);
                    }

                    mDestinationLocation = new LatLng(a.getLatitude(), a.getLongitude());

                    if(mCurrentLocation == null)
                        return;

                    Log.i("Positions", "source = " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude() +
                            " destination = " +a.getLatitude() + ", " + a.getLongitude());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if(destinationMarker==null)
                                destinationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(a.getLatitude(), a.getLongitude())).draggable(true)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            else
                                destinationMarker.setPosition(new LatLng(a.getLatitude(), a.getLongitude()));

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(a.getLatitude(), a.getLongitude()), 16.0f)/*currentMarker.getPosition()*/);
                        }
                    });
                }catch(IOException e){ }
            }
        }).start();
	}

	@Override
	public void onPause() {

        globals.stopProgress(String.valueOf(order_id));
		if (mLocationClient != null) {
//			mLocationClient.disconnect();
            mLocationClient.stopConnection();
		}
        super.onPause();
	}

    private boolean checkLocationServices(){
        LocationHelper.findLocationManager(this);
        if(!LocationHelper.isLocationServiceEnabled()){
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Please enable Location Services");
            dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
//                    finish();
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setCancelable(false);
            dialog.show();
            return false;
        }
        return true;
    }

	Location mCurrentLocation;
    LatLng mDestinationLocation;
	Handler mHandler = new Handler();

	public void showMyLocation() {
        LocationHelper.findLocationManager(this);
		if (mLocationClient != null && /*mLocationClient.isConnected() &&*/ LocationHelper.isLocationServiceEnabled()) {
			mCurrentLocation = mLocationClient.getLastLocation();
			if (mCurrentLocation == null) {
				Toast.makeText(this, "Waiting for location...",Toast.LENGTH_SHORT).show();
				return;
            }

            if(currentMarker==null)
                currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).draggable(true)
                        .title("Current Location"));
            else
                currentMarker.setPosition(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));

            if(mDestinationLocation!=null) {
                if(destinationMarker==null)
                    destinationMarker = mMap.addMarker(new MarkerOptions().position(mDestinationLocation).draggable(true)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                else
                    destinationMarker.setPosition(mDestinationLocation);
            }
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
    Polyline line;
    public void drawRoute(List<LatLng> route){

        if(line == null) {
            for (int z = 0; z < route.size() - 1; z++) {
                LatLng src = route.get(z);
                LatLng dest = route.get(z + 1);
                line = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                        .width(2)
                        .color(Color.BLUE).geodesic(true));

            }
        }
        else {
            line.setPoints(route);
        }
    }

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
            mLocationClient = LocationHelper.getInstance();
		}
	}

	public void onMarkerDrag(Marker paramMarker) {
	}

	public void onMarkerDragEnd(Marker paramMarker) {
		final LatLng localLatLng = paramMarker.getPosition();
		mCurrentLocation.setLatitude(localLatLng.latitude);
		mCurrentLocation.setLongitude(localLatLng.longitude);
		paramMarker.setTitle("Current location");
	}
	
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
        unRegisterBroadcast();
        Common.hideNotification(this);
		super.onDestroy();

	}

	public void onMarkerDragStart(Marker paramMarker) {
	}

	@Override
	public boolean onMyLocationButtonClick() {
		// TODO Auto-generated method stub
		return false;
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

    @Override
    public void onBackPressed(){

        hiddenDialog();
        Log.e("edwin", "Enter back button <<<<<<<<<<");
        if(Common.ordersCount>0)
            return;
        Log.e("edwin", "Enter superback button <<<<<<<<<");
        super.onBackPressed();
    }

    @Override
    public void updateOrdersTextView(final String count){

        Common.ordersCount = Integer.parseInt(count);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hiddenDialog();
                Log.i("Edwin","Interface method call");
                if ((Integer.parseInt(count) >= 2 && calledByNotify) || (!orderExist && Integer.parseInt(count) >= 1)){
                    calledByNotify = false;
                    startActivity(new Intent(DriverConfirmActivity.this,ActiveOrdersActivity.class));
                }
                orderCountTextView.setText(count);
                onBackPressed();
            }
        });
    }

    String count = "";
    public void getOrdersCount(){
        HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){
            @Override
            public void onSuccess(HttpResponse httpResponse, JSONObject re) {
                try{
                    count = re.getJSONObject("data").getString("count");
                }catch (Exception e){
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        orderCountTextView.setText(count);
                    }
                });

            }

            @Override
            public void onFailure(Exception e) {
                // TODO Auto-generated method stub
                super.onFailure(e);
            }
        });

        client.getTotalOrdersByDriverID(preferenceHelper.getUserInfo().id);

    }

    String jsoData = "{}";
    void getOrder() {
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
                        final JSONObject data = re.getJSONObject("data");
                        if(data!=null && data instanceof JSONObject) {
                            jsoData = data.toString();
                            preferenceHelper.setValue("order_detail", jsoData);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        client.getOrder(order_id, preferenceHelper.getUserInfo().id);
    }

    @Override
    public void setProgressText(final String minutes,final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt_min.setText(minutes);
                unit.setText(text);
            }
        });
    }

    @Override
    public void setProgress(final int currentProgress,final int maxProgress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressButton.setProgressAndMax(currentProgress,maxProgress);
            }
        });
    }

    @Override
    public void endProgress() {

    }



    private class DriverMap extends LocationHelperCallback{

        @Override
        public void connected() {
            showMyLocation();
        }

        @Override
        public void locationChanged(Location location) {
            super.locationChanged(location);

            if(mCurrentLocation != null)
                clientMaps.getDirections(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), mDestinationLocation);
            showMyLocation();
        }
    }
}
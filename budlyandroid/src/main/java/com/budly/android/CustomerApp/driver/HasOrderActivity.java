package com.budly.android.CustomerApp.driver;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.android.CustomerApp.Common;
import com.budly.android.CustomerApp.ImageViewActivity;
import com.budly.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.LocationHelper;
import com.budly.android.CustomerApp.td.utils.LocationHelperCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.budly.android.CustomerApp.td.widget.AspectRatioImageView;
import com.turbomanage.httpclient.HttpResponse;

public class HasOrderActivity extends BaseActivity implements OnClickListener{
	
	ImageView btn_accept, btn_deny, image;
	AspectRatioImageView license;
	ListView listView;
	MyAdapter adapter;
	TextView txt_status,txt_name, txt_info, txt_supplier_name, txt_total_price, txt_time_info;
	// Khai bao options display image
	DisplayImageOptions options = new DisplayImageOptions.Builder()
		.cacheInMemory(false).cacheOnDisc(true).build();
	
	int customer_id = -1;
	int order_id = -1;
	
	PreferenceHelper preferenceHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


        Common.turnOnScreen(this);
        Common.setScreenFlag(this);

		setContentView(R.layout.driver_activity_has_order);
		preferenceHelper = PreferenceHelper.getInstance();
		txt_status = (TextView) findViewById(R.id.txt_status);
        txt_name = (TextView) findViewById(R.id.nameView);
		txt_info = (TextView) findViewById(R.id.txt_info);
		txt_supplier_name = (TextView) findViewById(R.id.txt_supplier_name);
		txt_total_price = (TextView) findViewById(R.id.txt_total_price);
		txt_time_info = (TextView) findViewById(R.id.txt_time_info);

		btn_accept = (ImageView) findViewById(R.id.btn_accept);
		btn_deny = (ImageView) findViewById(R.id.btn_deny);
		image = (ImageView) findViewById(R.id.image);
        image.setOnClickListener(this);
//		license = (AspectRatioImageView) findViewById(R.id.license);
//		license.setOnClickListener(this);
		btn_accept.setOnClickListener(this);
		btn_accept.setEnabled(false);
		btn_deny.setOnClickListener(this);
		listView = (ListView) findViewById(R.id.list);

		customer_id = getIntent().getIntExtra("customer_id", -1);
		order_id = getIntent().getIntExtra("order_id", -1);

        Log.i("Edwin","Create "+ SystemClock.uptimeMillis());
		getOrder();
		updateLocation();
		adapter = new MyAdapter();
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();

        Log.d("Edwin", "Empieza contador");
        timer = new Timer();
        timer.schedule(timeOut, 60000);

        registerBroadcast();

        showMyDialog("Getting info");

//		mHandler.removeCallbacks(timeOut);
//		mHandler.postDelayed(timeOut, 1000);
	}

    ProgressDialog progressDialog;
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
    Runnable rHiddenDlg = new Runnable() {

        @Override
        public void run() {
            hiddenDialog();
        }
    };

    void showMyDialog(final String msg) {
        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if(progressDialog!=null && progressDialog.isShowing()) {
                            progressDialog.setMessage(msg);
                        } else {
                            progressDialog = ProgressDialog.show(HasOrderActivity.this, "", msg);
                        }
                    } catch (Exception e) { }
                    mHandler.removeCallbacks(rHiddenDlg);
                    mHandler.postDelayed(rHiddenDlg, 120000);
                }
            });
        } catch (Exception e) { e.printStackTrace(); }

    }

	Timer timer;
	Handler mHandler = new Handler();
	
	TimerTask timeOut = new TimerTask() {
		
		@Override
		public void run() {
			int dem = 0;

            Log.i("Edwin", "TomeOut "+ SystemClock.uptimeMillis());
			try {
				dem = Integer.parseInt(preferenceHelper.getValue("missing_order"));
			} catch (Exception e) { }
			
			try {
				preferenceHelper.setValue("missing_order", String.valueOf(dem+1));
			} catch (Exception e) { }
			
			finish();
		}
	};
	
	ArrayList<OrderDetail> mList = new ArrayList<HasOrderActivity.OrderDetail>();
	
	class OrderDetail{
		public String name;
		public String size;
		public int amount;
		public double price;
	}
	
	int dem_retry = 0;
	String jsoData = "{}", license_path="", recomendation_path="";
	void getOrder() {
		final HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onFailure(Exception error) {
				if(dem_retry>3) {
					try {
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Toast.makeText(getApplicationContext(), "Have error while get order information.", Toast.LENGTH_SHORT).show();
							}
						});
					} catch (Exception e) {
						// TODO: handle exception
					}
					return;
				}
				dem_retry++;
				getOrder();
			}

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				Log.i("Tuan", re.toString());
				try {
					int status = re.getInt("status");
					if(status==200) {
						mHandler.removeCallbacks(timeOut);
						mHandler.postDelayed(timeOut, 60000);
						final JSONObject data = re.getJSONObject("data");
						if(data!=null && data instanceof JSONObject) {
							jsoData = data.toString();
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									btn_accept.setEnabled(true);
									double total_price = 0;
									try {
										JSONArray details = data.getJSONArray("order_detail");
										for (int i = 0; i < details.length(); i++) {
											JSONObject jso = details.getJSONObject(i);
											OrderDetail od = new OrderDetail();
											od.amount = jso.getInt("amount");
											od.name = jso.getString("name");
											try {
												od.price = jso.getDouble("price");
											} catch (Exception e) {

                                                Common.saveException(e);
                                            }
											total_price+=(od.price*od.amount);
											od.size = jso.getString("size");
											mList.add(od);
										}
									} catch (Exception e) {
                                        Common.saveException(e);
										e.printStackTrace();
									}
									adapter.notifyDataSetChanged();
									txt_total_price.setText("Total $"+total_price);
									JSONObject customer = null;
									try {
										try {
											txt_time_info.setText(data.getString("order_time_system"));
										} catch (Exception e) {
                                            Common.saveException(e);
                                        }

                                        customer = data.getJSONObject("customer");
										int order_id = data.getInt("order_id");
                                        customer.put("order_id", data.getInt("order_id"));
//                                        PreferenceHelper.getInstance().setValue("customer-"+order_id, customer.toString());

										String fullname = customer.getString("first_name")+" "+customer.getString("last_name");
										String address = customer.getString("address");
										int verified = customer.getInt("verified");
										try {
											Common.MOBILE_PHONE = customer.getString("phone_number");
										} catch (Exception e) {
                                            Common.saveException(e);
                                        }
										if(verified==1) {
											txt_status.setText("VERIFIED");
											txt_status.setTextColor(Color.GREEN);
										} else {
											txt_status.setText("NOT VERIFIED");
											txt_status.setTextColor(Color.parseColor("#a13644"));
										}
										txt_info.setText(address);
                                        txt_name.setText(fullname);
									} catch (Exception e) {
                                        Common.saveException(e);
										e.printStackTrace();
									}
									try {
										String ip = customer.getString("image_license");
										ImageLoader.getInstance().displayImage(ip, image, options);
									} catch (Exception e) {
                                        Common.saveException(e);
										e.printStackTrace();
									}
									
									try {
                                        recomendation_path = customer.getString("image_recomendation");
										license_path = customer.getString("image_license");
//										ImageLoader.getInstance().loadImage(license_path, options, new ImageLoadingListener() {
//
//											@Override
//											public void onLoadingStarted(String imageUri, View view) {
//												// TODO Auto-generated method stub
//
//											}
//
//											@Override
//											public void onLoadingFailed(String imageUri, View view,
//													FailReason failReason) {
//												// TODO Auto-generated method stub
//
//											}
//
//											@Override
//											public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
//												// TODO Auto-generated method stub
//												license.post(new Runnable() {
//
//													@Override
//													public void run() {
//														license.setImageBitmap(loadedImage);
//													}
//												});
//											}
//
//											@Override
//											public void onLoadingCancelled(String imageUri, View view) {
//												// TODO Auto-generated method stub
//
//											}
//										});
									} catch (Exception e) {
                                        Common.saveException(e);
										e.printStackTrace();
									}
									
									try {
										JSONObject supplier = data.getJSONObject("supplier");
										txt_supplier_name.setText("Shop: "+supplier.getString("name"));
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
						}
					}
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                           hiddenDialog();
                        }});

				} catch (Exception e) {
					e.printStackTrace();
                    Common.saveException(e);
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            hiddenDialog();
                        }});
				}
			}
			
		});
		client.getOrder(order_id, preferenceHelper.getUserInfo().id);
	}

    public void updateLocation(){
        final HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

            @Override
            public void onFailure(Exception error) {
                if(dem_retry>3) {
                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Have error while get order information.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    return;
                }
                dem_retry++;
                getOrder();
            }

            @Override
            public void onSuccess(HttpResponse httpResponse, JSONObject re) {
                Log.i("Tuan", re.toString());
                try {
                    int status = re.getInt("status");
                    if(status==200) {
                        getOrder();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        LocationHelper.getInstance().startConnection(new LocationHelperCallback() {
            @Override
            public void connected() {
                client.on_start(preferenceHelper.getUserInfo().id, preferenceHelper.getLocation().latitude, preferenceHelper.getLocation().longitude);
            }
        });

    }

	@Override
	public void onClick(View v) {
        timer.cancel();
		switch (v.getId()) {
		case R.id.btn_left:
			//startActivity(new Intent(PlaceOrderActivity.this, ProfileUpdateActivity.class));
			break;
		case R.id.btn_accept:
			mHandler.removeCallbacks(timeOut);
			finish();
			Intent i = new Intent(HasOrderActivity.this, SubmitOrderActivity.class);
			i.putExtra("data", jsoData);
			i.putExtra("order_id", order_id);
			i.putExtra("customer_id", customer_id);
            i.putExtra("notify_id",getIntent().getStringExtra("notify_id"));
			startActivity(i);
			//Toast.makeText(this, "under construction", Toast.LENGTH_LONG).show();
			break;
		case R.id.btn_deny:
			mHandler.removeCallbacks(timeOut);
            preferenceHelper.removeNotification(getIntent().getStringExtra("notify_id"));
			finish();
			break;
			
//		case R.id.license:
//			if(license_path==null || license_path.equals("")) return;
//			Intent ii = new Intent(this, ImageViewActivity.class);
//			ii.putExtra("license", license_path);
//			startActivity(ii);
//			break;

        case R.id.image:
//            if(license_path==null || license_path.equals("")) return;
            Intent ii = new Intent(this, ImageViewActivity.class);
            ii.putExtra("license", license_path);
            ii.putExtra("recomendation", recomendation_path);


            startActivity(ii);
            break;
		default:
			break;
		}
	}
	
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
				row = inflater.inflate(R.layout.driver_order_result_item, parent, false);
				holder = new MyHolder();
				holder.txt_name = (TextView) row.findViewById(R.id.txt_name);
				holder.txt_amount = (TextView) row.findViewById(R.id.txt_amount);
				holder.txt_price = (TextView) row.findViewById(R.id.txt_price);
				holder.layout = (LinearLayout) row.findViewById(R.id.item_layout);
				row.setTag(holder);
			} else {
				holder = (MyHolder) row.getTag();
			}

			final OrderDetail item = mList.get(position);
			
			holder.txt_name.setText(item.name);
			holder.txt_amount.setText(item.size+" x "+item.amount);
			holder.txt_price.setText("$"+(item.amount*item.price));
			
			if(position%2==0) {
				holder.layout.setBackgroundColor(Color.parseColor("#aaffffff"));
			} else {
				holder.layout.setBackgroundColor(Color.parseColor("#aaf1f0ec"));
			}
			
			return row;
		}
	}
	static class MyHolder {
		LinearLayout layout;
		TextView txt_name, txt_amount, txt_price;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        Log.i("Edwin", "on Resume "+ SystemClock.uptimeMillis());
		StatusActivity.CURRENT_STATUS = StatusActivity.STATUS_ORDERHAS;


	}
	
	@Override
	protected void onDestroy() {
		mHandler.removeCallbacks(timeOut);
        Common.hideNotification(this);
        unRegisterBroadcast();
		super.onDestroy();


	}


    public final static String PROCESSED_ORDER = "processed_order";

    void registerBroadcast() {
        IntentFilter intent = new IntentFilter();
        intent.addAction(PROCESSED_ORDER);
        registerReceiver(mReceiver, intent);
    }

    void unRegisterBroadcast() {

        unregisterReceiver(mReceiver);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(PROCESSED_ORDER)) {
                if(intent.getIntExtra("order_id",0) == order_id) {
                    preferenceHelper.removeNotification(getIntent().getStringExtra("notify_id"));
                    finish();
                    startActivity(new Intent(HasOrderActivity.this, OrderLostActivity.class));

                }
            }
        }
    };

}

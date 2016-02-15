package com.budly.android.CustomerApp.driver;

import java.util.ArrayList;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.android.CustomerApp.Common;
import com.budly.android.CustomerApp.ImageViewActivity;
import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

public class SubmitOrderActivity extends BaseActivity implements OnClickListener, SeekBar.OnSeekBarChangeListener, TextWatcher {
	
	PreferenceHelper preferenceHelper;
	User mUser;
	TextView txt_total_price;
	TextView txt_status,txt_name, txt_info, txt_processing, txt_supplier_name, txt_time_info;
	ImageView image;
	Button btn_next;
	EditText txt_min;
	ListView listView;
	MyAdapter adapter;
    SeekBar seekbar;
	ArrayList<OrderDetail> mList = new ArrayList<OrderDetail>();
    String licence, recomendation;
	
	// Khai bao options display image
	DisplayImageOptions options = new DisplayImageOptions.Builder()
		.cacheInMemory(false).cacheOnDisc(true).build();
	
	JSONObject data = null;
	int customer_id = -1;
	int order_id = -1;
	
	Runnable lost_order = new Runnable() {
		
		@Override
		public void run() {
			finish();
		}
	};
	
	public static String dataString = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Common.setScreenFlag(this);

		StatusActivity.CURRENT_STATUS = StatusActivity.STATUS_ORDERSUBMIT;
		preferenceHelper = PreferenceHelper.getInstance();

		mUser = preferenceHelper.getUserInfo();
		setContentView(R.layout.driver_activity_submit_order);
		txt_status = (TextView) findViewById(R.id.txt_status);
        txt_name = (TextView) findViewById(R.id.nameView);
		txt_info = (TextView) findViewById(R.id.txt_info);
		image = (ImageView) findViewById(R.id.image);
        image.setOnClickListener(this);
		txt_total_price = (TextView) findViewById(R.id.txt_total_price);
		txt_processing = (TextView) findViewById(R.id.txt_processing);
		txt_supplier_name = (TextView) findViewById(R.id.txt_supplier_name);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_next.setOnClickListener(this);
		listView = (ListView) findViewById(R.id.list);
        //listView.setOnClickListener(this);
		txt_min = (EditText) findViewById(R.id.txt_min);
        txt_min.addTextChangedListener(this);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setMax(12);
        seekbar.setOnSeekBarChangeListener(this);
        txt_time_info = (TextView) findViewById(R.id.txt_time_info);

		customer_id = getIntent().getIntExtra("customer_id", -1);
		order_id = getIntent().getIntExtra("order_id", -1);
		
//		mHandler.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				if(StatusActivity.CURRENT_STATUS!=StatusActivity.STATUS_ORDERSUBMIT) return;
//				finish();
//				startActivity(new Intent(SubmitOrderActivity.this, OrderLostActivity.class));
//			}
//		}, 600000);
		
		preferenceHelper.setValue("order_detail", "");
		
		dataString = getIntent().getStringExtra("data");
		
		preferenceHelper.setValue("order_detail", dataString);
		
		try {
			data = new JSONObject(dataString);
            txt_time_info.setText(data.getString("order_time_system"));
			txt_supplier_name.setText("Shop: "+data.getJSONObject("supplier").getString("name"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(data!=null) {
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
					} catch (Exception e) {}
					total_price+=(od.price*od.amount);
					od.size = jso.getString("size");
					mList.add(od);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			adapter = new MyAdapter();
			listView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			txt_total_price.setText("Total $"+total_price);
			JSONObject customer = null;
			try {
				customer = data.getJSONObject("customer");
				int order_id = data.getInt("order_id");
				String address = customer.getString("address");
				String fullname = customer.getString("first_name")+" "+customer.getString("last_name");
				int verified = customer.getInt("verified");
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
				e.printStackTrace();
			}
			try {
                recomendation = customer.getString("image_recomendation");
				String ip = customer.getString("image_license");
                licence = ip;
				ImageLoader.getInstance().displayImage(ip, image, options);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		registerBroadcast();
	}
	
	Handler mHandler = new Handler();

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			SubmitOrderActivity.this.openOptionsMenu();
			break;
		case R.id.btn_next:
//			txt_processing.setVisibility(View.VISIBLE);
//			mHandler.postDelayed(new Runnable() {
//				
//				@Override
//				public void run() {
//					finish();
//					startActivity(new Intent(SubmitOrderActivity.this, OrderLostActivity.class));
//				}
//			}, 2000);
			estimateTime();
			break;
            case R.id.image:
                imageDialog();
            default:
			break;
		}
	}

    void imageDialog() {
        Intent ii = new Intent(this, ImageViewActivity.class);
        ii.putExtra("license", licence);
        ii.putExtra("recomendation", recomendation);
        startActivity(ii);
//        AlertDialog.Builder alert = new AlertDialog.Builder(this);
//
//        alert.setTitle("Document");
//        alert.setMessage("Enter your phone number");
//
//        alert.setView(image);
//        alert.setCancelable(true);
//
//        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                finish();
//            }
//        });
//
//        alert.show();
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
                            progressDialog = ProgressDialog.show(SubmitOrderActivity.this, "", msg);
                        }
                    } catch (Exception e) { }
                    mHandler.removeCallbacks(rHiddenDlg);
                    mHandler.postDelayed(rHiddenDlg, 120000);
                }
            });
        } catch (Exception e) { e.printStackTrace(); }

    }
	void estimateTime() {
		txt_processing.setVisibility(View.VISIBLE);
		txt_processing.setText("Processing...");
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        hiddenDialog();
                    }
                });
				Log.e("Tuan", re.toString());
				try {
					int status = re.getInt("status");
					if(status==200) {
                        preferenceHelper.removeNotification(getIntent().getStringExtra("notify_id"));
//						runOnUiThread(new Runnable() {
//
//							@Override
//							public void run() {
//								txt_processing.setText("WAIT FOR RESPONSE");
//							}
//						});
                        finish();
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						txt_min.setEnabled(true);
						btn_next.setEnabled(true);
						txt_processing.setVisibility(View.INVISIBLE);
						Toast.makeText(SubmitOrderActivity.this, "Has error while send request to server. Please try again.", Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onFailure(Exception e) {
				super.onFailure(e);
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
                        hiddenDialog();
						txt_min.setEnabled(true);
						btn_next.setEnabled(true);
						txt_processing.setVisibility(View.INVISIBLE);
						Toast.makeText(SubmitOrderActivity.this, "Cannot send request to server. Please try again.", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
		});
		int min = -1;
		
		try {
			min = Integer.parseInt(txt_min.getText().toString().trim().replace(" ", ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(min<=0) {
			Toast.makeText(this, "You must enter number is >= 0", Toast.LENGTH_SHORT).show();
			return;
		}

         showMyDialog("Sending estimate time");
		client.estimateTime(order_id, mUser.id, min);
		txt_min.setEnabled(false);
		btn_next.setEnabled(false);
		txt_processing.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.e("Tuan", "on resume");
		if(StatusActivity.CURRENT_STATUS != StatusActivity.STATUS_ORDERSUBMIT) {
			finish();
		}
	};

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        txt_min.setText(""+(i*5));
        Log.i("Edwin","Progress -> "+seekbar.getProgress());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    public final static String PROCESSED_ORDER = "processed_order";

    void registerBroadcast() {
        IntentFilter intent = new IntentFilter();
        intent.addAction(PROCESSED_ORDER);
        registerReceiver(mReceiver, intent);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unRegisterBroadcast();
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
                    startActivity(new Intent(SubmitOrderActivity.this, OrderLostActivity.class));
                }
            }
        }
    };

    @Override
    public void afterTextChanged(Editable editable) {
        try {
//            seekbar.setProgress(Integer.parseInt(editable.toString()) / 5);
        } catch(NumberFormatException e){}
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
				holder.layout.setBackgroundColor(Color.parseColor("#f1f0ec"));
			} else {
				holder.layout.setBackgroundColor(Color.parseColor("#00000000"));
			}
			
			return row;
		}
	}
	static class MyHolder {
		LinearLayout layout;
		TextView txt_name, txt_amount, txt_price;
	}
	
	class OrderDetail{
		public String name;
		public String size;
		public int amount;
		public double price;
	}
}

package com.budly.android.CustomerApp.driver;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;
import android.widget.Toast;

import com.budly.BaseActivity;
import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

public class SubmitOrderActivity extends BaseActivity implements OnClickListener {
	
	PreferenceHelper preferenceHelper;
	User mUser;
	TextView txt_total_price;
	TextView txt_status, txt_info, txt_processing, txt_supplier_name;
	ImageView image;
	Button btn_next;
	EditText txt_min;
	ListView listView;
	MyAdapter adapter;
	
	ArrayList<OrderDetail> mList = new ArrayList<OrderDetail>();
	
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
		StatusActivity.CURRENT_STATUS = StatusActivity.STATUS_ORDERSUBMIT;
		preferenceHelper = new PreferenceHelper(this);
		mUser = preferenceHelper.getUserInfo();
		setContentView(R.layout.driver_activity_submit_order);
		txt_status = (TextView) findViewById(R.id.txt_status);
		txt_info = (TextView) findViewById(R.id.txt_info);
		image = (ImageView) findViewById(R.id.image);
		txt_total_price = (TextView) findViewById(R.id.txt_total_price);
		txt_processing = (TextView) findViewById(R.id.txt_processing);
		txt_supplier_name = (TextView) findViewById(R.id.txt_supplier_name);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_next.setOnClickListener(this);
		listView = (ListView) findViewById(R.id.list);
		txt_min = (EditText) findViewById(R.id.txt_min);
		
		customer_id = getIntent().getIntExtra("customer_id", -1);
		order_id = getIntent().getIntExtra("order_id", -1);
		
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(StatusActivity.CURRENT_STATUS!=StatusActivity.STATUS_ORDERSUBMIT) return;
				finish();
				startActivity(new Intent(SubmitOrderActivity.this, OrderLostActivity.class));
			}
		}, 600000);
		
		preferenceHelper.setValue("order_detail", "");
		
		dataString = getIntent().getStringExtra("data");
		
		preferenceHelper.setValue("order_detail", dataString);
		
		try {
			data = new JSONObject(dataString);
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
				txt_info.setText(fullname+" - "+address);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				String ip = customer.getString("image");
				ImageLoader.getInstance().displayImage(ip, image, options);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
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
		default:
			break;
		}
	}
	
	void estimateTime() {
		txt_processing.setVisibility(View.VISIBLE);
		txt_processing.setText("Processing...");
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				Log.e("Tuan", re.toString());
				try {
					int status = re.getInt("status");
					if(status==200) {
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								txt_processing.setText("WAIT FOR RESPONSE");
							}
						});
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
			holder.txt_amount.setText(item.size+"x"+item.amount);
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

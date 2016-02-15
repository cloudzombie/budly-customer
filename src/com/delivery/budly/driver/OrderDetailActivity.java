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
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.budly.BaseActivity;
import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.budly.android.CustomerApp.td.widget.ExpandableHeightListView;

public class OrderDetailActivity extends BaseActivity implements OnClickListener {
	
	PreferenceHelper preferenceHelper;
	User mUser;
	TextView txt_total_price;
	ExpandableHeightListView listView;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferenceHelper = new PreferenceHelper(this);
		mUser = preferenceHelper.getUserInfo();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.driver_activity_order_detail);
		txt_total_price = (TextView) findViewById(R.id.txt_total_price);
		listView = (ExpandableHeightListView) findViewById(R.id.list);
		listView.setExpanded(true);
		setTitle("Order details");
		customer_id = getIntent().getIntExtra("customer_id", -1);
		order_id = getIntent().getIntExtra("order_id", -1);
		
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(StatusActivity.CURRENT_STATUS!=StatusActivity.STATUS_ORDERSUBMIT) return;
				finish();
				startActivity(new Intent(OrderDetailActivity.this, OrderLostActivity.class));
			}
		}, 600000);
		
		try {
			data = new JSONObject(preferenceHelper.getValue("order_detail"));
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
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				String ip = customer.getString("image");
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
			OrderDetailActivity.this.openOptionsMenu();
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
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.e("Tuan", "on resume");
		if(StatusActivity.CURRENT_STATUS != StatusActivity.STATUS_ORERCONFIRM) {
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

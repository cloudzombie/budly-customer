package com.budly.android.CustomerApp.user;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

public class ListDriverActivity extends BaseActivity implements OnClickListener{
	
	Button btn_next;
	ListView listView;
	MyAdapter adapter;
	
	JSONArray drivers=null;
	int order_id=-1, supplier_id;
	
	User mUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_activity_list_driver);
		listView = (ListView) findViewById(R.id.list);
		adapter = new MyAdapter();
		listView.setAdapter(adapter);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_next.setOnClickListener(this);
		
		PreferenceHelper pre = PreferenceHelper.getInstance();
		mUser = pre.getUserInfo();
		
		try {
			order_id = getIntent().getIntExtra("order_id", -1);
            supplier_id = getIntent().getIntExtra("supplier_id", -1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			drivers = new JSONArray(getIntent().getStringExtra("drivers"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		initData();
	}
	
	void initData() {
		if(drivers==null || drivers.length()==0) {
			finish();
			startActivity(new Intent(this, NoDriverActivity.class));
			return;
		}
		for (int i = 0; i < drivers.length(); i++) {
			try {
				JSONObject driver = drivers.getJSONObject(i);
				mList.add(new MyItem(driver.getString("first_name"), driver.getString("id"), driver.getString("on_time_rank")+"%", driver.getString("estimate_time")+" min"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
			finish();
			break;

		default:
			break;
		}
	}
	
	class MyItem {
		public String title;
		public String id;
		public String value_on_time_rank;
		public String value_delay_time;
		public MyItem() { }
		public MyItem(String title, String id, String on_time, String delay_time) {
			this.title = title;
			this.id = id;
			this.value_delay_time = delay_time;
			this.value_on_time_rank = on_time;
		}
	}
	ArrayList<MyItem> mList = new ArrayList<MyItem>();
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			View row = convertView;
			MyHolder holder = null;

			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.user_list_driver_item, parent, false);
				holder = new MyHolder();
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.value_on_time_rank = (TextView) row.findViewById(R.id.value_on_time_rank);
				holder.value_delay_time = (TextView) row.findViewById(R.id.value_delay_time);
				holder.delay_time = row.findViewById(R.id.delay_time);
				holder.on_time_rank = row.findViewById(R.id.on_time_rank);
				holder.layout = (RelativeLayout) row.findViewById(R.id.service_item_layout);
				holder.select = (Button) row.findViewById(R.id.btn_select);
				row.setTag(holder);
			} else {
				holder = (MyHolder) row.getTag();
			}
			
			if(position==0) {
				holder.delay_time.setVisibility(View.VISIBLE);
				holder.on_time_rank.setVisibility(View.VISIBLE);
			} else {
				holder.delay_time.setVisibility(View.INVISIBLE);
				holder.on_time_rank.setVisibility(View.INVISIBLE);
			}

			MyItem item = mList.get(position);
			holder.title.setText(item.title);
			holder.value_delay_time.setText(item.value_delay_time);
			holder.value_on_time_rank.setText(item.value_on_time_rank);
			
			if(position%2==0) {
				holder.layout.setBackgroundColor(Color.parseColor("#aaffffff"));
			} else {
				holder.layout.setBackgroundColor(Color.parseColor("#aaf1f0ec"));
			}
			
			holder.select.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try {
						selectDriver(drivers.getJSONObject(position));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			return row;
		}
		

	}
	static class MyHolder {
		RelativeLayout layout;
		TextView title;
		TextView value_on_time_rank, value_delay_time;
		Button select;
		View on_time_rank, delay_time;
	}
	
	void selectDriver(final JSONObject driver) {
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				try {
					int status = re.getInt("status");
					if(status==200) {
						finish();
						Intent intent = new Intent(ListDriverActivity.this, OrderConfirmedActivity.class);
						intent.putExtra("driver", driver.toString());
						intent.putExtra("order_id", order_id);
                        intent.putExtra("supplier_id", supplier_id);
						startActivity(intent);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(ListDriverActivity.this, "Have error while request to server. Please try again", Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onFailure(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(ListDriverActivity.this, "Have error while request to server. Please try again", Toast.LENGTH_SHORT).show();
					}
				});
				super.onFailure(e);
			}
			
		});
		try {
			client.selectDriver(order_id, driver.getInt("id"), mUser.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

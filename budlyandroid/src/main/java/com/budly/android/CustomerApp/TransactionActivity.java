package com.budly.android.CustomerApp;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

public class TransactionActivity extends Activity implements OnClickListener{
	
	ListView listView;
	MyAdapter adapter;
	PreferenceHelper preferenceHelper;
	User mUser;
	TextView title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferenceHelper = PreferenceHelper.getInstance();
		mUser = preferenceHelper.getUserInfo();
		setContentView(R.layout.main_activity_transaction);
		title = (TextView) findViewById(R.id.title);
		
		if(mUser.type.equals("customer")) {
			title.setText("Transactions");
		} else {
			title.setText("Deliveries");
		}
		
		listView = (ListView) findViewById(R.id.list);
		adapter = new MyAdapter();
		listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent =new Intent(TransactionActivity.this, TransactionDetailActivity.class);
                intent.putExtra("date_time", mList.get(i).date_time);
                intent.putExtra("supplier_name", mList.get(i).supplier_name);
                intent.putExtra("id", mList.get(i).id);
                intent.putExtra("status", mList.get(i).status);
                intent.putExtra("total_price", mList.get(i).total_price);
                startActivity(intent);
            }
        });
		getData();
	}
	
	Handler mHandler = new Handler();
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
		default:
			break;
		}
	}
	
	ArrayList<TransactionItem> mList = new ArrayList<TransactionItem>();
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
				row = inflater.inflate(R.layout.main_transaction_item, parent, false);
				holder = new MyHolder();
				holder.txt_time_1 = (TextView) row.findViewById(R.id.txt_time_1);
				holder.txt_order_id = (TextView) row.findViewById(R.id.txt_order_id);
				holder.txt_order_price = (TextView) row.findViewById(R.id.txt_order_price);
				holder.txt_supplier = (TextView) row.findViewById(R.id.txt_supplier);
                holder.txt_status = (TextView) row.findViewById(R.id.txt_status);
				holder.layout = (LinearLayout) row.findViewById(R.id.transaction_item_layout);
				row.setTag(holder);
			} else {
				holder = (MyHolder) row.getTag();
			}

			final TransactionItem item = mList.get(position);
			holder.txt_order_id.setText("Order #"+item.id);
			holder.txt_order_price.setText("$"+item.total_price);
			holder.txt_supplier.setText(item.supplier_name);
			holder.txt_time_1.setText(item.date_time);
            holder.txt_status.setText(item.status);
			if(position%2==0) {
				holder.layout.setBackgroundColor(Color.parseColor("#ddffffff"));
			} else {
				holder.layout.setBackgroundColor(Color.parseColor("#ddf1f0ec"));
			}
			
			return row;
		}
	}
	static class MyHolder {
		LinearLayout layout;
		TextView txt_time_1, txt_time_2, txt_order_id, txt_order_price, txt_supplier, txt_status;
	}
	
	class TransactionItem {
		public int id;
		public double total_price;
		public String date_time="";
		public String supplier_name;
        public String status;
	}
	
	HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

		@Override
		public void onFailure(Exception error) {
			// TODO Auto-generated method stub
			super.onFailure(error);
		}
		String titleStr="";
		@Override
		public void onSuccess(HttpResponse httpResponse, JSONObject re) {
			Log.i("Tuan", re.toString());
			try {
				int status = re.getInt("status");
				if(status==200) {
					JSONArray data = re.getJSONArray("data");
					for (int i = 0; i < data.length(); i++) {
						JSONObject jso = data.getJSONObject(i);
						TransactionItem ti = new TransactionItem();
						ti.id = jso.getInt("id");
						ti.total_price = jso.getDouble("total_price");
						try {
							ti.date_time = jso.getString("date_time");
                            if(ti.date_time.equals("null"))
                                ti.date_time = "-";
							else
								ti.date_time = Common.formatUTCToLocal(ti.date_time);
						} catch (Exception e) {
							e.printStackTrace();
						}
                        if(mUser.type.equals("customer"))
    						ti.supplier_name = jso.getString("supplier_name");
                        else
                            ti.supplier_name = jso.getString("customer_name");
                        ti.status = jso.getString("status").toUpperCase();
						mList.add(ti);
					}
					if(mList.size()==0) {
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								if(mUser.type.equals("driver")) {
									titleStr = "delivery";
								} else {
									titleStr = "transaction";
								}
								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										Toast.makeText(TransactionActivity.this, "You don't have "+titleStr, Toast.LENGTH_SHORT).show();
									}
								});
							}
						});
					}
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							adapter.notifyDataSetChanged();
						}
					});
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	});
    
    void getData() {
    	client.getTransactions(mUser.id, mUser.type);
    }
    
    int mPage = 0;
}

package com.budly.android.CustomerApp.user;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.BaseActivity;
import com.budly.R;
import com.budly.android.CustomerApp.beans.MenuItem;
import com.budly.android.CustomerApp.beans.MenuSupplier;
import com.budly.android.CustomerApp.beans.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

public class OrderActivity extends BaseActivity implements OnClickListener{
	
	Button btn_next, btn_add_more;
	ListView listView;
	ImageView btn_left, thumb;
	
	MyAdapter adapter;
	
	int mSupplierId, distance;
	String mSupplierName, mthumb;
	
	User mUser;
	PreferenceHelper preferenceHelper;
	
	TextView title, txt_mile;
	
	// Khai bao options display image
	DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.thumb1)
		.showImageForEmptyUri(R.drawable.thumb1)
		.showImageOnFail(R.drawable.thumb1)
		.displayer(new RoundedBitmapDisplayer(200))
		.cacheInMemory(false).cacheOnDisc(true).build();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_activity_order);
		preferenceHelper = new PreferenceHelper(this);
		mUser = preferenceHelper.getUserInfo();
		mSupplierId = getIntent().getIntExtra("supplier_id", -1);
		mSupplierName = getIntent().getStringExtra("supplier_name");
		mthumb = getIntent().getStringExtra("thumb");
		distance = getIntent().getIntExtra("distance", 0);
		thumb = (ImageView) findViewById(R.id.thumb);
		title = (TextView) findViewById(R.id.title);
		txt_mile = (TextView) findViewById(R.id.txt_mile);
		title.setText(mSupplierName);
		txt_mile.setText(distance+" Miles");
		
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_add_more = (Button) findViewById(R.id.btn_add_more);
		btn_add_more.setOnClickListener(this);
		btn_left = (ImageView) findViewById(R.id.btn_left);
		btn_next.setOnClickListener(this);
		btn_left.setOnClickListener(this);
		
		ImageLoader.getInstance().displayImage(mthumb, thumb, options);
		
		listView = (ListView) findViewById(R.id.list);
		adapter = new MyAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position>=0 && position<mList.size()) {
					Intent i = new Intent(OrderActivity.this, ItemOfMenuActivity.class);
					i.putExtra("position", position);
					i.putExtra("supplier_id", mSupplierId);
					i.putExtra("supplier_name", mSupplierName);
					i.putExtra("distance", distance);
					startActivity(i);
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mList.clear();
		for (int i = 0; i < MenuOfServiceActivity.mList.size(); i++) {
			if(MenuOfServiceActivity.mList.get(i).total>0) {
				mList.add(MenuOfServiceActivity.mList.get(i));
			}
		}
		try {
			adapter.notifyDataSetChanged();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static boolean IS_CHOOSE_AGAIN = false;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_add_more:
		case R.id.btn_left:
			finish();
			break;
			
		case R.id.btn_next:
			order();
			break;

		default:
			break;
		}
	}

	double total = 0;
	ArrayList<MenuSupplier> mList = new ArrayList<MenuSupplier>();
	JSONArray menu_items = new JSONArray();
	class MyAdapter extends BaseAdapter {

		@Override
		public void notifyDataSetChanged() {
			total = 0;
			for (int i = 0; i < mList.size(); i++) {
				total += mList.get(i).total;
				ArrayList<MenuItem> items = mList.get(i).items;
				for (int j = 0; j < items.size(); j++) {
					MenuItem mi = items.get(j);
					if(mi.quality>0) {
						try {
							JSONObject jo = new JSONObject();
							jo.put("item_id", mi.id);
							jo.put("amount", mi.quality);
							menu_items.put(jo);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			btn_next.setText(String.format("Complete Order: \t$%s", total));
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
				row = inflater.inflate(R.layout.user_menu_item, parent, false);
				holder = new MyHolder();
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.price = (TextView) row.findViewById(R.id.txt_price);
				holder.layout = (RelativeLayout) row.findViewById(R.id.menu_item_layout);
				row.setTag(holder);
			} else {
				holder = (MyHolder) row.getTag();
			}

			MenuSupplier item = mList.get(position);
			holder.title.setText(item.name);
			holder.price.setText("$"+mList.get(position).total);
			
			if(position%2==0) {
				holder.layout.setBackgroundColor(Color.parseColor("#aaffffff"));
			} else {
				holder.layout.setBackgroundColor(Color.parseColor("#aaf1f0ec"));
			}
			
			return row;
		}
	}
	
	void order() {
		if(total<=0) {
			Toast.makeText(this, "Your order don't have any item. Please select one", Toast.LENGTH_SHORT).show();
			return;
		}
		Log.e("Tuan2", "minimum is "+ListServiceInAreaActivity.minimum_per);
		if(total<ListServiceInAreaActivity.minimum_per) {
			Toast.makeText(this, "Minimum of an order is "+ListServiceInAreaActivity.minimum_per+"$. Please select again", Toast.LENGTH_SHORT).show();
			return;
		}
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onFailure(Exception error) {
				super.onFailure(error);
			}

			@Override
			public void onSuccess(HttpResponse httpResponse,JSONObject re) {
				Log.i("Tuan", re.toString());
				try {
					int status = re.getInt("status");
					if(status==200) {
						int order_id = re.getInt("data");
						if(order_id>0) {
							Intent intent = new Intent(OrderActivity.this, OrderProcessingActivity.class);
							intent.putExtra("order_id", order_id);
							intent.putExtra("supplier_id", mSupplierId);
							intent.putExtra("supplier_name", mSupplierName);
							intent.putExtra("total_price", total);
							startActivity(intent);
							IS_CHOOSE_AGAIN = true;
							finish();
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(OrderActivity.this, "Cannot send order to server", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
		});
		Log.i("Tuan",menu_items.toString()+"|"+mUser.id+"|"+mSupplierId+"|"+total);
		client.order(mUser.id, mSupplierId, total, menu_items.toString(), preferenceHelper.getValue("location_address"));
	}
	
	static class MyHolder {
		RelativeLayout layout;
		TextView title;
		TextView price;
	}
}


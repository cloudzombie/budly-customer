package com.budly.android.CustomerApp.user;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.budly.android.CustomerApp.beans.MenuSupplier;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class MenuOfServiceActivity extends BaseActivity implements OnClickListener{
	
	Button btn_next;
	ListView listView;
	ImageView btn_left, thumb;
	
	MyAdapter adapter;
	int mSupplierId, distance;
	String mSupplierName, mthumb;
	
	TextView title, txt_mile;
	
	DisplayImageOptions options = new DisplayImageOptions.Builder()
	.showStubImage(R.drawable.thumb1)
	.showImageForEmptyUri(R.drawable.thumb1)
	.showImageOnFail(R.drawable.thumb1)
	.displayer(new RoundedBitmapDisplayer(200))
	.cacheInMemory(false).cacheOnDisc(true).build();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mList.clear();
		setContentView(R.layout.user_activity_menu_of_service);
		mSupplierId = getIntent().getIntExtra("supplier_id", -1);
		mSupplierName = getIntent().getStringExtra("supplier_name");
		mthumb = getIntent().getStringExtra("thumb");
		distance = getIntent().getIntExtra("distance", 0);
		if(mSupplierId==-1) {
			Toast.makeText(this, "Supplier invalid. Please select again", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		thumb = (ImageView) findViewById(R.id.thumb);
		ImageLoader.getInstance().displayImage(mthumb, thumb, options);
		title = (TextView) findViewById(R.id.title);
		txt_mile = (TextView) findViewById(R.id.txt_mile);
		title.setText(mSupplierName);
		txt_mile.setText(distance+" Miles");
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_left = (ImageView) findViewById(R.id.btn_left);
		btn_next.setOnClickListener(this);
		btn_left.setOnClickListener(this);
		
		listView = (ListView) findViewById(R.id.list);
		adapter = new MyAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position>=0 && position<mList.size()) {
					Intent i = new Intent(MenuOfServiceActivity.this, ItemOfMenuActivity.class);
					i.putExtra("position", position);
					i.putExtra("supplier_id", mSupplierId);
					i.putExtra("supplier_name", mSupplierName);
					i.putExtra("distance", distance);
					i.putExtra("thumb", mthumb);
					startActivity(i);
				}
			}
		});
		
		getData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.btn_left:
			finish();
			break;
			
		case R.id.btn_next:
			Intent i = new Intent(this, OrderActivity.class);
			i.putExtra("supplier_id", mSupplierId);
			i.putExtra("supplier_name", mSupplierName);
			i.putExtra("thumb", mthumb);
			startActivity(i);	
			break;

		default:
			break;
		}
	}
	
	class MyItem {
		public String title;
		public String price;
		public boolean checked = false;
		public MyItem() { }
		public MyItem(String title, String price, boolean check) {
			this.title = title;
			this.price = price;
			this.checked = check;
		}
	}
	
	void getData() {
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
						JSONArray data = re.getJSONArray("data");
						for (int i = 0; i < data.length(); i++) {
							JSONObject jso = data.getJSONObject(i);
							MenuSupplier ms = MenuSupplier.parse(jso.toString());
							for (int j = 0; j < ms.items.size(); j++) {
								Log.i("Tuan", ""+ms.items.get(j).id);
							}
							if(ms!=null)
								mList.add(ms);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						adapter.notifyDataSetChanged();
					}
				});
			}
			
		});
		client.getMenu(mSupplierId);
	}
	
	public static ArrayList<MenuSupplier> mList = new ArrayList<MenuSupplier>();
	class MyAdapter extends BaseAdapter {

		@Override
		public void notifyDataSetChanged() {
			double total = 0;
			for (int i = 0; i < mList.size(); i++) {
				total += mList.get(i).total;
			}
			btn_next.setText(String.format("Confirm Total: \t$%s", total));
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
			holder.price.setText("$"+item.total);
			holder.title.setText(item.name);
			if(item.total>0) {
				holder.title.setTypeface(Typeface.DEFAULT_BOLD);
			} else {
				holder.title.setTypeface(Typeface.DEFAULT);
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
		TextView price;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(OrderActivity.IS_CHOOSE_AGAIN) {
			OrderActivity.IS_CHOOSE_AGAIN=false;
			finish();
		}
		try {
			adapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

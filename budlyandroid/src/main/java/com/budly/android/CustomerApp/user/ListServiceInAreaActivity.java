package com.budly.android.CustomerApp.user;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.android.CustomerApp.Common;
import com.budly.R;
import com.budly.android.CustomerApp.beans.Supplier;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.driver.StatusActivity;
import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

public class ListServiceInAreaActivity extends BaseActivity implements OnClickListener{
	
	ImageView btn_next;
	ListView listView;
	ImageView btn_minus, btn_plus;
	
	int valueMile = Common.DISTANCE_DEFAULT;
	
	TextView txt_value_mile;
	MyAdapter adapter;
	
	PreferenceHelper preferenceHelper;
	User mUser;
	LatLng mCurrentLocation;
	public static String supplier_name = "";
	public static String supplier_image = "";
	public static int supplier_id=0;
	public static int minimum_per=0;
	
	// Khai bao options display image
	DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.generic_logo)
		.showImageForEmptyUri(R.drawable.generic_logo)
		.showImageOnFail(R.drawable.generic_logo)
//		.displayer(new RoundedBitmapDisplayer(200))
		.cacheInMemory(false).cacheOnDisc(true).build();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		supplier_name = "";
		supplier_image = "";
		supplier_id=0;
		
		preferenceHelper = PreferenceHelper.getInstance();
		mUser = preferenceHelper.getUserInfo();
		mCurrentLocation = preferenceHelper.getLocation();
		if(mCurrentLocation==null) {
			Toast.makeText(this, "Current location not available", Toast.LENGTH_LONG).show();
			mCurrentLocation = new LatLng(0D, 0D);
		}
		setContentView(R.layout.user_activity_list_of_service_in_area);
		btn_minus = (ImageView) findViewById(R.id.btn_minus);
		btn_plus = (ImageView) findViewById(R.id.btn_plus);
		btn_minus.setOnClickListener(this);
		btn_plus.setOnClickListener(this);
		
		txt_value_mile = (TextView) findViewById(R.id.txt_value_mile);
		txt_value_mile.setText(String.valueOf(valueMile));
		
		listView = (ListView) findViewById(R.id.list);
		adapter = new MyAdapter();
		listView.setAdapter(adapter);
		getData(mPage);
	}
	
	Handler mHandler = new Handler();
	
	
	int step = 5;
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
			startActivity(new Intent(this, PlaceOrderActivity.class));
			break;
		case R.id.btn_minus:
			if(valueMile>step) {
				valueMile-=step;
				txt_value_mile.setText(String.valueOf(valueMile));
				mHandler.removeCallbacks(updateData);
				mHandler.postDelayed(updateData, 500);
			}
			break;
		case R.id.btn_plus:
			if(valueMile<100) {
				valueMile+=step;
				txt_value_mile.setText(String.valueOf(valueMile));
				mHandler.removeCallbacks(updateData);
				mHandler.postDelayed(updateData, 500);
			}
			break;

		default:
			break;
		}
	}
	
	ArrayList<Supplier> mList = new ArrayList<Supplier>();
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
				row = inflater.inflate(R.layout.user_service_item, parent, false);
				holder = new MyHolder();
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.txt_mile = (TextView) row.findViewById(R.id.txt_mile);
				holder.thumb = (ImageView) row.findViewById(R.id.thumb);
				holder.layout = (RelativeLayout) row.findViewById(R.id.service_item_layout);
				holder.select = (Button) row.findViewById(R.id.btn_select);
				row.setTag(holder);
			} else {
				holder = (MyHolder) row.getTag();
			}

			final Supplier item = mList.get(position);
			holder.title.setText(item.name);
			holder.txt_mile.setText(String.valueOf(item.distance)+" miles");
			//holder.thumb.setImageResource(R.drawable.thumb1);
			
			ImageLoader.getInstance().displayImage(item.image, holder.thumb, options);
			
			if(position%2==0) {
				holder.layout.setBackgroundColor(Color.parseColor("#aaffffff"));
			} else {
				holder.layout.setBackgroundColor(Color.parseColor("#aaf1f0ec"));
			}
			
			holder.select.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(ListServiceInAreaActivity.this, MenuOfServiceActivity.class);
					i.putExtra("supplier_id", item.id);
					i.putExtra("supplier_name", item.name);
					i.putExtra("distance", item.distance);
					i.putExtra("minimum_per", item.minimum_per);
					i.putExtra("thumb", item.image);
					supplier_id=item.id;
					supplier_name = item.name;
					supplier_image = item.image;
					minimum_per = item.minimum_per;
					startActivity(i);
				}
			});
			
			return row;
		}
	}
	static class MyHolder {
		RelativeLayout layout;
		TextView title;
		TextView txt_mile;
		ImageView thumb;
		Button select;
	}
	
	Runnable updateData = new Runnable() {
		
		@Override
		public void run() {
			Toast.makeText(ListServiceInAreaActivity.this, "Waiting for update...", Toast.LENGTH_SHORT).show();
			mPage=0;
			getData(mPage);
		}
	};
	
	HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

		@Override
		public void onFailure(Exception error) {
			// TODO Auto-generated method stub
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
						JSONObject ji = data.getJSONObject(i);


                        if (ji.getInt("total_drivers") <= 0){
                            continue;
                        }

						try {
							mList.add(Supplier.parse(ji));	
						} catch (Exception e) { }
					}
					if(mList.size()==0) {
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Toast.makeText(ListServiceInAreaActivity.this, "No service in area", Toast.LENGTH_SHORT).show();
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
    
    void getData(int page) { 
    	if(page==0) {
    		mList.clear();
    	}
    	client.getSuppliers(mCurrentLocation.latitude, mCurrentLocation.longitude, valueMile, page);
    }
    
    @Override
    protected void onResume() {
    	if(StatusActivity.CURRENT_STATUS==StatusActivity.STATUS_ORERCOMPLETE) {
    		StatusActivity.CURRENT_STATUS=StatusActivity.STATUS_ORDERWAIT;
    		finish();
    	}
    	super.onResume();
    }
    
    int mPage = 0;
}

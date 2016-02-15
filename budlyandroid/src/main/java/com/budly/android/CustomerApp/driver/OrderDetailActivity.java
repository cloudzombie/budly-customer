package com.budly.android.CustomerApp.driver;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.budly.android.CustomerApp.Common;
import com.budly.android.CustomerApp.ImageViewActivity;
import com.budly.R;
import com.budly.android.CustomerApp.TransactionActivity;
import com.budly.android.CustomerApp.beans.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.budly.android.CustomerApp.td.widget.ExpandableHeightListView;

public class OrderDetailActivity extends Activity implements View.OnClickListener, DriverInterface {
	
	PreferenceHelper preferenceHelper;
	User mUser;
	TextView txt_total_price;
    TextView txt_supplier;
    TextView orderCountTextView;
    ImageView img_id;
	ExpandableHeightListView listView;
	MyAdapter adapter;
    FrameLayout btn_completedOrders,btn_activeOrders,btn_orderDetails;
    ImageView btn_left;
	ArrayList<OrderDetail> mList = new ArrayList<OrderDetail>();
	String id_path;
    String recomendation_path;
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		preferenceHelper = PreferenceHelper.getInstance();
		mUser = preferenceHelper.getUserInfo();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.driver_activity_order_detail);

        this.btn_left = ((ImageView) findViewById(R.id.btn_left));
        this.btn_left.setOnClickListener(this);
        this.btn_completedOrders = (FrameLayout) findViewById(R.id.btn_CompletedOrders);
        this.btn_completedOrders.setOnClickListener(this);
        this.btn_activeOrders = (FrameLayout) findViewById(R.id.btn_activeOrders);
        this.btn_activeOrders.setOnClickListener(this);
        this.btn_orderDetails = (FrameLayout) findViewById(R.id.btn_orderDetails);
        this.btn_orderDetails.setOnClickListener(this);
        this.orderCountTextView = (TextView) findViewById(R.id.orderCounttextView);
        this.img_id = (ImageView) findViewById(R.id.img_id);
        this.img_id.setOnClickListener(this);

        Common.getOrdersCount(preferenceHelper.getUserInfo().id, this);

        txt_supplier = (TextView) findViewById(R.id.txt_supplier);
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
                JSONObject supplier = data.getJSONObject("supplier");
                txt_supplier.setText(supplier.getString("name"));
                id_path = data.getJSONObject("customer").getString("image_license");
                recomendation_path = data.getJSONObject("customer").getString("image_recomendation");
                ImageLoader.getInstance().displayImage(id_path, img_id);
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
//		case R.id.btn_left:
//			OrderDetailActivity.this.openOptionsMenu();
//			break;
            case R.id.btn_left:
                //StatusActivity.this.openOptionsMenu();
                final String[] time = { "Manage Suppliers", "Update profile", "Deliveries", "Status" };
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetailActivity.this);
                builder.setTitle("Budly");
                builder.setItems(time, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        if(position==0) {
                            startActivity(new Intent(OrderDetailActivity.this, ManagerSupplierActivity.class));
                        } else if(position==1){
                            startActivity(new Intent(OrderDetailActivity.this, ProfileUpdateActivity.class));
                        } else  if(position==2){
                            startActivity(new Intent(OrderDetailActivity.this, TransactionActivity.class));
                        }  else if(position==3){
                            startActivity(new Intent(OrderDetailActivity.this, StatusActivity.class));
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
        case R.id.btn_orderDetails:
            Log.e("Tuan2", "Click Order Details");
            startActivity(new Intent(this, OrderDetailActivity.class));
            finish();
            break;
        case R.id.btn_CompletedOrders:
            Log.e("Tuan2", "Click Completed Orders");
            startActivity(new Intent(this,CompletedOrdersActivity.class));
            finish();
            break;
        case R.id.btn_activeOrders:
            Log.e("Tuan2", "Click Active Orders");
            startActivity(new Intent(this,ActiveOrdersActivity.class));
            finish();
            break;
        case R.id.img_id:
            Log.e("Tuan2", "Click Image");
            Intent ii = new Intent(this, ImageViewActivity.class);
            ii.putExtra("license", id_path);
            ii.putExtra("recomendation", recomendation_path);
            startActivity(ii);
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

    @Override
    public void updateOrdersTextView(final String count){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                orderCountTextView.setText(count);
            }
        });
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

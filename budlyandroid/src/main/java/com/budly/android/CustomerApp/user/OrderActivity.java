package com.budly.android.CustomerApp.user;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.R;
import com.budly.android.CustomerApp.beans.MenuItem;
import com.budly.android.CustomerApp.beans.MenuSupplier;
import com.budly.android.CustomerApp.beans.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

public class OrderActivity extends BaseActivity implements OnClickListener{
	
	Button btn_next, btn_add_more;
	ListView listView;
//    ExpandableListView expListView;
	ImageView btn_left, thumb;
	
	MyAdapter adapter;
//    ExpandableListAdapter listAdapter;
	
	int mSupplierId, distance, minimum_per;
	String mSupplierName, mthumb;
	
	User mUser;
	PreferenceHelper preferenceHelper;
	
	TextView title, txt_mile, txt_minValue;
	
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
		setContentView(R.layout.user_activity_order);
		preferenceHelper = PreferenceHelper.getInstance();
		mUser = preferenceHelper.getUserInfo();
		mSupplierId = getIntent().getIntExtra("supplier_id", -1);
		mSupplierName = getIntent().getStringExtra("supplier_name");
		mthumb = getIntent().getStringExtra("thumb");
		distance = getIntent().getIntExtra("distance", 0);
        minimum_per = getIntent().getIntExtra("minimum_per", 0);
		thumb = (ImageView) findViewById(R.id.thumb);
		title = (TextView) findViewById(R.id.title);
		txt_mile = (TextView) findViewById(R.id.txt_mile);
		title.setText(mSupplierName);
		txt_mile.setText(distance+" Miles");
		txt_minValue = (TextView) findViewById(R.id.txt_minValue);
        txt_minValue.setText(txt_minValue.getText() + "$" + minimum_per);

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
//		listView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				if(position>=0 && position<mList.size()) {
//					Intent i = new Intent(OrderActivity.this, ItemOfMenuActivity.class);
//					i.putExtra("position", position);
//					i.putExtra("supplier_id", mSupplierId);
//					i.putExtra("supplier_name", mSupplierName);
//					i.putExtra("distance", distance);
//                    i.putExtra("child_position", position);
//                    i.putExtra("group_name", mList.get(position).category);
//					startActivity(i);
//				}
//			}
//		});
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mList.clear();

        for (List<MenuSupplier> menus: MenuOfServiceActivity.listDataChild.values())
        {
            for(MenuSupplier menu: menus){
                if(menu.total>0)
                    mList.add(menu);
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
    List<String> headers=new ArrayList<String>();
    HashMap<String, List<OrderItems>> checkoutlist=new HashMap<String, List<OrderItems>>();
    private class OrderItems{
        public int amount;
        public String name;
        public String size;
        public double price;
    }

	double total = 0;
	ArrayList<MenuSupplier> mList = new ArrayList<MenuSupplier>();
    ArrayList<OrderItems> orderitems =new ArrayList<OrderItems>();
	JSONArray menu_items = new JSONArray();
	class MyAdapter extends BaseAdapter {

		@Override
		public void notifyDataSetChanged() {
			total = 0;
            orderitems.clear();
            menu_items = new JSONArray();
			for (int i = 0; i < mList.size(); i++) {
				total += mList.get(i).total;
				ArrayList<MenuItem> items = mList.get(i).items;
				for (int j = 0; j < items.size(); j++) {
					MenuItem mi = items.get(j);
					if(mi.quality>0) {
                        OrderItems oi = new OrderItems();
						try {
							JSONObject jo = new JSONObject();
							jo.put("item_id", mi.id);
							jo.put("amount", mi.quality);
                            oi.amount = mi.quality;
                            oi.name = mList.get(i).name;
                            oi.size = mi.size;
                            oi.price = mi.price;
							menu_items.put(jo);
                            orderitems.add(oi);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			btn_next.setText(String.format("Complete Order: \t$%s", new DecimalFormat("0.00").format(total)));
			super.notifyDataSetChanged();
		};

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
//			return mList.size();
            return orderitems.size();
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
				holder.title = (TextView) row.findViewById(R.id.txt_name);
                holder.amount = (TextView) row.findViewById(R.id.txt_amount);
				holder.price = (TextView) row.findViewById(R.id.txt_price);
				holder.layout = (LinearLayout) row.findViewById(R.id.item_layout);
				row.setTag(holder);
			} else {
				holder = (MyHolder) row.getTag();
			}

//			MenuSupplier item = mList.get(position);
            try {
                holder.title.setText(orderitems.get(position).name+ " - " +
                        orderitems.get(position).size);
                holder.amount.setText(" x "+orderitems.get(position).amount);
                holder.price.setText("$" + new DecimalFormat("0.00").format(
                        orderitems.get(position).amount *
                        orderitems.get(position).price
                ));
            }catch (Exception e){}
			if(position%2==0) {
				holder.layout.setBackgroundColor(Color.parseColor("#aaffffff"));
			} else {
				holder.layout.setBackgroundColor(Color.parseColor("#aaf1f0ec"));
			}
			
			return row;
		}
	}

//    class ExpandableListAdapter extends BaseExpandableListAdapter {
//
//        private Context _context;
//        private List<String> _listDataHeader; // header titles
//        // child data in format of header title, child title
//        private HashMap<String, List<MenuSupplier>> _listDataChild;
//
//        public ExpandableListAdapter(Context context, List<String> listDataHeader,
//                                     HashMap<String, List<MenuSupplier>> listChildData) {
//            this._context = context;
//            this._listDataHeader = listDataHeader;
//            this._listDataChild = listChildData;
//        }
//
//        @Override
//        public int getGroupCount() {
//            return this._listDataHeader.size();
//        }
//
//        @Override
//        public int getChildrenCount(int groupPosition) {
//            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
//                    .size();
//        }
//
//        @Override
//        public Object getGroup(int groupPosition) {
//            return this._listDataHeader.get(groupPosition);
//        }
//
//        @Override
//        public Object getChild(int groupPosition, int childPosititon) {
//            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
//                    .get(childPosititon);
//        }
//
//        @Override
//        public long getGroupId(int groupPosition) {
//            return groupPosition;
//        }
//
//        @Override
//        public long getChildId(int i, int i2) {
//            return i2;
//        }
//
//        @Override
//        public boolean hasStableIds() {
//            return false;
//        }
//
//        @Override
//        public View getGroupView(int groupPosition, boolean isExpanded,
//                                 View convertView, ViewGroup parent) {
//            String headerTitle = (String) getGroup(groupPosition);
//            if (convertView == null) {
//                LayoutInflater infalInflater = (LayoutInflater) this._context
//                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                convertView = infalInflater.inflate(R.layout.user_menu_category, null);
//            }
//
//            TextView lblListHeader = (TextView) convertView
//                    .findViewById(R.id.category_text);
//            lblListHeader.setTypeface(null, Typeface.BOLD);
//            lblListHeader.setText(headerTitle);
//
//            return convertView;
//        }
//
//        @Override
//        public View getChildView(int groupPosition, final int childPosition,
//                                 boolean isLastChild, View convertView, ViewGroup parent) {
//
//            final MenuSupplier child = (MenuSupplier) getChild(groupPosition, childPosition);
//
//            if (convertView == null) {
//                LayoutInflater infalInflater = (LayoutInflater) this._context
//                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                convertView = infalInflater.inflate(R.layout.user_menu_item, null);
//            }
//
//            TextView titleListChild = (TextView) convertView.findViewById(R.id.title);
//            titleListChild.setText(child.name);
//            TextView priceListChild = (TextView) convertView.findViewById(R.id.txt_price);
//            priceListChild.setText("$"+new DecimalFormat("0.00").format(child.total));
//
//            return convertView;
//        }
//
//        @Override
//        public boolean isChildSelectable(int i, int i2) {
//            return true;
//        }
//
//        @Override
//        public void notifyDataSetChanged() {
//            double total=0;
//            for (List<MenuSupplier> menus: listDataChild.values())
//            {
//                for(MenuSupplier menu: menus){
//                    total+=menu.total;
//                }
//            }
//            btn_next.setText(String.format("Confirm Total: \t$%s", new DecimalFormat("0.00").format(total)));
//            super.notifyDataSetChanged();
//        }
//    }

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
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                       hiddenDialog();
                    }
                });
			}

			@Override
			public void onSuccess(HttpResponse httpResponse,JSONObject re) {
				Log.i("Tuan", re.toString());
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        hiddenDialog();
                    }
                });
				try {
					int status = re.getInt("status");
					if(status==200) {
						int order_id = re.getInt("data");
						if(order_id>0) {
                            Intent intent = new Intent(OrderActivity.this, WaitDriversActivity.class);
                            intent.putExtra("order_id", order_id);
							intent.putExtra("supplier_id", mSupplierId);
							intent.putExtra("supplier_name", mSupplierName);
                            intent.putExtra("distance", distance);
                            intent.putExtra("thumb", mthumb);
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
                        hiddenDialog();
						Toast.makeText(OrderActivity.this, "Cannot send order to server", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
		});
		Log.i("Tuan",menu_items.toString()+"|"+mUser.id+"|"+mSupplierId+"|"+total);
        showMyDialog("Sending order");
		client.order(mUser.id, mSupplierId, total, menu_items.toString(), preferenceHelper.getValue("location_address"),preferenceHelper.getLocation().latitude,preferenceHelper.getLocation().longitude);
	}

    ProgressDialog progressDialog;
    Handler mHandler = new Handler();
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
                            progressDialog = ProgressDialog.show(OrderActivity.this, "", msg);
                        }
                    } catch (Exception e) { }
                    mHandler.removeCallbacks(rHiddenDlg);
                    mHandler.postDelayed(rHiddenDlg, 120000);
                }
            });
        } catch (Exception e) { e.printStackTrace(); }

    }
	
	static class MyHolder {
		LinearLayout layout;
		TextView title;
        TextView amount;
		TextView price;
	}
}


package com.budly.android.CustomerApp.driver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.android.CustomerApp.Common;
import com.budly.R;
import com.budly.android.CustomerApp.TransactionActivity;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by EdwinSL on 1/23/2015.
 */
public class CompletedOrdersActivity extends Activity implements View.OnClickListener, DriverInterface {

    ListView listView;
    MyAdapter adapter;
    PreferenceHelper preferenceHelper;
    User mUser;
    FrameLayout btn_completedOrders,btn_activeOrders,btn_orderDetails;
    ImageView btn_left;
    TextView orderCountTextView;

    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        preferenceHelper = PreferenceHelper.getInstance();
        mUser = preferenceHelper.getUserInfo();
        setContentView(R.layout.driver_activity_completed_orders);

        this.btn_left = ((ImageView) findViewById(R.id.btn_left));
        this.btn_left.setOnClickListener(this);
        this.btn_completedOrders = (FrameLayout) findViewById(R.id.btn_CompletedOrders);
        this.btn_completedOrders.setOnClickListener(this);
        this.btn_activeOrders = (FrameLayout) findViewById(R.id.btn_activeOrders);
        this.btn_activeOrders.setOnClickListener(this);
        this.btn_orderDetails = (FrameLayout) findViewById(R.id.btn_orderDetails);
//        this.btn_orderDetails.setOnClickListener(this);

        this.orderCountTextView = (TextView) findViewById(R.id.orderCounttextView);

        Common.getOrdersCount(preferenceHelper.getUserInfo().id, this);



        listView = (ListView) findViewById(R.id.list);
        adapter = new MyAdapter();
        listView.setAdapter(adapter);
        getData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                //StatusActivity.this.openOptionsMenu();
                final String[] time = { "Manage Suppliers", "Update profile", "Deliveries", "Status" };
                AlertDialog.Builder builder = new AlertDialog.Builder(CompletedOrdersActivity.this);
                builder.setTitle("Budly");
                builder.setItems(time, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        if(position==0) {
                            startActivity(new Intent(CompletedOrdersActivity.this, ManagerSupplierActivity.class));
                        } else if(position==1){
                            startActivity(new Intent(CompletedOrdersActivity.this, ProfileUpdateActivity.class));
                        } else if(position==2){
                            startActivity(new Intent(CompletedOrdersActivity.this, TransactionActivity.class));
                        } else if(position==3){
                            startActivity(new Intent(CompletedOrdersActivity.this, StatusActivity.class));
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
            default:
                break;
        }
    }

    ArrayList<TransactionItem> mList = new ArrayList<TransactionItem>();

    class TransactionItem {
        public int id;
        public double total_price;
        public String date_time="";
        public String supplier_name;
        public String address;
    }

    static class MyHolder {
        LinearLayout layout;
        TextView txt_time_1, txt_order_id, txt_price, txt_order_time, txt_address;
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
                row = inflater.inflate(R.layout.driver_order_item, parent, false);
                holder = new MyHolder();
                holder.txt_time_1 = (TextView) row.findViewById(R.id.txt_time_1);
                holder.txt_order_id = (TextView) row.findViewById(R.id.txt_order_id);
                holder.txt_price = (TextView) row.findViewById(R.id.txt_price);
                holder.txt_order_time = (TextView) row.findViewById(R.id.txt_order_time);
                holder.txt_address = (TextView) row.findViewById(R.id.txt_address);
                holder.layout = (LinearLayout) row.findViewById(R.id.order_item_layout);
                row.setTag(holder);
            } else {
                holder = (MyHolder) row.getTag();
            }

            final TransactionItem item = mList.get(position);
            holder.txt_order_id.setText("Order #" + item.id);
            holder.txt_price.setText("$" + item.total_price);
            if (item.date_time == null || item.date_time.equals("null")){
                holder.txt_order_time.setText("-");
                holder.txt_time_1.setText("-");
           }else {
                holder.txt_order_time.setText(item.date_time.substring(item.date_time.lastIndexOf(" ")));
                holder.txt_time_1.setText(item.date_time.substring(0, item.date_time.lastIndexOf(" ")));
            }
                holder.txt_address.setText(item.address);
            holder.txt_price.setText("Amount: $"+item.total_price);
            if(position%2==0) {
                holder.layout.setBackgroundColor(Color.parseColor("#ddffffff"));
            } else {
                holder.layout.setBackgroundColor(Color.parseColor("#ddf1f0ec"));
            }

            return row;
        }
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ti.supplier_name = jso.getString("supplier_name");
                        ti.address = jso.getString("address");
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
                                        Toast.makeText(CompletedOrdersActivity.this, "You don't have " + titleStr, Toast.LENGTH_SHORT).show();
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

    @Override
    public void updateOrdersTextView(final String count){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                orderCountTextView.setText(count);
            }
        });
    }

}

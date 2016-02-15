package com.budly.android.CustomerApp.driver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.budly.android.CustomerApp.Globals;
import com.budly.R;
import com.budly.android.CustomerApp.TransactionActivity;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.budly.android.CustomerApp.td.widget.ProgressButton;
import com.turbomanage.httpclient.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chiichuy on 1/27/15.
 */
public class ActiveOrdersActivity extends Activity implements OnClickListener, DriverInterface {

    PreferenceHelper preferenceHelper;
    User mUser;
    TextView orderCountTextView;
    MyAdapter adapter;
    ListView listView;

    //menu
    ImageView btn_left;
    FrameLayout btn_completedOrders,btn_activeOrders,btn_orderDetails;

    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        preferenceHelper = PreferenceHelper.getInstance();
        mUser = preferenceHelper.getUserInfo();
        setContentView(R.layout.driver_activity_active_orders);



        orderCountTextView = (TextView) findViewById(R.id.orderCounttextView);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new MyAdapter();

        //menu
        this.btn_left = ((ImageView) findViewById(R.id.btn_left));
        this.btn_left.setOnClickListener(this);
        this.btn_completedOrders = (FrameLayout) findViewById(R.id.btn_CompletedOrders);
        this.btn_completedOrders.setOnClickListener(this);
        this.btn_activeOrders = (FrameLayout) findViewById(R.id.btn_activeOrders);
        this.btn_activeOrders.setOnClickListener(this);
        this.btn_orderDetails = (FrameLayout) findViewById(R.id.btn_orderDetails);
        //this.btn_orderDetails.setOnClickListener(this);

        listView.setAdapter(adapter);
        getData();
        registerBroadcast();
        Common.getOrdersCount(mUser.id, this);

        id=getIntent().getIntExtra("order_id", -1);
    }

    Handler mHandler = new Handler();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                //StatusActivity.this.openOptionsMenu();
                final String[] time = { "Manage Suppliers", "Update profile", "Deliveries", "Status" };
                AlertDialog.Builder builder = new AlertDialog.Builder(ActiveOrdersActivity.this);
                builder.setTitle("Budly");
                builder.setItems(time, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        if(position==0) {
                            startActivity(new Intent(ActiveOrdersActivity.this, ManagerSupplierActivity.class));
                        } else if(position==1){
                            startActivity(new Intent(ActiveOrdersActivity.this, ProfileUpdateActivity.class));
                        } else if(position==2){
                            startActivity(new Intent(ActiveOrdersActivity.this, TransactionActivity.class));
                        } else if(position==3){
                            startActivity(new Intent(ActiveOrdersActivity.this, StatusActivity.class));
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
            case R.id.btn_next:
                break;
            default:
                break;
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(REQUEST_CONFIRM)) {
                Log.e("Tuan2", "Broadcast ActiveOrders RequestConfirm");
            } else if(action.equals(ACCEPT_CONFIRM)) {
                Log.e("Tuan2", "Broadcast ActiveOrders AcceptConfirm");
                Common.getOrdersCount(mUser.id, ActiveOrdersActivity.this);
                adapter.clear();
                getData();
            } else if(action.equals(DENY_CONFIRM)) {
                Log.e("Tuan2", "Broadcast ActiveOrders DenyConfirm");
            } else if(action.equals(CANCELLED_ORDER)){
                Log.e("Tuan2", "Broadcast ActiveOrders CancelledOrder");
                Common.getOrdersCount(mUser.id, ActiveOrdersActivity.this);
                adapter.clear();
                getData();
                try {
                    Intent i = new Intent(getApplicationContext(), OrderCancelledActivity.class);
//                    i.putExtra("orderId",order_id);
                    startActivity(i);
                } catch (Exception e) { }
            }
        }
    };

    int id;

    @Override
    public void onBackPressed(){

        if(id==0){
            if(mList.size()>0){
                return;
            } else{
                super.onBackPressed();
            }
        } else{
            super.onBackPressed();
        }
    }

    public final static String REQUEST_CONFIRM = "request_confirm";
    public final static String ACCEPT_CONFIRM = "accept_confirm";
    public final static String DENY_CONFIRM = "deny_confirm";
    public final static String CANCELLED_ORDER = "cancelled_order";

    void registerBroadcast() {
        IntentFilter intent = new IntentFilter();
        //intent.addAction(REQUEST_CONFIRM);
        intent.addAction(ACCEPT_CONFIRM);
        intent.addAction(DENY_CONFIRM);
        intent.addAction(CANCELLED_ORDER);
        registerReceiver(mReceiver, intent);
    }

    void unRegisterBroadcast() {
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy(){
        unRegisterBroadcast();
        super.onDestroy();
    }

    ArrayList<Order> mList = new ArrayList<Order>();
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

        public void clear(){
            mList.clear();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            MyHolder holder = null;

            if (row != null) {
                holder = (MyHolder) row.getTag();
                holder.cancelProgress();
            }

            LayoutInflater inflater = getLayoutInflater();
            row = inflater.inflate(R.layout.main_active_order_item, parent, false);
            holder = new MyHolder();
            holder.txt_customer = (TextView) row.findViewById(R.id.txt_customer);
            holder.txt_order_id = (TextView) row.findViewById(R.id.txt_order);
            holder.txt_time = (TextView) row.findViewById(R.id.txt_time);
            holder.txt_address = (TextView) row.findViewById(R.id.txt_address);
            holder.layout = (LinearLayout) row.findViewById(R.id.transaction_item_layout);
            holder.txt_progTime = (TextView) row.findViewById(R.id.progTime);
            holder.txt_progMinutes = (TextView) row.findViewById(R.id.progMinutes);
            holder.progressButton = (ProgressButton) row.findViewById(R.id.prog_view);
            holder.progressButton.setInnerSize(getResources().getDimensionPixelSize(R.dimen.progress2_inner_size));
            //holder.guid = UUID.randomUUID().getMostSignificantBits()+"";
            holder.g =  new Globals();
            row.setTag(holder);


            final Order item = mList.get(position);
            holder.txt_customer.setText(item.customer_name);
            holder.txt_order_id.setText("Order #"+item.id);
            holder.txt_address.setText(item.address);
            holder.txt_time.setText(item.estimate_time + " minute drive");
            holder.guid = item.id +"";
            holder.order = item;
            if(position%2==0) {
                holder.layout.setBackgroundColor(Color.parseColor("#ddffffff"));
            } else {
                holder.layout.setBackgroundColor(Color.parseColor("#ddf1f0ec"));
            }
            holder.progressInit();

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    MyHolder holder = (MyHolder) view.getTag();
                    int customer_id = holder.order.customer_id;
                    int order_id = holder.order.id;
                    int estimate_time = holder.order.estimate_time;
                    String addressDestination = holder.order.address;
                    String start_date =  holder.order.start_date;
                    Intent intent = new Intent(context, DriverConfirmActivity.class);
                    intent.putExtra("customer_id", customer_id);
                    intent.putExtra("order_id", order_id);
                    intent.putExtra("estimate_time", estimate_time);
                    intent.putExtra("address",addressDestination);
                    intent.putExtra("start_time",start_date);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    finish();
                }
            });

            return row;
        }
    }

    class MyHolder implements Progress {
        LinearLayout layout;
        TextView txt_order_id, txt_time, txt_address, txt_customer;
        ProgressButton progressButton;
        TextView txt_progTime,txt_progMinutes;
        Order order;
        Globals g;
        String guid;
        Boolean run=false;
        Thread t;
        void run(){
            t = g.setProgressTimer(order.id,guid,order.estimate_time, order.start_date, this);
        }
        void progressInit(){
            run = true;
            cancelProgress();

            if(t == null){
                run = false;
                run();
                return;
            }

            if( t.getState() == Thread.State.TERMINATED){
                run = false;
                run();
                return;
            }


            //g.init(guid,order.estimate_time,order.start_date,order.id,this);
        }

        void cancelProgress(){
            run = false;
            Globals.stopProgress(guid);
        }

        @Override
        public void setProgressText(final String minutes,final String text){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txt_progMinutes.setText(minutes);
                    txt_progTime.setText(text);
                }
            });
        }

        @Override
        public void setProgress(int currentProgress,final int max){

            final int prog = currentProgress;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    progressButton.setMax(max);
//                    progressButton.setProgress(prog);
                    progressButton.setProgressAndMax(prog,max);
                }
            });
        }

        @Override
        public void endProgress() {
            if(run){
                run();
            }
        }
    }

    class Order {
        public int id;
        public String start_date="";
        public String address;
        public int estimate_time;
        public int customer_id;
        public String customer_name;
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
                        Order ti = new Order();
                        ti.id = jso.getInt("id");
                        ti.estimate_time = jso.getInt("estimate_time");
                        ti.customer_id = jso.getInt("customer_id");
                        ti.customer_name = jso.getString("customer_name");
                        ti.start_date = jso.getString("date_time");

                        ti.address = jso.getString("address");
                        mList.add(ti);
                    }

                    if(mList.size()==0) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                finish();
                                Toast.makeText(ActiveOrdersActivity.this, "You don't have orders", Toast.LENGTH_SHORT).show();
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
        client.getActiveOrdersByDriverID(mUser.id);
    }

    int mPage = 0;

    @Override
    public void updateOrdersTextView(final String count){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                orderCountTextView.setText(count);
            }
        });
    }


    String count = "";
    public void getOrdersCount(){
        HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){
            @Override
            public void onSuccess(HttpResponse httpResponse, JSONObject re) {
                try{
                    count = re.getJSONObject("data").getString("count");
                }catch (Exception e){
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        orderCountTextView.setText(count);
                    }
                });

            }

            @Override
            public void onFailure(Exception e) {
                // TODO Auto-generated method stub
                super.onFailure(e);
            }
        });

        client.getTotalOrdersByDriverID(preferenceHelper.getUserInfo().id);

    }
}

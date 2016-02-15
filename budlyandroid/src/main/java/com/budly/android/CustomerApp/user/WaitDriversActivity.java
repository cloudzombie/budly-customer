package com.budly.android.CustomerApp.user;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.android.CustomerApp.Common;
import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by EdwinSL on 2/18/2015.
 */
public class WaitDriversActivity extends BaseActivity implements View.OnClickListener {

    Button btn_next;
    PreferenceHelper preferenceHelper;
    ProgressBar loading;
    int currentProgress = 0;
    boolean isBreak = false;
    int order_id, supplier_id, distance;
    String supplier_name, mthumb;
    final int TIME_WAIT = 60000;//TODO: time_wait needs to value 60000
    final int TIME_STEP = 10000;

    ListView listView;
    MyAdapter adapter;


    User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_drivers);

        preferenceHelper = PreferenceHelper.getInstance();
        mUser = preferenceHelper.getUserInfo();

        loading = (ProgressBar) findViewById(R.id.loading);
        listView = (ListView) findViewById(R.id.list);
        adapter = new MyAdapter();
        listView.setAdapter(adapter);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);

        order_id = getIntent().getIntExtra("order_id", -1);
        supplier_id = getIntent().getIntExtra("supplier_id", -1);
        supplier_name = getIntent().getStringExtra("supplier_name");
        mthumb = getIntent().getStringExtra("thumb");
        distance = getIntent().getIntExtra("distance", 0);
        if(order_id<=0) {
            Toast.makeText(this, "Order id invalid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        registerGCM();
        registerBroadcast();
    }

    void registerGCM(){
        final HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback());

        new Thread(new Runnable() {

            @Override
            public void run() {
            String regId="";
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(WaitDriversActivity.this);
            try {
                regId = gcm.register(Common.PROJECT_NUMBER);
                preferenceHelper.setGCMID(regId);
                client.updateDevice(mUser.id, regId);
            }catch(IOException e){}
            }
        }).start();
    }

    void initData() {
//        if(drivers==null || drivers.length()==0) {
//            finish();
//            startActivity(new Intent(this, NoDriverActivity.class));
//            return;
//        }
        mList.clear();
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

    JSONArray drivers = new JSONArray();
    void getDrivers(){
        drivers = new JSONArray();
        HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

            @Override
            public void onFailure(Exception error) {
                super.onFailure(error);
                return;
            }

            @Override
            public void onSuccess(HttpResponse httpResponse, JSONObject re) {
                Log.i("Tuan", re.toString());
                try {
                    int status = re.getInt("status");
                    if(status==200) {
                        drivers = re.getJSONArray("data");
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                initData();

                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        client.getDrivers(order_id);
    }
    public final static String DRIVER_RESPONDED = "driver_responded";
    void registerBroadcast() {
        IntentFilter intent = new IntentFilter();
        intent.addAction(DRIVER_RESPONDED);
        registerReceiver(mReceiver, intent);
    }

    void unRegisterBroadcast() {
        unregisterReceiver(mReceiver);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Edwin","WaitDriversActivity received broadcast");
            getDrivers();
        }
    };

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
                            progressDialog = ProgressDialog.show(WaitDriversActivity.this, "", msg);
                        }
                    } catch (Exception e) { }
                    mHandler.removeCallbacks(rHiddenDlg);
                    mHandler.postDelayed(rHiddenDlg, 120000);
                }
            });
        } catch (Exception e) { e.printStackTrace(); }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                selectNoDriver();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unRegisterBroadcast();
        isBreak = true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

            holder.select.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        showMyDialog("Selecting driver");
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

    void selectNoDriver(){
        HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

            @Override
            public void onSuccess(HttpResponse httpResponse, JSONObject re) {
                try {
                    Log.i("Edwin", re.toString());
                    int status = re.getInt("status");
                    if(status==200) {
                        finish();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(WaitDriversActivity.this, "Have error on the server response", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(WaitDriversActivity.this, "Have error while request to server. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
                super.onFailure(e);
            }
        });
        try {
            client.selectDriver(order_id, -1, mUser.id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void selectDriver(final JSONObject driver) {
        HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

            @Override
            public void onSuccess(HttpResponse httpResponse, JSONObject re) {
                try {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            hiddenDialog();
                        }
                    });
                    int status = re.getInt("status");
                    if(status==200) {
                        finish();
                        String start_date = "";
                        try {
                            JSONObject data = re.getJSONObject("data");
                            Common.MOBILE_PHONE = data.getString("phone_number");

                        } catch (Exception e) { }

                        JSONObject json = new JSONObject();
                        json.put("driver", driver.toString());
                        json.put("order_id", order_id);
                        json.put("supplier_id", supplier_id);
                        json.put("supplier_name", supplier_name);
                        json.put("distance", distance);
                        json.put("thumb", mthumb);
                        json.put("phone", Common.MOBILE_PHONE);
                        preferenceHelper.setValue(PreferenceHelper.LASTORDER,json.toString());


                        Intent intent = new Intent(WaitDriversActivity.this, OrderConfirmedActivity.class);
                        intent.putExtra("driver", driver.toString());
                        intent.putExtra("order_id", order_id);
                        intent.putExtra("supplier_id", supplier_id);
                        intent.putExtra("supplier_name", supplier_name);
                        intent.putExtra("distance", distance);
                        intent.putExtra("thumb", mthumb);
                        startActivity(intent);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(WaitDriversActivity.this, "Have error while request to server. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        hiddenDialog();
                        Toast.makeText(WaitDriversActivity.this, "Have error while request to server. Please try again", Toast.LENGTH_SHORT).show();
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

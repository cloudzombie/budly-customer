package com.budly.android.CustomerApp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.budly.R;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class TransactionDetailActivity extends Activity {

    View layout_detail;
    MyAdapter adapter;
    ListView items;
    TextView date_time, supplier_name, status, total_price, id;
    int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_transaction_detail);

        orderId = getIntent().getIntExtra("id", -1);

        ((TextView)findViewById(R.id.layout_detail).findViewById(R.id.txt_time_1))
                .setText(getIntent().getStringExtra("date_time"));
        ((TextView)findViewById(R.id.layout_detail).findViewById(R.id.txt_supplier))
                .setText(getIntent().getStringExtra("supplier_name"));
        ((TextView)findViewById(R.id.layout_detail).findViewById(R.id.txt_order_id))
                .setText("Order #"+orderId);
        ((TextView)findViewById(R.id.layout_detail).findViewById(R.id.txt_status))
                .setText(getIntent().getStringExtra("status"));
        ((TextView)findViewById(R.id.layout_detail).findViewById(R.id.txt_order_price))
                .setText("$"+new DecimalFormat("0.00").format(getIntent().getDoubleExtra("total_price", 0)));

        items = (ListView) findViewById(R.id.items);
        adapter = new MyAdapter();
        items.setAdapter(adapter);

        getData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_transaction_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ArrayList<TransactionDetailItem> mList = new ArrayList<TransactionDetailItem>();
    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            if(view == null)
                view = getLayoutInflater().inflate(R.layout.main_transaction_detail_item, viewGroup, false);

            final TransactionDetailItem item = mList.get(position);
            ((TextView) view.findViewById(R.id.item_name))
                    .setText(item.item_name);
            ((TextView) view.findViewById(R.id.item_price))
                    .setText("$"+new DecimalFormat("0.00").format(item.total_price));

            if(position%2==0) {
                view.findViewById(R.id.transaction_detail_item)
                        .setBackgroundColor(Color.parseColor("#ddffffff"));
            } else {
                view.findViewById(R.id.transaction_detail_item)
                        .setBackgroundColor(Color.parseColor("#ddf1f0ec"));
            }

            return view;
        }
    }

    class TransactionDetailItem {
        public int id;
        public double total_price;
        public String item_name="";
    }

    void getData() {
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
                        JSONObject data = re.getJSONObject("data");

                        JSONArray jsonItems = data.getJSONArray("order_detail");

                        for(int i=0; i < jsonItems.length(); i++)
                        {
                            TransactionDetailItem ti = new TransactionDetailItem();
                            JSONObject jsonItem = jsonItems.getJSONObject(i);
                            ti.item_name = jsonItem.getString("name") +" - "+ jsonItem.getString("size");
                            ti.total_price = jsonItem.getDouble("price") * jsonItem.getDouble("amount");
                            mList.add(ti);
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

        client.getOrder(orderId, -1);
    }
}

package com.budly.android.CustomerApp.user;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.budly.BaseActivity;
import com.budly.R;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.widget.ProgressButton;
import com.turbomanage.httpclient.HttpResponse;

public class OrderProcessingActivity extends BaseActivity implements OnClickListener{
	
	ProgressButton progressButton;
	int currentProgress = 0;
	boolean isBreak = false;
	int order_id, supplier_id;
	String supplier_name;
	final int TIME_WAIT = 60000;
	final int TIME_STEP = 10;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_activity_order_processing);
		order_id = getIntent().getIntExtra("order_id", -1);
		if(order_id<=0) {
			Toast.makeText(this, "Order id invalid", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		progressButton = (ProgressButton) findViewById(R.id.pin_progress_1);
		progressButton.setMax(TIME_WAIT);
		progressButton.setProgress(currentProgress);

		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					int n = (int) Math.round(TIME_WAIT/TIME_STEP+0.5);
					for (int i = 1; i <= n; i++) {
						if(!isBreak) {
							try {
								Thread.sleep(TIME_STEP);	
							} catch (Exception e) { }
							currentProgress+=TIME_STEP;
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									if(currentProgress>=TIME_WAIT) currentProgress = TIME_WAIT;
									progressButton.setProgress(currentProgress);
								}
							});
						} else {
							break;
						}
					}
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							if(!isBreak) {
								getDrivers();
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	JSONArray drivers = new JSONArray();
	void getDrivers(){
		drivers = new JSONArray();
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onFailure(Exception error) {
				super.onFailure(error);
				finish();
				startActivity(new Intent(OrderProcessingActivity.this, ListDriverActivity.class));
				return;
			}

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				Log.i("Tuan", re.toString());
				try {
					int status = re.getInt("status");
					if(status==200) {
						drivers = re.getJSONArray("data");
						if(drivers.length()>0) {
							if(drivers.length()==1) {
								finish();
								Intent intent = new Intent(OrderProcessingActivity.this, OneDriverActivity.class);
								intent.putExtra("drivers", drivers.toString());
								intent.putExtra("order_id", order_id);
								startActivity(intent);
							} else {
								finish();
								Intent intent = new Intent(OrderProcessingActivity.this, ListDriverActivity.class);
								intent.putExtra("drivers", drivers.toString());
								intent.putExtra("order_id", order_id);
								startActivity(intent);
							}
							return;
						} else {
							finish();
							startActivity(new Intent(OrderProcessingActivity.this, NoDriverActivity.class));
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				finish();
				startActivity(new Intent(OrderProcessingActivity.this, NoDriverActivity.class));
			}
		});
		client.getDrivers(order_id);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isBreak = true;
	}
	
	@Override
	public void onBackPressed() {
		
	}

}

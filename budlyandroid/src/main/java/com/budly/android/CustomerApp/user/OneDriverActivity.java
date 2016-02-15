package com.budly.android.CustomerApp.user;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.android.CustomerApp.Common;
import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.budly.android.CustomerApp.td.widget.ProgressButton;
import com.turbomanage.httpclient.HttpResponse;

public class OneDriverActivity extends BaseActivity implements OnClickListener{
	
	ImageView btn_accept, btn_deny;
	TextView txt_driver_name, txt_on_time_rank, txt_time;
	
	ProgressButton progressButton;
	int currentProgress = 0;
	
	JSONArray drivers=null;
	JSONObject driver=null;
	int order_id=-1, supplier_id;
	User mUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_activity_one_driver);
		btn_accept = (ImageView) findViewById(R.id.btn_accept);
		btn_accept.setOnClickListener(this);
		btn_deny = (ImageView) findViewById(R.id.btn_deny);
		btn_deny.setOnClickListener(this);
		txt_driver_name = (TextView) findViewById(R.id.txt_driver_name);
		txt_on_time_rank = (TextView) findViewById(R.id.txt_on_time_rank);
		txt_time = (TextView) findViewById(R.id.txt_time);
		
		PreferenceHelper pre = PreferenceHelper.getInstance();
		mUser = pre.getUserInfo();
		
		progressButton = (ProgressButton) findViewById(R.id.pin_progress_1);
		
		try {
			order_id = getIntent().getIntExtra("order_id", -1);
            supplier_id = getIntent().getIntExtra("supplier_id", -1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			drivers = new JSONArray(getIntent().getStringExtra("drivers"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		initData();
	}
	
	int estimate_time = 0;
	final int TIME_STEP = 100;
	
	void initData() {
		if(drivers==null || drivers.length()==0) {
			finish();
			startActivity(new Intent(this, NoDriverActivity.class));
			return;
		}
		try {
			driver = drivers.getJSONObject(0);
			txt_driver_name.setText(driver.getString("first_name"));
			txt_on_time_rank.setText(driver.getString("on_time_rank")+"%");
			txt_time.setText(driver.getString("estimate_time"));
			estimate_time = driver.getInt("estimate_time");
			//progressButton.setMax(estimate_time*60000);
			//progressButton.setProgress(currentProgress);
			return;
		} catch (Exception e) {
			driver = null;
			e.printStackTrace();
		}
		finish();
		startActivity(new Intent(this, NoDriverActivity.class));
		return;
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_accept:
			if(driver!=null) {
				selectDriver();
			}
			break;
		case R.id.btn_deny:
			finish();
			break;

		default:
			break;
		}
	}
	
	void selectDriver() {
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				try {
					int status = re.getInt("status");
					if(status==200) {
						try {
							JSONObject data = re.getJSONObject("data");
							Common.MOBILE_PHONE = data.getString("phone_number");
						} catch (Exception e) { }
						finish();
						Intent intent = new Intent(OneDriverActivity.this, OrderConfirmedActivity.class);
						intent.putExtra("driver", driver.toString());
						intent.putExtra("order_id", order_id);
                        intent.putExtra("supplier_id", supplier_id );
						startActivity(intent);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(OneDriverActivity.this, "Have error while request to server. Please try again", Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onFailure(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(OneDriverActivity.this, "Have error while request to server. Please try again", Toast.LENGTH_SHORT).show();
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


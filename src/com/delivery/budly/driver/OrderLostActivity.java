package com.budly.android.CustomerApp.driver;

import android.os.Bundle;
import android.os.Handler;

import com.budly.BaseActivity;
import com.budly.R;

public class OrderLostActivity extends BaseActivity {
	
	Handler mHandler = new Handler();
	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.driver_activity_order_lost);
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				finish();
			}
		}, 15000);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StatusActivity.CURRENT_STATUS = StatusActivity.STATUS_ORERLOST;
	}
}
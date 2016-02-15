package com.budly.android.CustomerApp.driver;

import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.android.CustomerApp.Common;
import com.budly.R;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;

public class OrderLostActivity extends BaseActivity {
	
	Handler mHandler = new Handler();
	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

        Common.turnOnScreen(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.driver_activity_order_lost);
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				finish();
			}
		}, 4000);


        PreferenceHelper.getInstance().removeNotification(getIntent().getStringExtra("notify_id"));
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StatusActivity.CURRENT_STATUS = StatusActivity.STATUS_ORERLOST;
	}

    @Override
    protected  void onDestroy(){
        Common.hideNotification(this);
        super.onDestroy();
    }
}
package com.budly.android.CustomerApp.driver;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.budly.BaseActivity;
import com.budly.R;

public class ReportActivity extends BaseActivity implements OnClickListener{
	
	Handler mHandler = new Handler();
	Button btn_next;
	ImageView btn_left;
	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.driver_activity_report);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_left = (ImageView) findViewById(R.id.btn_left);
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			finish();
			break;
			
		case R.id.btn_next:
			finish();
			break;

		default:
			break;
		}
	}
}
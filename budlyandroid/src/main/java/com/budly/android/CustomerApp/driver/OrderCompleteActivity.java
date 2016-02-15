package com.budly.android.CustomerApp.driver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.R;

public class OrderCompleteActivity extends BaseActivity implements OnClickListener {
	
	Handler mHandler = new Handler();

    Button report;
    int orderID;
	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.driver_activity_order_complete);
        report = (Button) findViewById(R.id.reportProblem);
        report.setOnClickListener(this);
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				finish();
			}
		}, 3000);
        orderID = getIntent().getIntExtra("orderId",0);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StatusActivity.CURRENT_STATUS = StatusActivity.STATUS_ORERCOMPLETE;
	}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reportProblem:
                Intent i = new Intent(this,ReportActivity.class);
                i.putExtra("orderId",orderID);
                startActivity(i);
                finish();
                break;

            default:
                break;
        }
    }
}
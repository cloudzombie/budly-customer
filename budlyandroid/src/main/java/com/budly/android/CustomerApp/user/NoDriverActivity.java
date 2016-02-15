package com.budly.android.CustomerApp.user;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.R;

public class NoDriverActivity extends BaseActivity implements OnClickListener{
	
	Button btn_next;
	ImageView btn_left;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_activity_no_driver);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_next.setOnClickListener(this);
		btn_left = (ImageView) findViewById(R.id.btn_left);
		btn_left.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
			//startActivity(new Intent(NoDriverActivity.this, LocationActivity.class));
			finish();
			break;
		case R.id.btn_left:
			//startActivity(new Intent(PlaceOrderActivity.this, ProfileUpdateActivity.class));
			break;

		default:
			break;
		}
	}
}

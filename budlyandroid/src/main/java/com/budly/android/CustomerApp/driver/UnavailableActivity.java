package com.budly.android.CustomerApp.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.android.CustomerApp.Common;
import com.budly.R;
import com.budly.android.CustomerApp.TransactionActivity;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;

public class UnavailableActivity extends BaseActivity implements
		View.OnClickListener {
	ImageView btn_available;
	ImageView btn_left;
	ImageView btn_unavailable;
	TextView txt_first_name;
	PreferenceHelper preferenceHelper;
	User mUser;

	public void onClick(View paramView) {
		switch (paramView.getId()) {
		case R.id.btn_i_am_available:
			startActivity(new Intent(this, HasOrderActivity.class));
			break;
		case R.id.btn_i_am_unavailable:
			break;
		case R.id.btn_left:
			openOptionsMenu();
			break;
		}
	}

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

        Common.setScreenFlag(this);

		preferenceHelper = PreferenceHelper.getInstance();
		mUser = preferenceHelper.getUserInfo();
		setContentView(R.layout.driver_activity_unavailable);
		txt_first_name = (TextView) findViewById(R.id.txt_first_name);
		txt_first_name.setText(mUser.first_name);
		this.btn_left = ((ImageView) findViewById(R.id.btn_left));
		this.btn_available = ((ImageView) findViewById(R.id.btn_i_am_available));
		this.btn_unavailable = ((ImageView) findViewById(R.id.btn_i_am_unavailable));
		this.btn_left.setOnClickListener(this);
		this.btn_available.setOnClickListener(this);
		this.btn_unavailable.setOnClickListener(this);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.driver, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.manager_suppliers:
	        	startActivity(new Intent(this, ManagerSupplierActivity.class));
	            return true;
	        case R.id.update_profile:
	        	startActivity(new Intent(this, ProfileUpdateActivity.class));
	            return true;
	        case R.id.delivery:
	        	//Toast.makeText(this, "Under construction", Toast.LENGTH_LONG).show();
	        	startActivity(new Intent(this, TransactionActivity.class));
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
package com.budly.android.CustomerApp.user;

import org.json.JSONArray;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.R;
import com.budly.android.CustomerApp.driver.StatusActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class RateActivity extends BaseActivity implements View.OnClickListener {
	Button btn_next;
	ImageView rate1, rate2, rate3, rate4, rate5, thumb;
	TextView name;
	
	// Khai bao options display image
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.generic_logo)
		.showImageForEmptyUri(R.drawable.generic_logo)
		.showImageOnFail(R.drawable.generic_logo)
//		.displayer(new RoundedBitmapDisplayer(200))
		.cacheInMemory(false).cacheOnDisc(true).build();

	public void onClick(View paramView) {
		switch (paramView.getId()) {
		
		case R.id.btn_next:
			if(score==0)
				Toast.makeText(this, "Please rate 1-5 star.", Toast.LENGTH_SHORT).show();
			else
				sendRate();
			break;
		case R.id.rate1:
			rate(1);
			break;
		case R.id.rate2:
			rate(2);
			break;
		case R.id.rate3:
			rate(3);
			break;
		case R.id.rate4:
			rate(4);
			break;
		case R.id.rate5:
			rate(5);
			break;
		default:
			break;
		}
	}
	
	void sendRate() {
		showDialog("sending...");
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){
			@Override
			public void onSuccess(HttpResponse httpResponse, JSONArray re) {
				super.onSuccess(httpResponse, re);
				finish();
				hiddenDialog();
			}
			
			@Override
			public void onFailure(Exception e) {
				super.onFailure(e);
				finish();
				hiddenDialog();
			}
		});
		
		client.rate(OrderConfirmedActivity.driver_id, ListServiceInAreaActivity.supplier_id, score);
	}
	
	int score = 0;
	void rate(int score) {
		this.score = score;
		ImageView[] rates = {rate1, rate2, rate3, rate4, rate5};
		for (int i = 0; i < rates.length; i++) {
			if(i<score) {
				rates[i].setImageResource(R.drawable.star_a);
			} else {
				rates[i].setImageResource(R.drawable.star_i);
			}
		}
	}

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.user_activity_rate);
		btn_next = ((Button) findViewById(R.id.btn_next));
		btn_next.setOnClickListener(this);
		rate1 = (ImageView) findViewById(R.id.rate1);
		rate2 = (ImageView) findViewById(R.id.rate2);
		rate3 = (ImageView) findViewById(R.id.rate3);
		rate4 = (ImageView) findViewById(R.id.rate4);
		rate5 = (ImageView) findViewById(R.id.rate5);
		
		ImageView[] rates = {rate1, rate2, rate3, rate4, rate5};
		for (int i = 0; i < rates.length; i++) {
			rates[i].setOnClickListener(this);
		}
		
		thumb = (ImageView) findViewById(R.id.thumb);
		name = (TextView) findViewById(R.id.name);
		
		ImageLoader.getInstance().displayImage(ListServiceInAreaActivity.supplier_image, thumb, options);
		name.setText(ListServiceInAreaActivity.supplier_name);
	}
	
	@Override
	protected void onResume() {
		StatusActivity.CURRENT_STATUS = StatusActivity.STATUS_ORERCOMPLETE;
		super.onResume();
	}
	
	ProgressDialog progressDialog;
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
	
	void showDialog(final String msg) {
		try {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						if(progressDialog!=null && progressDialog.isShowing()) {
							progressDialog.setMessage(msg);
						} else {
							progressDialog = ProgressDialog.show(RateActivity.this, "", msg);
						}
					} catch (Exception e) { }
				}
			});
		} catch (Exception e) { }
	}
	
}
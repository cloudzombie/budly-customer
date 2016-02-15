package com.budly.android.CustomerApp;

import java.io.File;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.user.ProfileActivity;
import com.budly.android.CustomerApp.user.RecomendationActivity;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.google.android.gms.analytics.HitBuilders;
import com.turbomanage.httpclient.HttpResponse;

public class MainActivity extends Activity implements View.OnClickListener {
	ImageView btn_customer;
	ImageView btn_i_am_a_driver;
	View progressBar;
	PreferenceHelper preferenceHelper;

	public void onClick(View paramView) {
		switch (paramView.getId()) {
		case R.id.btn_customer:
			updateUser();
			finish();
//            startActivity(new Intent(this, RecomendationActivity.class));
			startActivity(new Intent(this, ProfileActivity.class));
			break;
		case R.id.btn_i_am_a_driver:
			updateUser();
			finish();
			startActivity(new Intent(this, com.budly.android.CustomerApp.driver.ProfileActivity.class));
			break;
		default:
			break;
		}

	}
	
	void updateUser() {
		User user = preferenceHelper.getUserInfo();
		user.phone_number = mPhoneNumber;
		preferenceHelper.setUserInfo(user);
	}


	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

        if(Common.notifications ==null)
            Common.initCommon(this);
		setContentView(R.layout.main_activity_main);
		preferenceHelper = PreferenceHelper.getInstance();
		try {
			File f = new File(Common.SDCARD_AUDIO);
			if(!f.exists()) {
				f.mkdirs();
			}	
		} catch (Exception e) { }


		this.btn_i_am_a_driver = ((ImageView) findViewById(R.id.btn_i_am_a_driver));
		this.btn_customer = ((ImageView) findViewById(R.id.btn_customer));
		this.btn_i_am_a_driver.setOnClickListener(this);
		this.btn_customer.setOnClickListener(this);
		progressBar = findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
		btn_customer.setVisibility(View.GONE);
		btn_i_am_a_driver.setVisibility(View.GONE);
		
		User user = preferenceHelper.getUserInfo();
		Log.i("Tuan", user.toJsonString());
		
//		startActivity(new Intent(MainActivity.this, com.budly.android.CustomerApp.user.ProfilePictureLicenseActivity.class));
//		if(1==1) return;
		
		if(isValidPhoneNumber(user.phone_number) && user.type.equals("customer")) {
			if(user.id>0) {
				finish();
				startActivity(new Intent(MainActivity.this, com.budly.android.CustomerApp.user.PlaceOrderActivity.class));
			} else {
				finish();
				startActivity(new Intent(MainActivity.this, com.budly.android.CustomerApp.user.LoginActivity.class));
			}
		} else {
			getPhoneNumber();
			//startActivity(new Intent(this, ProfilePictureActivity.class));
		}
	}
	
	String mPhoneNumber = "";
	
	void getPhoneNumber() {
		try {
			TelephonyManager manager =(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			mPhoneNumber = manager.getLine1Number();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(mPhoneNumber==null || mPhoneNumber.equals("") || isValidPhoneNumber(mPhoneNumber)==false) {
			mPhoneNumber = "";
			manualPhoneNumber();
		} else {
			checkPhone();
		}
	}
	
	boolean isValidPhoneNumber(String value) {
		String regexStr = "^[0-9]{10,13}$";
		if(value.length()<10 || value.length()>13 || value.matches(regexStr)==false  ) {
			return false;
        } else {
        	return true;
        }
	}
	
	void manualPhoneNumber() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Budly");
		alert.setMessage("Enter your phone number");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_PHONE);
		alert.setView(input);
		alert.setCancelable(false);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			  mPhoneNumber = input.getText().toString();
			  Log.i("Tuan", "Phone number "+mPhoneNumber);
	          if(isValidPhoneNumber(mPhoneNumber)==false) {
	        	  mPhoneNumber = "";
	        	  dialog.dismiss();
	              Toast.makeText(MainActivity.this,"Please enter valid phone number",Toast.LENGTH_SHORT).show();
	              manualPhoneNumber();
	          } else {
	        	  dialog.dismiss();
	        	  checkPhone();
	          }
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    finish();
		  }
		});

		alert.show();
	}
	
	void showChooseCustomerDriver() {
		updateUser();
		finish();
//			startActivity(new Intent(this, com.budly.android.CustomerApp.user.ProfileActivity.class));
//		startActivity(new Intent(this, RecomendationActivity.class));
		startActivity(new Intent(this, ProfileActivity.class));
//		runOnUiThread(new Runnable() {
//
//			@Override
//			public void run() {
//				progressBar.setVisibility(View.GONE);
//				btn_customer.setVisibility(View.VISIBLE);
//				btn_i_am_a_driver.setVisibility(View.VISIBLE);
//			}
//		});
	}
	
	void checkPhone() {
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onFailure(Exception e) {
                Common.saveException(e);
				// TODO Auto-generated method stub
				super.onFailure(e);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Check phone problems", Toast.LENGTH_SHORT).show();
                    }
                });
			}

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject response) {
				// TODO Auto-generated method stub
				//super.onSuccess(statusCode, response);
				try {
					Log.i("Tuan", response.toString());
					JSONObject data = response.getJSONObject("data");
					String status = data.getString("status");
					if(status.equals("unavaiable")) {
                        //throw new CustomException("Account unavailable");
						showChooseCustomerDriver();
					} else {
						String type = data.getString("type");
						if(type.equals("customer")) {
							preferenceHelper.setPhoneNumber(mPhoneNumber);
							finish();
							startActivity(new Intent(MainActivity.this, com.budly.android.CustomerApp.user.LoginActivity.class));
						} else {
							mPhoneNumber="";
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
								Toast.makeText(MainActivity.this, "Invalid customer number", Toast.LENGTH_SHORT).show();
								finish();
								}
							});
						}
					}
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
				showChooseCustomerDriver();
				
			}
			
		});
		client.checkphone(mPhoneNumber);
	}
}
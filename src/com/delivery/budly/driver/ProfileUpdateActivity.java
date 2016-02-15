package com.budly.android.CustomerApp.driver;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.BaseActivity;
import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

public class ProfileUpdateActivity extends BaseActivity implements View.OnClickListener {
	EditText first_name, last_name, email, password, password_again, address, zipcode, state, city;
	PreferenceHelper preferenceHelper;
	User mUser;
	
	TextView change_profile_photo; 

	public void onClick(View paramView) {
		switch (paramView.getId()) {
		case R.id.change_profile_photo:
			startActivity(new Intent(this, ProfilePictureUpdateActivity.class));
			break;
		default:
			break;
		}

	}
	
	void update() {
		if (first_name.getText().toString().equals("") || last_name.getText().toString().equals("")
				|| email.getText().toString().equals("")
				|| password.getText().toString().equals("")
				|| password_again.getText().toString().equals("")) {
			Toast.makeText(this, "You must enter all information", Toast.LENGTH_SHORT).show();
		} else {
			if(!password.getText().toString().equals(password_again.getText().toString())) {
				Toast.makeText(this, "Password not match", Toast.LENGTH_SHORT).show();
			} else {
				mUser.first_name = first_name.getText().toString();
				mUser.last_name = last_name.getText().toString();
				mUser.email = email.getText().toString();
				mUser.password = password.getText().toString();
				mUser.address = address.getText().toString();
				mUser.state = state.getText().toString();
				mUser.zip = zipcode.getText().toString();
				mUser.City = city.getText().toString();
				HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

					@Override
					public void onFailure(Exception error) {
						// TODO Auto-generated method stub
						super.onFailure(error);
						error.printStackTrace();
						Toast.makeText(ProfileUpdateActivity.this, "Update failed. Please try again", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onSuccess(HttpResponse httpResponse, JSONObject response) {
						// TODO Auto-generated method stub
						try {
							Log.i("Tuan", response.toString());
							boolean is_update = response.getBoolean("data");
							int status = response.getInt("status");
							if(is_update && status==200) {
								preferenceHelper.setUserInfo(mUser);
								startActivity(new Intent(ProfileUpdateActivity.this, ProfilePictureActivity.class));
								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										Toast.makeText(ProfileUpdateActivity.this, "Update success", Toast.LENGTH_SHORT).show();
									}
								});
								return;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Toast.makeText(ProfileUpdateActivity.this, "Update failed. Please try again", Toast.LENGTH_SHORT).show();
							}
						});
					}
					
				});
				try {
					JSONObject data = new JSONObject();
					data.put("first_name", mUser.first_name);
					data.put("last_name", mUser.last_name);
					data.put("email", mUser.email);
					data.put("password", mUser.password);
					data.put("phone_number", mUser.phone_number);
					data.put("id", mUser.id);
					data.put("address", mUser.address);
					data.put("state", mUser.state);
					data.put("City", mUser.City);
					data.put("zip", mUser.zip);
					client.update(data.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	void register() {
		if (first_name.getText().toString().equals("") || last_name.getText().toString().equals("")
				|| email.getText().toString().equals("")
				|| password.getText().toString().equals("")
				|| password_again.getText().toString().equals("")) {
			Toast.makeText(this, "You must enter all information", Toast.LENGTH_SHORT).show();
		} else {
			if(!password.getText().toString().equals(password_again.getText().toString())) {
				Toast.makeText(this, "Password not match", Toast.LENGTH_SHORT).show();
			} else {
				mUser.first_name = first_name.getText().toString();
				mUser.last_name = last_name.getText().toString();
				mUser.email = email.getText().toString();
				mUser.password = password.getText().toString();
				mUser.address = address.getText().toString();
				mUser.state = state.getText().toString();
				mUser.zip = zipcode.getText().toString();
				mUser.City = city.getText().toString();
				HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

					@Override
					public void onFailure(Exception error) {
						// TODO Auto-generated method stub
						super.onFailure(error);
						error.printStackTrace();
						Toast.makeText(ProfileUpdateActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onSuccess(HttpResponse httpResponse, JSONObject response) {
						// TODO Auto-generated method stub
						//super.onSuccess(statusCode, response);
						try {
							Log.i("Tuan", response.toString());
							int id = response.getInt("data");
							int status = response.getInt("status");
							if(id>0 && status==200) {
								mUser.id = id;
								mUser.type = "customer";
								preferenceHelper.setUserInfo(mUser);
								startActivity(new Intent(ProfileUpdateActivity.this, ProfilePictureActivity.class));
								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										Toast.makeText(ProfileUpdateActivity.this, "Register success", Toast.LENGTH_SHORT).show();
									}
								});
								
								return;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Toast.makeText(ProfileUpdateActivity.this, "Register failed. Please try again", Toast.LENGTH_SHORT).show();
							}
						});
					}
					
				});
				client.register(mUser.first_name, mUser.last_name, mUser.phone_number, mUser.email, 
						mUser.password, "customer", mUser.address, mUser.City, mUser.state, mUser.zip);
			}
		}
	}

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.driver_activity_profile_update);
		change_profile_photo = (TextView) findViewById(R.id.change_profile_photo);
		change_profile_photo.setOnClickListener(this);
		first_name = (EditText) findViewById(R.id.txt_first_name);
		last_name = (EditText) findViewById(R.id.txt_last_name);
		email = (EditText) findViewById(R.id.txt_email);
		address = (EditText) findViewById(R.id.txt_address);
		zipcode = (EditText) findViewById(R.id.txt_zip);
		city = (EditText) findViewById(R.id.txt_city);
		state = (EditText) findViewById(R.id.txt_state);
		password = (EditText) findViewById(R.id.txt_password);
		password_again = (EditText) findViewById(R.id.txt_password_again);
		preferenceHelper = new PreferenceHelper(this);
		mUser = preferenceHelper.getUserInfo();
		first_name.setText(mUser.first_name);
		last_name.setText(mUser.last_name);
		email.setText(mUser.email);
		password.setText(mUser.password);
		password_again.setText(mUser.password);
		address.setText(mUser.address);
		state.setText(mUser.state);
		zipcode.setText(mUser.zip);
		city.setText(mUser.City);
		if(mUser.id>0) {
			((EditText) findViewById(R.id.txt_number)).setText("#"+mUser.id);
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		try {
			mUser = preferenceHelper.getUserInfo();	
		} catch (Exception e) {
		}
	}
	
	@Override
	public void onBackPressed() {
		update();
		super.onBackPressed();
	}
}
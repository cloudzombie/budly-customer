package com.budly.android.CustomerApp.driver;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.budly.BaseActivity;
import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

public class ProfileActivity extends BaseActivity implements View.OnClickListener {
	Button btn_next;
	EditText first_name, last_name, email, password, password_again;
	PreferenceHelper preferenceHelper;
	User mUser;

	public void onClick(View paramView) {
		switch (paramView.getId()) {
		default:
			break;
		case R.id.btn_next:
			if(mUser.id>0) {
				update();
			} else {
				register();
			}
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
				HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

					@Override
					public void onFailure(Exception error) {
						// TODO Auto-generated method stub
						super.onFailure(error);
						error.printStackTrace();
						Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
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
								startActivity(new Intent(ProfileActivity.this, ProfilePictureActivity.class));
								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										Toast.makeText(ProfileActivity.this, "Update success", Toast.LENGTH_SHORT).show();
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
								//Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
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
				HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

					@Override
					public void onFailure(Exception error) {
						// TODO Auto-generated method stub
						super.onFailure(error);
						error.printStackTrace();
						Toast.makeText(ProfileActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
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
								mUser.type = "driver";
								preferenceHelper.setUserInfo(mUser);
								startActivity(new Intent(ProfileActivity.this, ProfilePictureActivity.class));
								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										Toast.makeText(ProfileActivity.this, "Register success", Toast.LENGTH_SHORT).show();
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
								Toast.makeText(ProfileActivity.this, "Register failed. Please try again", Toast.LENGTH_SHORT).show();
							}
						});
					}
					
				});
				client.register(mUser.first_name, mUser.last_name, mUser.phone_number, mUser.email, 
						mUser.password, "driver", mUser.address, mUser.City, mUser.state, mUser.zip);
			}
		}
	}

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.driver_activity_profile);
		
//		Display display = getWindowManager().getDefaultDisplay(); 
//		int width = display.getWidth();  // deprecated
//		int height = display.getHeight();  // deprecated
//		
//		try {
//			LinearLayout layout_input = (LinearLayout) findViewById(R.id.layout_input);
//			LayoutParams layoutParams = (LayoutParams) layout_input.getLayoutParams();
//			layoutParams.height = (int)(height*(941.0f/1136.0f));
//			layout_input.setLayoutParams(layoutParams);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		this.btn_next = ((Button) findViewById(R.id.btn_next));
		this.btn_next.setOnClickListener(this);
		first_name = (EditText) findViewById(R.id.txt_first_name);
		last_name = (EditText) findViewById(R.id.txt_last_name);
		email = (EditText) findViewById(R.id.txt_email);
		password = (EditText) findViewById(R.id.txt_password);
		password_again = (EditText) findViewById(R.id.txt_password_again);
		preferenceHelper = new PreferenceHelper(this);
		mUser = preferenceHelper.getUserInfo();
		first_name.setText(mUser.first_name);
		last_name.setText(mUser.last_name);
		email.setText(mUser.email);
		password.setText(mUser.password);
		password_again.setText(mUser.password);
		if(mUser.id>0) {
			((EditText) findViewById(R.id.txt_number)).setText("#"+mUser.id);
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(StatusActivity.IS_FIRST) finish();
	}
}
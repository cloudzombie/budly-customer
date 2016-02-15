package com.budly.android.CustomerApp.user;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
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

public class LoginActivity extends BaseActivity implements OnClickListener{
	
	Button btn_next;
	PreferenceHelper preferenceHelper;
	User mUser;
	EditText txt_pass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_activity_login);
		preferenceHelper = new PreferenceHelper(this);
		mUser = preferenceHelper.getUserInfo();
		setContentView(R.layout.driver_activity_login);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_next.setOnClickListener(this);
		txt_pass = (EditText) findViewById(R.id.txt_pass);
	}
	
	ProgressDialog progressDialog;
	
	void login() {
		if(txt_pass.getText().toString().equals("")) {
			Toast.makeText(this, "You must enter password", Toast.LENGTH_SHORT).show();
			return;
		}
		
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onFailure(Exception error) {
				super.onFailure(error);
				runOnUiThread(new Runnable() {
					public void run() {
						try {
							progressDialog.dismiss();
							Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				Log.i("Tuan", re.toString());
				runOnUiThread(new Runnable() {
					public void run() {
						try {
							progressDialog.dismiss();	
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				
				try {
					int status = re.getInt("status");
					if(status==200) {
						JSONObject data = re.getJSONObject("data");
						mUser.id = data.getInt("id");
						mUser.zip = data.getString("zip");
						mUser.first_name = data.getString("first_name");
						mUser.last_name = data.getString("last_name");
						mUser.address = data.getString("address");
						mUser.email = data.getString("email");
						mUser.state = data.getString("state");
						mUser.City = data.getString("city");
						try {
							mUser.image = data.getString("image");
						} catch (Exception e) {}
						
						try {
							mUser.image_license = data.getString("image_license");
						} catch (Exception e) {}
						
						mUser.type = "customer";
						mUser.password = txt_pass.getText().toString();
						preferenceHelper.setUserInfo(mUser);
						startActivity(new Intent(LoginActivity.this, PlaceOrderActivity.class));
						finish();
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
		progressDialog = ProgressDialog.show(LoginActivity.this, "", "waiting...");
		progressDialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int id, KeyEvent event) {
				// TODO Auto-generated method stub
				try {
					dialog.dismiss();	
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		});
		client.login(mUser.phone_number, txt_pass.getText().toString());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
			login();
			break;

		default:
			break;
		}
	}
}

package com.budly.android.CustomerApp.user;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.android.CustomerApp.Common;
import com.budly.android.CustomerApp.CustomException;
import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

import org.json.JSONObject;

/**
 * Created by EdwinSL on 2/26/2015.
 */
public class RecomendationUpdateActivity extends BaseActivity implements View.OnClickListener {
    Button btn_next;
    EditText recomendation, expiry, doc_name, doc_web, phone;

    PreferenceHelper preferenceHelper;
    User mUser;

    protected void onCreate(Bundle paramBundle){
        super.onCreate(paramBundle);
        preferenceHelper = PreferenceHelper.getInstance();
        mUser = preferenceHelper.getUserInfo();

        setContentView(R.layout.user_activity_recomendation);
        this.btn_next = ((Button) findViewById(R.id.btn_next));
        this.btn_next.setOnClickListener(this);

        this.recomendation = (EditText) findViewById(R.id.txt_recomendation);
        this.expiry = (EditText) findViewById(R.id.txt_expiry);
        this.expiry.setOnClickListener(this);
        this.doc_name = (EditText) findViewById(R.id.txt_doc_name);
        this.doc_web = (EditText) findViewById(R.id.txt_doc_web);
        this.phone = (EditText) findViewById(R.id.txt_phone);

        mUser = PreferenceHelper.getInstance().getUserInfo();
        recomendation.setText(mUser.recomendation);
        expiry.setText(mUser.expiry);
        doc_name.setText(mUser.doc_name);
        doc_web.setText(mUser.doc_web);
        phone.setText(mUser.clinic_phone);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_next:
//                startActivity(new Intent(this, ProfileActivity.class));
                update();
                break;
            case R.id.txt_expiry:
                Log.e("Edwin", "Pressed date");
                manualDatePicker();
                break;
        }
    }

    void manualDatePicker() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Budly");
        alert.setMessage("Expiration date");

        // Set an EditText view to get user input
        final DatePicker input = new DatePicker(this);
        input.setCalendarViewShown(false);
        alert.setView(input);
        alert.setCancelable(false);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.i("Edwin", "Date " + input.getDayOfMonth()+"/"+input.getMonth());
                expiry.setText((input.getMonth()+1)+"/"+input.getDayOfMonth()+"/"+input.getYear());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });

        alert.show();
    }

    void update() {
        if (recomendation.getText().toString().equals("")
                || expiry.getText().toString().equals("")
                || doc_name.getText().toString().equals("")
                || doc_web.getText().toString().equals("")
                || phone.getText().toString().equals("")) {
            Toast.makeText(this, "You must enter all information", Toast.LENGTH_SHORT).show();
        } else {
            mUser.recomendation = recomendation.getText().toString();
            mUser.expiry = expiry.getText().toString();
            mUser.doc_name = doc_name.getText().toString();
            mUser.doc_web = doc_web.getText().toString();
            mUser.clinic_phone = phone.getText().toString();

            HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

                @Override
                public void onFailure(Exception error) {
                    // TODO Auto-generated method stub
                    super.onFailure(error);
                    error.printStackTrace();
                    Toast.makeText(RecomendationUpdateActivity.this, "Register failed. Please try again", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(HttpResponse httpResponse, JSONObject response) {
                    // TODO Auto-generated method stub
                    //super.onSuccess(statusCode, response);
                    try {
                        int status = response.getInt("status");

                        if (status==400){
                            if (response.getString("message").equals("register problem")){
                                throw new CustomException("problem on database");
                            }else{
                                throw new CustomException(response.getString("message"));
                            }
                        }

                        Log.i("Tuan", response.toString());
                        boolean is_update = response.getBoolean("data");
                        if(is_update && status==200) {
                            preferenceHelper.setUserInfo(mUser);
                            startActivity(new Intent(RecomendationUpdateActivity.this, ProfilePictureRecomendationActivity.class));
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(RecomendationUpdateActivity.this, "Register success", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                    }catch (CustomException e) {
                        Common.saveException(e);
                        final String msg = e.getMsg();
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(RecomendationUpdateActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }catch (Exception e) {
                        e.printStackTrace();

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(RecomendationUpdateActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            });
            try {
                JSONObject data = new JSONObject();
                data.put("id", mUser.id);
                data.put("phone_number", mUser.phone_number);
                data.put("recomendation", mUser.recomendation);
                data.put("expiration", mUser.expiry);
                data.put("doc_name", mUser.doc_name);
                data.put("doc_web", mUser.doc_web);
                data.put("clinic_phone", mUser.clinic_phone);
                client.update(data.toString());
            }catch (Exception e){e.printStackTrace();}
        }
    }

    @Override
    public void onBackPressed() {
//        register();
        super.onBackPressed();
    }
}

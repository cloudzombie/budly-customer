package com.budly.android.CustomerApp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

import org.json.JSONObject;

/**
 * Created by EdwinSL on 4/23/2015.
 */
public class PasswordResetActivity extends BaseActivity {

    Button btn_reset;
    PreferenceHelper preferenceHelper;
    User mUser;
    EditText txt_phone;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_password_reset);
        mUser = PreferenceHelper.getInstance().getUserInfo();
        btn_reset = (Button) findViewById(R.id.btn_next);
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });
        txt_phone = (EditText) findViewById(R.id.txt_phone);
        txt_phone.setText(mUser.phone_number);
    }

    void resetPassword(){
        if(txt_phone.getText().toString().equals("")) {
            Toast.makeText(this, "You must enter your phone number", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(PasswordResetActivity.this, "Reset Failed", Toast.LENGTH_SHORT).show();
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
                        final String mail = re.getString("data");
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    new AlertDialog.Builder(PasswordResetActivity.this)
                                            .setTitle("Budly")
                                            .setMessage("Please check your email\n" + mail + "\nto proceed with the password reset")
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    finish();
//                                                    startActivity(new Intent(PasswordResetActivity.this, LoginActivity.class));
                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setCancelable(false).show();
                                } catch (Exception e) { }
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
                        Toast.makeText(PasswordResetActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        progressDialog = ProgressDialog.show(PasswordResetActivity.this, "", "waiting...");
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

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
        client.passwordRest(txt_phone.getText().toString());
    }
}

package com.budly.android.CustomerApp.user;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.budly.android.CustomerApp.BaseActivity;
import com.budly.R;
import com.budly.android.CustomerApp.TransactionActivity;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.turbomanage.httpclient.HttpResponse;

import org.json.JSONObject;

/**
 * Created by chiichuy on 3/4/15.
 */
public class ReportActivity extends BaseActivity implements View.OnClickListener {

    Handler mHandler = new Handler();
    Button btn_next;
    ImageView btn_left;
    EditText content;

    int orderId;
    PreferenceHelper preferenceHelper;
    User mUser;

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        preferenceHelper = PreferenceHelper.getInstance();
        mUser = preferenceHelper.getUserInfo();
        setContentView(R.layout.user_activity_report);
        content = (EditText) findViewById(R.id.txt_problem);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);
        btn_left = (ImageView) findViewById(R.id.btn_left);
        btn_left.setOnClickListener(this);

        orderId = getIntent().getIntExtra("orderId",0);

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
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
    Runnable rHiddenDlg = new Runnable() {

        @Override
        public void run() {
            hiddenDialog();
        }
    };

    void showMyDialog(final String msg) {
        try {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if(progressDialog!=null && progressDialog.isShowing()) {
                            progressDialog.setMessage(msg);
                        } else {
                            progressDialog = ProgressDialog.show(ReportActivity.this, "", msg);
                        }
                    } catch (Exception e) { }
                    mHandler.removeCallbacks(rHiddenDlg);
                    mHandler.postDelayed(rHiddenDlg, 120000);
                }
            });
        } catch (Exception e) { e.printStackTrace(); }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                final String[] time = { "Transactions", "Update profile" };
                AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
                builder.setTitle("Budly");
                builder.setItems(time, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int positon) {
                        if(positon==0) {
                            startActivity(new Intent(ReportActivity.this, TransactionActivity.class));
                        } else {
                            startActivity(new Intent(ReportActivity.this, ProfileUpdateActivity.class));
                        }
                    }
                });
                builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();

                break;

            case R.id.btn_next:
                AlertDialog.Builder confirm = new AlertDialog.Builder(this);
                confirm.setMessage("This order will be cancelled, are you Sure?");
                confirm.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showMyDialog("Sending report");
                        sendReport();

                        Intent intent = new Intent();
                        intent.setAction(OrderConfirmedActivity.CANCELLED_ORDER);
                        intent.putExtra("order_id", orderId);
                        ReportActivity.this.sendBroadcast(intent);
                    }
                });
                confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                confirm.create().show();
                break;

            default:
                break;
        }
    }

    public void sendReport(){
        HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

            @Override
            public void onFailure(Exception error) {
                super.onFailure(error);
                runOnUiThread(new Runnable() {
                    public void run() {
                        hiddenDialog();
                    }
                });
            }

            @Override
            public void onSuccess(HttpResponse httpResponse, JSONObject re) {
                Log.i("Tuan", re.toString());
                runOnUiThread(new Runnable() {
                    public void run() {
                        hiddenDialog();
                    }
                });
                try {
                    int status = re.getInt("status");
                    if(status==200) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                finish();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        client.reportProblem(mUser.id,orderId,content.getText().toString());
    }
}

package com.budly.android.CustomerApp.user;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budly.BaseActivity;
import com.budly.Common;
import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.driver.StatusActivity;
import com.google.android.gcm.GCMRegistrar;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.budly.android.CustomerApp.td.widget.MyRecorder;
import com.budly.android.CustomerApp.td.widget.ProgressButton;
import com.turbomanage.httpclient.HttpResponse;

public class OrderConfirmedActivity extends BaseActivity implements OnClickListener{
	
	ProgressButton progressButton;
	int currentProgress = 0;
	Button btn_next;
	TextView txt_min;
	boolean isOk = false;
	TextView unit; 
	
	JSONObject driver = null;
	int order_id = 0;
	int time = 0;
	int time_tmp = 0;
	public static int driver_id = 0;
	
	final int TIME_STEP = 100;
	
	PreferenceHelper preferenceHelper;
	User mUser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerGCM();
		preferenceHelper = new PreferenceHelper(this);
		mUser = preferenceHelper.getUserInfo();
		
		setContentView(R.layout.user_activity_order_confirmed);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_next.setOnClickListener(this);
		progressButton = (ProgressButton) findViewById(R.id.pin_progress_1);
		
		progressButton.setProgress(currentProgress);
		
		txt_min = (TextView) findViewById(R.id.txt_min);
		unit = (TextView) findViewById(R.id.unit);
		driver_id = 0;
		try {
			driver = new JSONObject(getIntent().getStringExtra("driver"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			order_id = getIntent().getIntExtra("order_id", -1);
		} catch (Exception e) { }
		
		if(driver==null) {
			finish();
			return;
		}
		try {
			time = driver.getInt("estimate_time");
			time_tmp = time*60000;
			progressButton.setMax(time*60000);
			driver_id = driver.getInt("id");
			txt_min.setText(String.valueOf(time));
			unit.setText("Minutes");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					int n = (int) Math.round(time*60000/TIME_STEP+0.5);
					for (int i = 1; i <= n; i++) {
						if(!isOk) {
							try {
								Thread.sleep(TIME_STEP);	
							} catch (Exception e) { }
							currentProgress+=TIME_STEP;
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									try {
										runOnUiThread(new Runnable() {
											
											@Override
											public void run() {
												try {
													time_tmp -= TIME_STEP;
													if(time_tmp<0) time_tmp = 0;
													if(time_tmp<60000) {
														txt_min.setText(""+(int)(time_tmp/1000));
														unit.setText("Seconds");
													} else {
														unit.setText("Minutes");
														txt_min.setText(String.valueOf((int)(time_tmp*1f/60000+0.5f)));	
													}
												} catch (Exception e) { }
											}
										});
									} catch (Exception e) { }
									
									if(currentProgress>=time*60000) currentProgress = time*60000;
									progressButton.setProgress(currentProgress);
								}
							});
						} else {
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		btnRecoder = (ImageView) findViewById(R.id.btn_recorder);
		setUpRecord();
		registerBroadcast();
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
			confirm();
			break;
		default:
			break;
		}
	}
	
	void confirm() {
		progressDialog = ProgressDialog.show(this, "", "Please wait...");
		progressDialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				try {
					dialog.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		});
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				runOnUiThread(new Runnable() {
					
					@Override
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
						finish();
						startActivity(new Intent(OrderConfirmedActivity.this, RateActivity.class));
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(OrderConfirmedActivity.this, "Have error while send confirm to server. Please try again.", Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onFailure(Exception e) {
				// TODO Auto-generated method stub
				super.onFailure(e);
				Toast.makeText(OrderConfirmedActivity.this, "Have error while send confirm to server. Please try again.", Toast.LENGTH_SHORT).show();
				try {
					progressDialog.dismiss();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			
		});
		client.acceptConfirm(mUser.id, order_id);
	}
	
	ImageView btnRecoder;
	String fileName = "";
	void setUpRecord() {
		btnRecoder.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				try {
					Intent intent = new Intent(Intent.ACTION_CALL);
					intent.setData(Uri.parse("tel:" + Common.MOBILE_PHONE));
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(OrderConfirmedActivity.this, "Try to call failed. Please try again", Toast.LENGTH_SHORT).show();
				}
			}
		});
		/*
		btnRecoder.setOnTouchListener(new OnTouchListener() {
			private long startTimeRecord = 0;
			private long lastActionDown = 0;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					long currentTime = System.currentTimeMillis();
					if((currentTime-lastActionDown)<400) return true;
					lastActionDown = currentTime;
					startTimeRecord = System.currentTimeMillis();
					
					Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					// Vibrate for 100 milliseconds
					vib.vibrate(80);
					
					btnRecoder.setImageResource(R.drawable.recoder_pressed);
																									// popup
					SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
					fileName = Common.SDCARD_AUDIO + "audio_" + mUser.id + "_" + timeStampFormat.format(new Date()) + ".m4a";
					new Thread(new Runnable() {
						public void run() {
							// make sure the SD card is present for the recording
							if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
								try {
									startRecord(fileName);
									startTimeRecord = System.currentTimeMillis();
									if (mRecorder.getState() == MyRecorder.State.ERROR) {
										fileName = null;
										Log.e("Tuan", "Ghi am bị lỗi");
									}
								} catch (Exception e) { startTimeRecord = System.currentTimeMillis(); fileName = null; }
							}
						}
					}).start();
					return true;

				case MotionEvent.ACTION_MOVE:
					return true;
				case MotionEvent.ACTION_UP:
					btnRecoder.setImageResource(R.drawable.recoder_normal);
					Log.i("", "Reset");
					long currentTimeUp = System.currentTimeMillis();
					if((currentTimeUp-lastActionDown)<400) return true;
					lastActionDown = currentTimeUp;
					if(mRecorder==null || !mRecorder.isRecording()) return false;
					if ((System.currentTimeMillis() - this.startTimeRecord) > 1000) {
						mHandler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								new Thread(new Runnable() {
									
									@Override
									public void run() {
										try {
											if(fileName==null) return;
											try {
												Thread.sleep(300);
											} catch (InterruptedException e1) { }
											try {
												stopRecord();
											} catch (Exception e) { e.printStackTrace(); }
											try {
												Thread.sleep(300);
											} catch (InterruptedException e1) { }
											
											LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
											data.put("from_id", String.valueOf(mUser.id));
											data.put("to_id", String.valueOf(driver_id));
											String res = UploadFile.uploadFile("http://mybudly.com/DAPP/api/user/sendVoice", fileName, getApplicationContext(), data);
											Log.i("Tuan", res);
										} catch (Exception e) { }
									}
								}).start();
							}
						}, 100);
					} else {
						try {
							stopRecord();
						} catch (Exception e) { e.printStackTrace(); }
					}
					break;
				}
				return true;
			}
		});
		*/
	}
	
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
	    .setTitle("Budly")
	    .setMessage("Are you sure you want to cancel this order?")
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	finish();
	        }
	     })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	dialog.dismiss();
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_alert)
	     .show();
	}
	
	Handler mHandler = new Handler();
	
	MyRecorder mRecorder;
	void startRecord(String output) {
		try {
			mRecorder = new MyRecorder(0, 0, 0, 0);
			mRecorder.setOutputFile(output);
			mRecorder.prepare();
			mRecorder.start(); // Recording is now started
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void stopRecord() {
		try {
			if(mRecorder!=null) {
				mRecorder.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void registerGCM() {
		final HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback());
//		if (Build.FINGERPRINT.startsWith("generic")) {
//		    // running on an emulator
//			Log.e("Tuan", "Emulator======================");
//		} else {
		    // running on a device
			// Make sure the device has the proper dependencies.
			GCMRegistrar.checkDevice(this);

			// Make sure the manifest was properly set - comment out this line
			// while developing the app, then uncomment it when it's ready.
			GCMRegistrar.checkManifest(this);
	        
			String regId = GCMRegistrar.getRegistrationId(this);
			Log.e("Tuan", "GCMID="+regId);

			// Check if regid already presents
			if (regId.equals("")) {
				GCMRegistrar.register(this, Common.PROJECT_NUMBER);
			}
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					String regId="";
					
					while (regId.equals("")) {
						regId = GCMRegistrar.getRegistrationId(OrderConfirmedActivity.this);
						Log.i("Tuan", "Registered with id "+regId);
						preferenceHelper.setGCMID(regId);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					client.updateDevice(mUser.id, regId);
				}
			}).start();
				
//		}
	}
	
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(REQUEST_CONFIRM)) {
				replyConfirm();
			} else if(action.equals(ACCEPT_CONFIRM)) {
				hiddenDialog();
				showToast("Request confirm has accepted");
				finish();
				try {
					startActivity(new Intent(getApplicationContext(), RateActivity.class));	
				} catch (Exception e) { }
			} else if(action.equals(DENY_CONFIRM)) {
				hiddenDialog();
				showToast("Request confirm has deny");
			}
		}
	};
	
	
	final String REQUEST_CONFIRM = "request_confirm";
	final String ACCEPT_CONFIRM = "accept_confirm";
	final String DENY_CONFIRM = "deny_confirm";
	
	void registerBroadcast() {
		IntentFilter intent = new IntentFilter();
		intent.addAction(REQUEST_CONFIRM);
		//intent.addAction(ACCEPT_CONFIRM);
		//intent.addAction(DENY_CONFIRM);
		registerReceiver(mReceiver, intent);
	}
	
	void unRegisterBroadcast() {
		unregisterReceiver(mReceiver);
	}
	

	void replyConfirm() {
		try {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						new AlertDialog.Builder(OrderConfirmedActivity.this)
					    .setTitle("Budly")
					    .setMessage("Are you sure you want to confirm to complete this order?")
					    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					        	acceptConfirm();
					        	dialog.dismiss();
					        }
					     })
					    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) {
					        	denyConfirm();
					        	dialog.dismiss();
					        }
					     })
					    .setIcon(android.R.drawable.ic_dialog_alert)
					    .setCancelable(false).show();
					} catch (Exception e) { }
				}
			});
		} catch (Exception e) { }
	}
	
	ProgressDialog progressDialog;
	void requestConfirm() {
		showDialog("Sending request...");
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				try {
					int status = re.getInt("status");
					if(status==200) {
						showDialog("Waiting accept...");
						return;
					}
				} catch (Exception e) { }
				hiddenDialog();
				showToast("Request failed");
			}

			@Override
			public void onFailure(Exception e) {
				hiddenDialog();
				showToast("Request failed");
				super.onFailure(e);
			}
		});
		
		client.requestConfirm(preferenceHelper.getUserInfo().id, order_id);
	}
	
	void acceptConfirm() {
		showDialog("Sending accept request...");
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				try {
					int status = re.getInt("status");
					if(status==200) {
						hiddenDialog();
						finish();
						try {
							startActivity(new Intent(getApplicationContext(), RateActivity.class));	
						} catch (Exception e) { }
						return;
					}
				} catch (Exception e) { }
				hiddenDialog();
				showToast("Request failed");
			}

			@Override
			public void onFailure(Exception e) {
				hiddenDialog();
				showToast("Request failed");
				super.onFailure(e);
			}
			
		});
		client.acceptConfirm(preferenceHelper.getUserInfo().id, order_id);
	}
	
	void denyConfirm() {
		showDialog("Sending deny request...");
		HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){

			@Override
			public void onSuccess(HttpResponse httpResponse, JSONObject re) {
				try {
					int status = re.getInt("status");
					if(status==200) {
						hiddenDialog();
						return;
					}
				} catch (Exception e) { }
				hiddenDialog();
				showToast("Request failed");
			}

			@Override
			public void onFailure(Exception e) {
				hiddenDialog();
				super.onFailure(e);
			}
			
		});
		client.denyConfirm(preferenceHelper.getUserInfo().id, order_id);
	}
	
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
							progressDialog = ProgressDialog.show(OrderConfirmedActivity.this, "", msg);
						}
					} catch (Exception e) { }
				}
			});
		} catch (Exception e) { }
	}
	
	void showToast(final String msg) {
		try {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
					} catch (Exception e) { }
				}
			});
		} catch (Exception e) { }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatusActivity.CURRENT_STATUS = StatusActivity.STATUS_ORERCONFIRM;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unRegisterBroadcast();
	}
}

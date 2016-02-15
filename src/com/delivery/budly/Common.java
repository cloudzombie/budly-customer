package com.budly;

import java.io.IOException;

import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.budly.android.CustomerApp.driver.DriverConfirmActivity;
import com.budly.android.CustomerApp.driver.HasOrderActivity;
import com.budly.android.CustomerApp.driver.OrderLostActivity;
import com.budly.android.CustomerApp.driver.StatusActivity;
import com.budly.android.CustomerApp.td.widget.MyPlayer;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Common {
	public static final String PROJECT_NUMBER = "1078120714945"; //Dung cho google cloud msg 
	final static public String SDCARD_AUDIO			= Environment.getExternalStorageDirectory() + "/budly/audio/";
	public static String MOBILE_PHONE = "";
	public static int DISTANCE_DEFAULT = 50;
	public static boolean ON_SCREEN = true;
	
	//Phan tich msg tu server socket hoac google cloud message
	public static void onReceiveMSG(String message, Context context) {
		try  {
			try {
				PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		         boolean isScreenOn = pm.isScreenOn();
		         Log.e("screen on.................................", ""+isScreenOn);
				if (isScreenOn == false) {
					WakeLock wl = pm.newWakeLock(
							PowerManager.FULL_WAKE_LOCK
							| PowerManager.ACQUIRE_CAUSES_WAKEUP
							| PowerManager.ON_AFTER_RELEASE, "MyLock");
					wl.acquire(10000);
					WakeLock wl_cpu = pm.newWakeLock(
							PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");
					wl_cpu.acquire(10000);
				}
			} catch (Exception e2) { }
			JSONObject jso = new JSONObject(message);
			String cmd = jso.getString("cmd");
			JSONObject data = jso.getJSONObject("data");
			playSound(context);
			if (cmd.equals("has_order")) {
				showNotification(context, "You have an order!");
				int customer_id = data.getInt("customer_id");
				int order_id = data.getInt("order_id");
				if(StatusActivity.CURRENT_STATUS == StatusActivity.STATUS_ORDERHAS || StatusActivity.CURRENT_STATUS == StatusActivity.STATUS_ORDERSUBMIT) return;
				Intent intent = new Intent(context, HasOrderActivity.class);
				intent.putExtra("customer_id", customer_id);
				intent.putExtra("order_id", order_id);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			} else if(cmd.equals("accept_order")) {
				showNotification(context, "Order has accepted!");
				int customer_id = data.getInt("customer_id");
				int order_id = data.getInt("order_id");
				int estimate_time = data.getInt("estimate_time");
				Intent intent = new Intent(context, DriverConfirmActivity.class);
				intent.putExtra("customer_id", customer_id);
				intent.putExtra("order_id", order_id);
				intent.putExtra("estimate_time", estimate_time);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			} else if(cmd.equals("deny_order")) {
				showNotification(context, "Order has denied!");
				int customer_id = data.getInt("customer_id");
				int order_id = data.getInt("order_id");
				if(StatusActivity.CURRENT_STATUS == StatusActivity.STATUS_ORERLOST) return;
				Intent intent = new Intent(context, OrderLostActivity.class);
				intent.putExtra("customer_id", customer_id);
				intent.putExtra("order_id", order_id);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			} else if(cmd.equals("has_voice")) {
				int from_id = data.getInt("from");
				final String file_url = data.getString("url");
//				if(StatusActivity.CURRENT_STATUS == StatusActivity.STATUS_ORERLOST) return;
//				Intent intent = new Intent(context, OrderLostActivity.class);
//				intent.putExtra("from_id", from_id);
//				intent.putExtra("file_url", file_url);
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				context.startActivity(intent);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						MyPlayer player = new MyPlayer();
						try {
							player.mPlay(file_url);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
			} else if(cmd.equals("request_confirm")) {
				showNotification(context, "You have a request!");
				int from = data.getInt("from");
				int order_id = data.getInt("order_id");
				if(StatusActivity.CURRENT_STATUS != StatusActivity.STATUS_ORERCONFIRM) return;
				Intent intent = new Intent();
				intent.setAction(DriverConfirmActivity.REQUEST_CONFIRM);
				intent.putExtra("from", from);
				intent.putExtra("order_id", order_id);
				context.sendBroadcast(intent);
			} else if(cmd.equals("accept_confirm")) {
				showNotification(context, "Order has accepted!");
				int from = data.getInt("from");
				int order_id = data.getInt("order_id");
				if(StatusActivity.CURRENT_STATUS != StatusActivity.STATUS_ORERCONFIRM) return;
				Intent intent = new Intent();
				intent.setAction(DriverConfirmActivity.ACCEPT_CONFIRM);
				intent.putExtra("from", from);
				intent.putExtra("order_id", order_id);
				context.sendBroadcast(intent);
			} else if(cmd.equals("deny_confirm")) {
				showNotification(context, "Order has denied!");
				int from = data.getInt("from");
				int order_id = data.getInt("order_id");
				if(StatusActivity.CURRENT_STATUS != StatusActivity.STATUS_ORERCONFIRM) return;
				Intent intent = new Intent();
				intent.setAction(DriverConfirmActivity.DENY_CONFIRM);
				intent.putExtra("from", from);
				intent.putExtra("order_id", order_id);
				context.sendBroadcast(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void playSound(Context context) {
		try {
//			MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.done);
//			mPlayer.setVolume(80, 80);
//			mPlayer.start();
		} catch (Exception e) {
		 e.printStackTrace();
		}
	}
	
	
    @SuppressWarnings({ "unused", "deprecation" })
   	private static void showNotification(Context context, String msg) {
    	if(Common.ON_SCREEN) return;
    	NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    	
    	Notification notification = new Notification(R.drawable.ic_launcher, "Budly", System.currentTimeMillis());

       
       // The PendingIntent to launch our activity if the user selects this notification
       Intent i = new Intent();
       PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

       // Set the info for the views that show in the notification panel.
       // msg.length()>15 ? msg : msg.substring(0, 15);
       notification.setLatestEventInfo(context, "Budly",
                      						msg, 
                      						contentIntent);
       
       notification.defaults |= Notification.DEFAULT_SOUND;
       
       PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
       boolean isScreenOn = pm.isScreenOn();
       Log.e("screen on.................................", ""+isScreenOn);
       if(isScreenOn==false) {
	       WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock"); 
	       wl.acquire(10000);
	       WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
          wl_cpu.acquire(10000);
       }
       
       //TODO: it can be improved, for instance message coming from same user may be concatenated
       // next version
       
       // We use a layout id because it is a unique number.  We use it later to cancel.
       notification.flags = Notification.FLAG_ONGOING_EVENT;
       notification.flags = Notification.FLAG_AUTO_CANCEL;
       notificationManager.notify(0x12345678, notification);
    }
}

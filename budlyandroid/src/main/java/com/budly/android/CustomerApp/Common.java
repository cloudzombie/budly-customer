package com.budly.android.CustomerApp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.WindowManager;

import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.budly.android.CustomerApp.driver.ActiveOrdersActivity;
import com.budly.android.CustomerApp.driver.DriverConfirmActivity;
import com.budly.android.CustomerApp.driver.DriverInterface;
import com.budly.android.CustomerApp.driver.HasOrderActivity;
import com.budly.android.CustomerApp.driver.OrderLostActivity;
import com.budly.android.CustomerApp.driver.StatusActivity;
import com.budly.android.CustomerApp.driver.SubmitOrderActivity;
import com.budly.android.CustomerApp.user.OrderConfirmedActivity;
import com.budly.android.CustomerApp.user.WaitDriversActivity;
import com.budly.android.CustomerApp.td.http.HttpBasicClientHelper;
import com.budly.android.CustomerApp.td.http.MyJsonAsyncCallback;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.budly.android.CustomerApp.td.widget.MyPlayer;
import com.turbomanage.httpclient.HttpResponse;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Common {
	public static final String PROJECT_NUMBER = "5534043021"; //Dung cho google cloud msg
	final static public String SDCARD_AUDIO			= Environment.getExternalStorageDirectory() + "/budly/audio/";
	public static String MOBILE_PHONE = "";
	public static int DISTANCE_DEFAULT = 50;
	public static boolean ON_SCREEN = true;
    public static int ordersCount = 0;

    public static SoundPool notifications;
    public static int receiveOrder;

	//Phan tich msg tu server socket hoac google cloud message
	public static void onReceiveMSG(String message, Context context, boolean automatic) {
		try  {
//			try {
//                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
//                wl.acquire();
//			} catch (Exception e2) { }
			JSONObject jso = new JSONObject(message);


            String id = "";
            if (automatic) {
//                if (jso.getString("cmd").equals("has_order") || jso.getString("cmd").equals("accept_order") || jso.getString("cmd").equals("deny_order")){
//                    id = new PreferenceHelper(context).addNotification(message,1);
                    id = PreferenceHelper.getInstance().addNotification(message,1);
//                }
            }else{
                id = jso.getString("id");
                jso = new JSONObject(jso.getString("data"));
            }

            String cmd = jso.getString("cmd");
            JSONObject data = jso.getJSONObject("data");

            if (dateTimeDifferenceGeneric(data.getString("time"),DATE_NOTIFICATION_MASK) > 2){
                return;
            }

			if (cmd.equals("has_order")) {
                playSound(context);
                Log.i("notificaiton","has order");

				//showNotification(context, "You have an order!");
				int customer_id = data.getInt("customer_id");
				int order_id = data.getInt("order_id");
				//if(StatusActivity.CURRENT_STATUS == StatusActivity.STATUS_ORDERHAS || StatusActivity.CURRENT_STATUS == StatusActivity.STATUS_ORDERSUBMIT) return;
				Intent intent = new Intent(context, HasOrderActivity.class);
				intent.putExtra("customer_id", customer_id);
				intent.putExtra("order_id", order_id);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("notify_id",id);
                notify(context,intent,"You have an order!");
                context.startActivity(intent);
			} else if(cmd.equals("accept_order")) {
				//showNotification(context,"Order has accepted!" );


				int customer_id = data.getInt("customer_id");
				int order_id = data.getInt("order_id");
				int estimate_time = data.getInt("estimate_time");
                String start_time = data.getString("start_time");
                String addressDestination = data.getString("address");
				Intent intent = new Intent(context, DriverConfirmActivity.class);
				intent.putExtra("customer_id", customer_id);
				intent.putExtra("order_id", order_id);
                intent.putExtra("notify_id",id);
				intent.putExtra("estimate_time", estimate_time);
                intent.putExtra("address",addressDestination);
                intent.putExtra("start_time",start_time);
                intent.putExtra("notification",true);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                notify(context,intent,"Order has accepted!");
				context.startActivity(intent);
			} else if(cmd.equals("deny_order")) {
//				showNotification(context, "Order has denied!");
				int customer_id = data.getInt("customer_id");
				int order_id = data.getInt("order_id");
				//if(StatusActivity.CURRENT_STATUS == StatusActivity.STATUS_ORERLOST) return;
				Intent intent = new Intent(context, OrderLostActivity.class);
				intent.putExtra("customer_id", customer_id);
				intent.putExtra("order_id", order_id);
                intent.putExtra("notify_id",id);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                notify(context,intent,"Order has denied!");
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
//				showNotification(context, "You have a request!");


				int from = data.getInt("from");
				int order_id = data.getInt("order_id");
				//if(StatusActivity.CURRENT_STATUS != StatusActivity.STATUS_ORERCONFIRM) return;
				Intent intent = new Intent();
				intent.setAction(DriverConfirmActivity.REQUEST_CONFIRM);
				intent.putExtra("from", from);
				intent.putExtra("order_id", order_id);
                intent.putExtra("notify_id",id);
                notify(context,intent,"You have a request!");
                PreferenceHelper.getInstance().removeNotification(id);
				context.sendBroadcast(intent);
			} else if(cmd.equals("accept_confirm")) {
//				showNotification(context, "Order has accepted!");

                int from = data.getInt("from");
				int order_id = data.getInt("order_id");
				//if(StatusActivity.CURRENT_STATUS != StatusActivity.STATUS_ORERCONFIRM) return;
				Intent intent = new Intent();
				intent.setAction(DriverConfirmActivity.ACCEPT_CONFIRM);
				intent.putExtra("from", from);
				intent.putExtra("order_id", order_id);
                intent.putExtra("notify_id",id);
                notify(context,intent,"Order has accepted!");
                PreferenceHelper.getInstance().removeNotification(id);
				context.sendBroadcast(intent);
			} else if(cmd.equals("deny_confirm")) {
//				showNotification(context, "Order has denied!");

                int from = data.getInt("from");
				int order_id = data.getInt("order_id");
				if(StatusActivity.CURRENT_STATUS != StatusActivity.STATUS_ORERCONFIRM) return;
				Intent intent = new Intent();
				intent.setAction(DriverConfirmActivity.DENY_CONFIRM);
				intent.putExtra("from", from);
				intent.putExtra("order_id", order_id);
                intent.putExtra("notify_id",id);
                notify(context,intent,"Order has denied!");
                PreferenceHelper.getInstance().removeNotification(id);
				context.sendBroadcast(intent);
			} else if(cmd.equals("drivers_list")) {
//                showNotification(context, "Driver responded");

//                int from = data.getInt("from");
                int order_id = data.getInt("order_id");
//                if(StatusActivity.CURRENT_STATUS != StatusActivity.STATUS_ORERCONFIRM) return;
                Intent intent = new Intent();
                intent.setAction(WaitDriversActivity.DRIVER_RESPONDED);
//                intent.putExtra("from", from);
                intent.putExtra("order_id", order_id);
                intent.putExtra("notify_id",id);

                notify(context,intent,"Driver responded");
                PreferenceHelper.getInstance().removeNotification(id);
                context.sendBroadcast(intent);
            } else if(cmd.equals("cancel_order")){
                int order_id = data.getInt("order_id");
                Intent intent = new Intent();
                if(PreferenceHelper.getInstance().getUserInfo().type.equals(User.CUSTOMER)){
                    intent.setAction(OrderConfirmedActivity.CANCELLED_ORDER);
//                    notify(context,intent,"Driver cancelled");
                }else if(PreferenceHelper.getInstance().getUserInfo().type.equals(User.DRIVER)) {
                    intent.setAction(DriverConfirmActivity.CANCELLED_ORDER);
                    intent.setAction(ActiveOrdersActivity.CANCELLED_ORDER);
//                    notify(context,intent,"Customer cancelled");
                }
                intent.putExtra("order_id", order_id);

                PreferenceHelper.getInstance().removeNotification(id);
                context.sendBroadcast(intent);
            } else if(cmd.equals("processed_order")){
                int order_id = data.getInt("order_id");
                Intent intent = new Intent();
                intent.setAction(HasOrderActivity.PROCESSED_ORDER);
                intent.setAction(SubmitOrderActivity.PROCESSED_ORDER);
                intent.putExtra("order_id", order_id);
                PreferenceHelper.getInstance().removeNotification(id);
                context.sendBroadcast(intent);
            } else if(cmd.equals("force_confirm")){
                int order_id = data.getInt("order_id");
                Intent intent =new Intent();
                intent.setAction(OrderConfirmedActivity.ACCEPT_CONFIRM);
                intent.putExtra("order_id", order_id);
                PreferenceHelper.getInstance().removeNotification(id);
                context.sendBroadcast(intent);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void turnOnScreen(Activity a){
        a.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }
    public static void checkEvents(Context context){
        JSONArray json = PreferenceHelper.getInstance().getNotifications();
        if (json.length() > 0) {
            try {
                Common.onReceiveMSG(json.getJSONObject(0).toString(), context, false);
            }catch (Exception e){}
        }
    }

    public static void initCommon(Context context){
        notifications = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        receiveOrder = notifications.load(context, R.raw.done,1);
    }

	public static void playSound(Context context) {
        if(notifications == null){
            notifications = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
            receiveOrder = notifications.load(context,R.raw.done,1);
        }
        notifications.play(receiveOrder, 1.0f, 1.0f, 1, 0, 1);
	}

    public static void notify(Context context,Intent intent,String msg){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("My budly!")
                        .setContentText(msg);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        if (intent != null){
            stackBuilder.addNextIntent(intent);
        }
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = mBuilder.build();
        //n.defaults |= Notification.DEFAULT_SOUND;
        n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(0x12345678, n);
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
       Log.e("screen on....", ""+isScreenOn);
       if(isScreenOn==false) {
	       PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
	       wl.acquire(10000);
	       PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
          wl_cpu.acquire(10000);
       }
       
       //TODO: it can be improved, for instance message coming from same user may be concatenated
       // next version
       
       // We use a layout id because it is a unique number.  We use it later to cancel.
       notification.flags = Notification.FLAG_ONGOING_EVENT;
       notification.flags = Notification.FLAG_AUTO_CANCEL;
       notificationManager.notify(0x12345678, notification);
    }
    public static int dateDifferenceLocal(String start_time){

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mma");



        Date staart = new Date();

        try {
            staart = sdf.parse(start_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date end = new Date();

        long diff = end.getTime() - staart.getTime();

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000);// % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);


//        Log.w("chuy", hoy.toString() + " today");
        Log.w("chuy", staart.toString() + " start");
        Log.w("chuy", diffMinutes + " minutes");

        return Math.round(diffMinutes);
    }

    public static final String DATE_NOTIFICATION_MASK = "yyyy-MM-dd kk:mm:ss";

    public static int dateTimeDifferenceGeneric(String start_time,String mask){
        SimpleDateFormat sdf = new SimpleDateFormat(mask);

        DateFormat df = DateFormat.getDateTimeInstance();
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String gmtTime = df.format(new Date());


        SimpleDateFormat localsdf = new SimpleDateFormat("MMM d, yyyy hh:mm:ss aaa");
        SimpleDateFormat localsdf24 = new SimpleDateFormat("MMM d, yyyy hh:mm:ss");
        Date staart = new Date();
        Date end = new Date();

        try {
            staart = sdf.parse(start_time);
            end = localsdf.parse(gmtTime);
        } catch (ParseException e) {
            try {
                end = localsdf24.parse(gmtTime);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
//        Date hoy = new Date();
        long diff = end.getTime() - staart.getTime();
        long diffMinutes = diff / (60 * 1000);// % 60;

        Log.e("chuy time:",String.valueOf(Math.round(diffMinutes)));
        return Math.round(diffMinutes);
    }

    public static int dateDifference(String start_time){

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mma");
//        sdf.setTimeZone(TimeZone.getTimeZone("PST"));

        //SimpleDateFormat utc = new SimpleDateFormat(staart);

        //Mar 9, 2015 5:03:48 PM utc
        DateFormat df = DateFormat.getDateTimeInstance();
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String gmtTime = df.format(new Date());


        SimpleDateFormat localsdf = new SimpleDateFormat("MMM d, yyyy hh:mm:ss aaa");
        SimpleDateFormat localsdf24 = new SimpleDateFormat("MMM d, yyyy hh:mm:ss");
        Date staart = new Date();
        Date end = new Date();

        try {
            staart = sdf.parse(start_time);
            end = localsdf.parse(gmtTime);

        } catch (ParseException e) {

            try {
                end = localsdf24.parse(gmtTime);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }

        Log.w("chuy", end.toString() + " utc");
//        Date hoy = new Date();
        long diff = end.getTime() - staart.getTime();

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000);// % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);


//        Log.w("chuy", hoy.toString() + " today");
        Log.w("chuy", staart.toString() + " start");
        Log.w("chuy", diffMinutes + " minutes");

        return Math.round(diffMinutes);
    }

    public static String formatUTCToLocal(String datetime) {
        String returnTimeDate = "";
        DateTime dtUTC = null;
        DateTimeZone timezone = DateTimeZone.getDefault();
        DateTimeFormatter formatDT = DateTimeFormat.forPattern("MMM dd, yyyy  hh:mma");

        try {
            DateTime dateDateTime1 = formatDT.parseDateTime(datetime);
            DateTime now = new DateTime();
            DateTime nowUTC = new LocalDateTime(now).toDateTime(DateTimeZone.UTC);
            long instant = now.getMillis();
            long instantUTC = nowUTC.getMillis();
            long offset = instantUTC - instant;

            //convert to local time
            dtUTC = dateDateTime1.withZoneRetainFields(DateTimeZone.UTC);
            //dtUTC = dateDateTime1.toDateTime(timezone);
            dtUTC = dtUTC.plusMillis((int) offset);

            returnTimeDate = dtUTC.toString(formatDT);
        }catch (Exception e) {
            returnTimeDate = "null";
            e.printStackTrace();
        }
        return returnTimeDate;
    }

    public static void setScreenFlag(Activity w){
        if(ordersCount > 0){
            w.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    static String count="";
    public static void getOrdersCount(int userId, final DriverInterface menu){

        HttpBasicClientHelper client = new HttpBasicClientHelper(new MyJsonAsyncCallback(){
            @Override
            public void onSuccess(HttpResponse httpResponse, JSONObject re) {
                try{
                    count = re.getJSONObject("data").getString("count");
                    Common.ordersCount = Integer.parseInt(count);
                    menu.updateOrdersTextView(count);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                // TODO Auto-generated method stub
                super.onFailure(e);
            }
        });

        client.getTotalOrdersByDriverID(userId);
    }


    public static void saveException(Throwable exception) {
        final String LINE_SEPARATOR = "\n";
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("************ CAUSE OF ERROR ************\n\n");
        errorReport.append(stackTrace.toString());

        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
        errorReport.append("Brand: ");
        errorReport.append(Build.BRAND);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Device: ");
        errorReport.append(Build.DEVICE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Model: ");
        errorReport.append(Build.MODEL);
        errorReport.append(LINE_SEPARATOR);

        File root = android.os.Environment.getExternalStorageDirectory();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(
                new Date());

        File dir = new File(root.getAbsolutePath() + "/budly/log");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "error_log.txt");

        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            buf.append(currentDateTimeString + ":" + errorReport.toString());
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void hideNotification(Context context){
        NotificationManager n = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        n.cancel(0x12345678);
    }
}

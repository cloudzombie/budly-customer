package com.budly.android.CustomerApp;

import android.util.Log;

import com.budly.android.CustomerApp.driver.Progress;

import java.util.HashMap;

/**
 * Created by chiichuy on 1/29/15.
 */
public class Globals extends Thread {

//    static int time,time_tmp,currentProgress;
//    static boolean isOk = false;

    public static final String VERSION ="1.0.2";
    public static final int PIC_X = 500;
    public static final int PIC_Y = 500;

    static HashMap<String,Integer> map = new HashMap<String, Integer>();
    private static void addMapItem(String id,String key,int value){
        map.put(id+"."+key,value);
    }
    private static int getMapItem(String id,String key){
        return map.get(id+"."+key);
    }
    private static void removeMapItems(String id){
        map.remove(id+"time");
        map.remove(id+"time_tmp");
        map.remove(id+"isOk");
        map.remove(id+"currentProgress");
        map.remove(id+"n");
        map.remove(id+"m");
        map.remove(id+"i");
    }
    public static void stopProgress(String id){
        addMapItem(id,"isOk",1);
    }


    public static void init(String guid,int estimate_time,String start_date,int orderID,Progress progress){
        try {
            addMapItem(guid,"time", estimate_time);
            addMapItem(guid,"time_tmp",getMapItem(guid,"time")*60000);
        } catch (Exception e) { }
        addMapItem(guid,"isOk",0);
        Log.e("chuy", orderID + " order");
        addMapItem(guid, "currentProgress", 60000 * Common.dateDifference(start_date));

        if(getMapItem(guid,"currentProgress")>=getMapItem(guid,"time")*60000){
            addMapItem(guid,"currentProgress", getMapItem(guid,"time")*60000);
        }

        progress.setProgress(getMapItem(guid,"currentProgress"),getMapItem(guid,"time")*60000);


        int n = (int) Math.round(getMapItem(guid,"time")*60000/TIME_STEP+0.5);
        int m = (int) Math.round(getMapItem(guid,"currentProgress")/TIME_STEP+0.5);



        if (m>n){
            m=n;
        }

        addMapItem(guid,"n",n);
        addMapItem(guid,"m",m);
        addMapItem(guid,"i",m);
    }
    final static int TIME_STEP = 100;
    public Thread setProgressTimer(final int orderID,String id,int estimate_time,String start_date,final Progress progress){

        final String guid = id;
        init(guid,estimate_time,start_date,orderID,progress);


        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    addMapItem(guid,"time_tmp",getMapItem(guid,"time_tmp")-(TIME_STEP*getMapItem(guid,"m")));

                    for (int i = getMapItem(guid,"m") ; i <= getMapItem(guid,"n"); i++) {

                        if(getMapItem(guid,"isOk") != 1) {
                            try {
                                Thread.sleep(TIME_STEP);
                            } catch (Exception e) { }
                            addMapItem(guid,"currentProgress",getMapItem(guid,"currentProgress")+TIME_STEP);
                            addMapItem(guid,"time_tmp",getMapItem(guid,"time_tmp")-(TIME_STEP));
                            if(getMapItem(guid,"time_tmp")<0){
                                addMapItem(guid,"time_tmp", 0);
                            }
                            if(getMapItem(guid,"time_tmp")<60000) {
                                progress.setProgressText("" + (int) (getMapItem(guid, "time_tmp") / 1000), "Seconds");
                            } else {
                                progress.setProgressText(String.valueOf((int) (getMapItem(guid, "time_tmp") * 1f / 60000 + 0.5f)), "Minutes");
                            }

                            if(getMapItem(guid,"currentProgress")>=getMapItem(guid,"time")*60000){
                                addMapItem(guid,"currentProgress",getMapItem(guid,"time")*60000);
                            }

                            if(getMapItem(guid,"currentProgress") > 99*60000){
                                progress.setProgress(getMapItem(guid, "currentProgress")/60000, getMapItem(guid,"time"));
                            }else{
                                progress.setProgress(getMapItem(guid, "currentProgress"), getMapItem(guid,"time")*60000);
                            }

                        } else {
                            break;
                        }

                    }

                    removeMapItems(guid);
                    progress.endProgress();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        return t;
    }
}

package com.budly.android.CustomerApp.driver;

/**
 * Created by chiichuy on 1/30/15.
 */
public interface Progress {

    public void setProgressText(String minutes,String text);
    public void setProgress(int currentProgress,int maxProgress);
    public void endProgress();
}

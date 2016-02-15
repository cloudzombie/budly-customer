package com.budly.android.CustomerApp;

/**
 * Created by chiichuy on 3/18/15.
 */
public class CustomException extends Exception {

    private final String msg;

    public CustomException(String msg){
        super(msg);
        this.msg = msg;
    }

    public String getMsg(){
        return msg;
    }
}

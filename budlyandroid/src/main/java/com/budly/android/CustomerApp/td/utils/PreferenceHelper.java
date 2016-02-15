package com.budly.android.CustomerApp.td.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.budly.android.CustomerApp.Common;
import com.budly.android.CustomerApp.beans.User;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class PreferenceHelper {
	private SharedPreferences sharedPreferences;
    private static PreferenceHelper preferenceHelper;

	private final String USER_INFO = "user_info";
    private final String ORDERS_INFO = "orders_info";
	private final String CHECKED = "checked";
	private final String LOCATION = "location";
	private final String NOTIFICATIONS = "notifications";
    public static String LASTORDER = "lastOrder";

	private PreferenceHelper(Context paramContext) {
		this.sharedPreferences = paramContext.getSharedPreferences("budly", 0);
	}

    public String addNotification(String notification,int expireMinutes){

        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mma");
        String date = sdf.format(d);
        String id = UUID.randomUUID().toString();

        JSONObject jso = new JSONObject();
        try {
            JSONArray jarray = new JSONArray(sharedPreferences.getString(NOTIFICATIONS, "[]"));

            jso.put("date", date);
            jso.put("data", notification);
            jso.put("expire",expireMinutes);
            jso.put("id", id);

            jarray.put(jso);

            SharedPreferences.Editor localEditor = this.sharedPreferences.edit();
            localEditor.putString(NOTIFICATIONS, jarray.toString());
            localEditor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return id;
    }

    public JSONArray getNotifications() {
        try {
            JSONArray jarray = new JSONArray(sharedPreferences.getString(NOTIFICATIONS, "[]"));
            JSONArray newArray = new JSONArray();

            for (int i = 0;i < jarray.length();i++){
                String date = jarray.getJSONObject(i).getString("date");
                if (Common.dateDifferenceLocal(date) <  jarray.getJSONObject(i).getInt("expire")){
                    newArray.put(jarray.getJSONObject(i));
                }
            }

            SharedPreferences.Editor localEditor = this.sharedPreferences.edit();
            localEditor.putString(NOTIFICATIONS, newArray.toString());
            localEditor.commit();

            return newArray;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void removeNotification(String uuid){
        try {
            JSONArray jarray = new JSONArray(sharedPreferences.getString(NOTIFICATIONS, "[]"));

            jarray = deleteObject(jarray,uuid);

            SharedPreferences.Editor localEditor = this.sharedPreferences.edit();
            localEditor.putString(NOTIFICATIONS, jarray.toString());
            localEditor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray deleteObject(JSONArray json,String uuid){
        JSONArray newArray = new JSONArray();

        try {
            for (int i = 0; i < json.length(); i++) {
                if (!json.getJSONObject(i).getString("id").equals(uuid)) {
                    newArray.put(json.getJSONObject(i));
                }
            }
        }catch (Exception e){
            return json;
        }


        return newArray;
    }

    public static synchronized void initializeInstance(Context context) {
        if (preferenceHelper == null) {
            preferenceHelper = new PreferenceHelper(context);
        }
    }

    public static synchronized PreferenceHelper getInstance() {
        if (preferenceHelper == null) {
            throw new IllegalStateException(PreferenceHelper.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return preferenceHelper;
    }

	public String getGCMID() {
		return sharedPreferences.getString("gcm_id", "");
	}
	
	
	public void setGCMID(String id) {
		SharedPreferences.Editor localEditor = this.sharedPreferences.edit();
		localEditor.putString("gcm_id", id);
		localEditor.commit();
	}
	
	public User getUserInfo() {
		User user = User.parseUser(sharedPreferences.getString(USER_INFO, ""));
		if(user==null) return new User();
		else return user;
	}
	
	
	public void setUserInfo(User user_info) {
		SharedPreferences.Editor localEditor = this.sharedPreferences.edit();
		localEditor.putString(USER_INFO, user_info.toJsonString());
		localEditor.commit();
	}

    public void setDriverStatus(int status){
        SharedPreferences.Editor localEditor = this.sharedPreferences.edit();
        localEditor.putInt("driver_status", status);
        localEditor.commit();
    }

    public int getDriverStatus(){
        return sharedPreferences.getInt("driver_status", 0);
    }

	public void setPhoneNumber(String phone_number) {
		User user = getUserInfo();
		user.phone_number = phone_number;
		setUserInfo(user);
	}
	
	public String getPhoneNumber() {
		User user = getUserInfo();
		return user.phone_number;
	}
	
	public void setChecked(String checked) {
		SharedPreferences.Editor localEditor = this.sharedPreferences.edit();
		localEditor.putString(CHECKED, checked);
		localEditor.commit();
	}
	
	public String getChecked() {
		return sharedPreferences.getString(CHECKED, "[]");
	}
	
	public void setLocation(LatLng latlng) {
		try {
			JSONObject jso = new JSONObject();
			jso.put("lat", latlng.latitude);
			jso.put("lng", latlng.longitude);
			SharedPreferences.Editor localEditor = this.sharedPreferences.edit();
			localEditor.putString(LOCATION, jso.toString());
			localEditor.commit();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public LatLng getLocation() {
		try {
			String jstr = sharedPreferences.getString(LOCATION, "{}");
			JSONObject jso = new JSONObject(jstr);
			return new LatLng(jso.getDouble("lat"), jso.getDouble("lng"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	// remove all share reference
	public void clearAllPreferences() {
		
	}
	
	public void setValue(String key, String val) {
		SharedPreferences.Editor localEditor = this.sharedPreferences.edit();
		localEditor.putString(key, val);
		localEditor.commit();
	}
	
	public String getValue(String key) {
		return sharedPreferences.getString(key, "");
	}
}

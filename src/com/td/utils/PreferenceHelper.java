package com.budly.android.CustomerApp.td.utils;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.budly.android.CustomerApp.beans.User;
import com.google.android.gms.maps.model.LatLng;

public class PreferenceHelper {
	private SharedPreferences sharedPreferences;
	private final String USER_INFO = "user_info";
	private final String CHECKED = "checked";
	private final String LOCATION = "location";
	
	public PreferenceHelper(Context paramContext) {
		this.sharedPreferences = paramContext.getSharedPreferences("budly", 0);
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

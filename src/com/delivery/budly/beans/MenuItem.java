package com.budly.android.CustomerApp.beans;

import org.json.JSONObject;

public class MenuItem {
	public int id;
	public String size;
	public double price;
	public int quality=0;
	String toJSONString() {
		try {
			JSONObject jso = new JSONObject();
			jso.put("id", id);
			jso.put("size", size);
			jso.put("price", price);
			try {
				jso.put("quality", quality);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return jso.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static MenuItem parse(String jsonStr) {
		try {
			JSONObject jso = new JSONObject(jsonStr);
			MenuItem i = new MenuItem();
			i.id = jso.getInt("id");
			i.size = jso.getString("size");
			i.price = jso.getDouble("price");
			try {
				i.quality = jso.getInt("quality");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return i;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

package com.budly.android.CustomerApp.beans;

import org.json.JSONObject;

import android.util.Log;

//import com.google.android.gms.internal.di;

public class Supplier {
	public String name="";
	public String image="";
	public int avg_rate=0;
	public int distance=0;
	public int id=0;
	public int minimum_per=0;
	public Supplier() { }
	public Supplier(String name, int id, int avg_rate, String image) {
		this.name = name;
		this.id = id;
		this.avg_rate = avg_rate;
		this.image = image;
	}
	
	public Supplier(String name, int id, int avg_rate, String image, int distance, int minimum_per) {
		this.name = name;
		this.id = id;
		this.avg_rate = avg_rate;
		this.image = image;
		this.distance = distance;
		this.minimum_per = minimum_per;
	}
	
	@Override
	public String toString() {
		try {
			JSONObject jso = new JSONObject();
			jso.put("name", name);
			jso.put("id", id);
			jso.put("avg_rate", avg_rate);
			jso.put("image", image);
			jso.put("distance", distance);
			jso.put("minimum_per", minimum_per);
			return jso.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Supplier parse(String jsonString) {
		try {
			JSONObject jso = new JSONObject(jsonString);
			return parse(jso);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Supplier parse(JSONObject jso) {
		try {
			int distance = 0;
			int minimum_per = 0;
			try {
				if(jso.has("distance"))
					distance = jso.getInt("distance");
			} catch (Exception e) { }
			try {
				if(jso.has("minimum_per")) {
					minimum_per = jso.getInt("minimum_per");
					Log.e("Tuan2", "==="+minimum_per);
				}
			} catch (Exception e) { }
			return new Supplier(jso.getString("name"), jso.getInt("id"), jso.getInt("avg_rate"), jso.getString("image"), distance, minimum_per);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

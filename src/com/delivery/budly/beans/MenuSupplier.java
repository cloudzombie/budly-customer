package com.budly.android.CustomerApp.beans;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class MenuSupplier {
	public int id;
	public String name;
	public String desc;
	public double total=0D;
	public ArrayList<MenuItem> items;
	public String toJSONString() {
		try {
			JSONObject jso = new JSONObject();
			jso.put("id", id);
			jso.put("name", name);
			jso.put("desc", desc);
			jso.put("total", total);
			JSONArray jsa = new JSONArray();
			for (int i = 0; i < items.size(); i++) {
				jsa.put(items.get(i).toJSONString());	
			}
			jso.put("items", jsa);
			return jso.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static MenuSupplier parse(String jsonstr) {
		try {
			JSONObject jso = new JSONObject(jsonstr);
			MenuSupplier m = new MenuSupplier();
			m.id = jso.getInt("id");
			m.name = jso.getString("name");
			m.desc = jso.getString("desc");
			try {
				m.total = jso.getDouble("total");
			} catch (Exception e) { }
			JSONArray jsa = jso.getJSONArray("items");
			ArrayList<MenuItem> items = new ArrayList<MenuItem>();
			for (int i = 0; i < jsa.length(); i++) {
				try {
					MenuItem mi = MenuItem.parse(jsa.getString(i));
					if(mi!=null)
						items.add(mi);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			m.items = items;
			return m;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

package com.budly.android.CustomerApp.beans;

import org.json.JSONObject;

public class User {
	public String first_name="";
	public String last_name="";
	public String phone_number="";
	public String email="";
	public String password="";
	public String address="";
	public String City="";
	public String state="";
	public String zip="";
	public String type="";
	public String image="";
	public String image_license="";
	public int id=-1;
	
	public String toJsonString() {
		try {
			JSONObject data = new JSONObject();
			data.put("first_name", first_name);
			data.put("last_name", last_name);
			data.put("phone_number", phone_number);
			data.put("email", email);
			data.put("password", password);
			data.put("type", type);
			data.put("id", id);
			data.put("address", address);
			data.put("City", City);
			data.put("state", state);
			data.put("zip", zip);
			data.put("image_license", image_license);
			data.put("image", image);
			return data.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "null";
	}
	
	public static User parseUser(String dataJsonString) {
		try {
			User user = new User();
			JSONObject data = new JSONObject(dataJsonString);
			user.id = data.getInt("id");
			user.first_name = data.getString("first_name");
			user.last_name = data.getString("last_name");
			user.phone_number = data.getString("phone_number");
			user.email = data.getString("email");
			user.password = data.getString("password");
			user.type = data.getString("type");
			try {
				user.image = data.getString("image");
			} catch (Exception e) {}
			
			if(user.type.equals("customer")) {
				user.address = data.getString("address");
				user.City = data.getString("City");
				user.state = data.getString("state");
				user.zip = data.getString("zip");
				user.image_license = data.getString("image_license");
			}
			return user;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

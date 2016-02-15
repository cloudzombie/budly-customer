package com.budly.android.CustomerApp.beans;

import org.json.JSONObject;

/**
 * Created by EdwinSL on 3/2/2015.
 */
public class Customer {
    int orderID;
    String first_name;
    String last_name;
    String address;
    int verified;

    public String toJsonString() {
        try {
            JSONObject data = new JSONObject();
            data.put("first_name", first_name);
            data.put("last_name", last_name);
            data.put("address", address);
            data.put("verified", verified);
            data.put("orderID", orderID);

            return data.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "null";
    }

    public static Customer parseCustomer(String dataJsonString){
        try {
            Customer customer = new Customer();
            JSONObject data = new JSONObject(dataJsonString);
            customer.orderID = data.getInt("order_id");
            customer.first_name = data.getString("first_name");
            customer.last_name = data.getString("last_name");
            customer.address = data.getString("address");
            customer.verified = data.getInt("verified");

            return customer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

package com.budly.android.CustomerApp.td.http;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

public class HttpBasicClientHelper {
	private static final String BASE_URL = "http://site4demo.com/login/";
//	private static final String BASE_URL = "http://budly.com/login/";
//  private static final String BASE_URL = "http://104.236.44.55/";

	private static AndroidHttpClient client = new AndroidHttpClient();
	private AsyncCallback asyncHttpResponseHandler;
	
	public HttpBasicClientHelper(AsyncCallback asyncHttpResponseHandler) {
		this.asyncHttpResponseHandler = asyncHttpResponseHandler;
		client.setConnectionTimeout(30000);
	}

	public static void get(String url, AsyncCallback responseHandler, ParameterMap params) {
		Log.i("Tuan", url);
		client.get(url, params, responseHandler);
	}
	
	public static void get(String url, AsyncCallback responseHandler) {
		Log.i("Tuan", url);
		client.get(url, null, responseHandler);
	}

	public static void post(String url, AsyncCallback responseHandler, ParameterMap params) {
		Log.d("POST params",(new JSONObject(params)).toString());
		client.post(url, params, responseHandler);
	}

   public static String getAbsoluteURL(String relativeUrl){
       return BASE_URL+relativeUrl;
   }

	private static String getAbsoluteUrl(String base, String relativeUrl) {
		return base + relativeUrl;
	}
	
	public void image(int user_id, String type, File fileUpload) {
		try {
			Log.i("Tuan", "============upload image==============");
			ParameterMap params = new ParameterMap();
			params.put("user_id", String.valueOf(user_id));
			params.put("type", String.valueOf(type));
			params.put("fileUpload", String.valueOf(fileUpload));
			post(getAbsoluteUrl(BASE_URL, "api/user/image"), asyncHttpResponseHandler, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void login(String phone_number, String password){
		ParameterMap params = new ParameterMap();
		params.put("phone_number", phone_number);
		params.put("password", password);
		post(getAbsoluteUrl(BASE_URL, "api/user/login"), asyncHttpResponseHandler, params);
	}
	
	public void checkphone(String phone_number) {
		String url = getAbsoluteUrl(BASE_URL, ("api/user/check_phone/@"+ phone_number));
		get(url, asyncHttpResponseHandler);
	}
	
	public void register(String first_name, String last_name, String phone_number, String email, 
			String password, String type, String address, String City, String state, String zip, String signup_date){
		try {
			JSONObject data = new JSONObject();
			data.put("first_name", first_name);
			data.put("last_name", last_name);
			data.put("phone_number", phone_number);
			data.put("email", email);
			data.put("password", password);
			data.put("type", type);

			data.put("signup_date", signup_date);

			if(type.equals("customer")) {
				data.put("address", address);
				data.put("City", City);
				data.put("state", state);
				data.put("zip", zip);
			}
			ParameterMap params = new ParameterMap();
			params.put("data", data.toString());

			post(getAbsoluteUrl(BASE_URL, "api/user/register"), asyncHttpResponseHandler, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void register2(String phone_number, String recomendation, String expiry, String clinic_phone, String doc_name,
                         String doc_web){
        try {
            JSONObject data = new JSONObject();

            data.put("first_name", "test");
            data.put("last_name", "test");
            data.put("phone_number", phone_number);
            data.put("email", "test");
            data.put("password", "test");
            data.put("type", "customer");
            data.put("address", "test");
            data.put("City", "test");
            data.put("state", "test");
            data.put("zip", "test");


            data.put("recomendation", recomendation);
            data.put("expiration", expiry);
            data.put("doc_name", doc_name);
            data.put("doc_web", doc_web);
            data.put("clinic_phone", clinic_phone);

            ParameterMap params = new ParameterMap();
            params.put("data", data.toString());


            post(getAbsoluteUrl(BASE_URL, "api/user/register"), asyncHttpResponseHandler, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public void update(String dataJson){
		try {
			Log.i("Tuan", dataJson);
			ParameterMap params = new ParameterMap();
			params.put("data", dataJson);
			post(getAbsoluteUrl(BASE_URL, "api/user/update"), asyncHttpResponseHandler, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getTransactions(int user_id, String type) {
		String url = getAbsoluteUrl(BASE_URL, String.format("api/user/getTransactions/%s/@%s", type, String.valueOf(user_id)));
		get(url, asyncHttpResponseHandler);
	}
	
	public void getSuppliers(double latitude, double logitude, int distance, int page_number) {
		String url = getAbsoluteUrl(BASE_URL, String.format("api/supplier/getSuppliers/@%s,%s,%s,%s", 
				String.valueOf(latitude), String.valueOf(logitude), String.valueOf(distance), String.valueOf(page_number)));
		get(url, asyncHttpResponseHandler);
	}
	
	public void getSuppliersWithDriver(int driver_id, double latitude, double logitude, int distance, int page_number) {
		String url = getAbsoluteUrl(BASE_URL, String.format("api/driver/getSuppliers/%s/@%s,%s,%s,%s", String.valueOf(driver_id),
				String.valueOf(latitude), String.valueOf(logitude), String.valueOf(distance), String.valueOf(page_number)));
		get(url, asyncHttpResponseHandler);
	}
	
	public void getMenu(int supplier_id) {
		String url = getAbsoluteUrl(BASE_URL, String.format("api/supplier/getMenu/@%s", String.valueOf(supplier_id)));
		get(url, asyncHttpResponseHandler);
	}
	
	public void order(int customer_id, int supplier_id, double total_price, String list_items, String address, double latitude, double longitude){
		try {
			ParameterMap params = new ParameterMap();
			params.put("customer_id", String.valueOf(customer_id));
			params.put("supplier_id", String.valueOf(supplier_id));
			params.put("total_price", String.valueOf(total_price));
            params.put("latitude", String.valueOf(latitude));
            params.put("longitude", String.valueOf(longitude));
			params.put("list_items", list_items);
			params.put("address", address);
			post(getAbsoluteUrl(BASE_URL, "api/customer/order"), asyncHttpResponseHandler, params);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getDrivers(int order_id) {
		String url = getAbsoluteUrl(BASE_URL, String.format("api/customer/getDrivers/@%s", String.valueOf(order_id)));
		get(url, asyncHttpResponseHandler);
	}
	
	public void selectDriver(int order_id, int driver_id, int customer_id){
		try {
			ParameterMap params = new ParameterMap();
			params.put("order_id", String.valueOf(order_id));
			params.put("driver_id", String.valueOf(driver_id));
			params.put("customer_id", String.valueOf(customer_id));
			Log.e("Tuan", order_id+"/"+driver_id+"/"+customer_id);
			post(getAbsoluteUrl(BASE_URL, "api/customer/selectDriver"), asyncHttpResponseHandler, params);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void selectSupplier(int driver_id, JSONArray supplier_ids){
		try {
			ParameterMap params = new ParameterMap();
			params.put("driver_id", String.valueOf(driver_id));
			params.put("supplier_ids", supplier_ids.toString());
			post(getAbsoluteUrl(BASE_URL, "api/driver/selectSupplier"), asyncHttpResponseHandler, params);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateStatus(int driver_id, int avaiable){
		try {
			ParameterMap params = new ParameterMap();
			params.put("driver_id", String.valueOf(driver_id));
			params.put("avaiable", String.valueOf(avaiable));
			post(getAbsoluteUrl(BASE_URL, "api/driver/updateStatus"), asyncHttpResponseHandler, params);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getOrder(int order_id, int driver_id) {
		String url = getAbsoluteUrl(BASE_URL, String.format("api/driver/getOrder/@%s", String.valueOf(order_id)));
		ParameterMap params = new ParameterMap();
		params.put("driver_id", String.valueOf(driver_id));
		post(url, asyncHttpResponseHandler, params);	
	}

    public void getActiveOrdersByDriverID(int driver_id){
        String url = getAbsoluteUrl(BASE_URL, String.format("api/driver/getOrders"));
        ParameterMap params = new ParameterMap();
        params.put("driver_id", String.valueOf(driver_id));
        post(url,asyncHttpResponseHandler, params);
    }
    public void getTotalOrdersByDriverID(int driver_id){
        String url = getAbsoluteUrl(BASE_URL, String.format("api/driver/getOrdersCount"));
        ParameterMap params = new ParameterMap();
        params.put("driver_id", String.valueOf(driver_id));
        post(url,asyncHttpResponseHandler, params);
    }
	
	public void estimateTime(int order_id, int driver_id, int estimate_time){
		try {
			ParameterMap params = new ParameterMap();
			params.put("order_id", String.valueOf(order_id));
			params.put("driver_id", String.valueOf(driver_id));
			params.put("estimate_time", String.valueOf(estimate_time));
			post(getAbsoluteUrl(BASE_URL, "api/driver/estimateTime"), asyncHttpResponseHandler, params);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void confirmDelivery(int order_id, int driver_id, String type){
		try {
			ParameterMap params = new ParameterMap();
			params.put("order_id", String.valueOf(order_id));
			params.put("driver_id", String.valueOf(driver_id));
			post(getAbsoluteUrl(BASE_URL, String.format("api/%s/confirmDelivery", type)), asyncHttpResponseHandler, params);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateDevice(int user_id, String device_id){
		try {
			ParameterMap params = new ParameterMap();
			params.put("user_id", String.valueOf(user_id));
			params.put("device_id", device_id);
			params.put("device_type", "ANDROID");
			post(getAbsoluteUrl(BASE_URL, "api/user/updateDevice"), asyncHttpResponseHandler, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void requestConfirm(int from_id, int order_id){
		try {
			ParameterMap params = new ParameterMap();
			params.put("from_id", String.valueOf(from_id));
			params.put("order_id", String.valueOf(order_id));
			post(getAbsoluteUrl(BASE_URL, "api/user/requestConfirm"), asyncHttpResponseHandler, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void acceptConfirm(int from_id, int order_id){
		try {
			ParameterMap params = new ParameterMap();
			params.put("from_id", String.valueOf(from_id));
			params.put("order_id", String.valueOf(order_id));
			post(getAbsoluteUrl(BASE_URL, "api/user/acceptConfirm"), asyncHttpResponseHandler, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void denyConfirm(int from_id, int order_id){
		try {
			ParameterMap params = new ParameterMap();
			params.put("from_id", String.valueOf(from_id));
			params.put("order_id", String.valueOf(order_id));
			post(getAbsoluteUrl(BASE_URL, "api/user/denyConfirm"), asyncHttpResponseHandler, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getCurrentRank(int driver_id){
		try {
			ParameterMap params = new ParameterMap();
			params.put("driver_id", String.valueOf(driver_id));
			post(getAbsoluteUrl(BASE_URL, "api/driver/getCurrentRank"), asyncHttpResponseHandler, params);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void rate(int driver_id, int supplier_id, int rate){
		try {
			Log.e("Tuan", "id="+driver_id+" supid="+supplier_id+" rate="+rate);
			ParameterMap params = new ParameterMap();
			params.put("driver_id", String.valueOf(driver_id));
			params.put("supplier_id", String.valueOf(supplier_id));
			params.put("rate", String.valueOf(rate));
			post(getAbsoluteUrl(BASE_URL, "api/customer/rate"), asyncHttpResponseHandler, params);	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void on_start(int user_id, double latitude, double longitude){
		try {
			ParameterMap params = new ParameterMap();
			params.put("user_id", String.valueOf(user_id));
			params.put("latitude", String.valueOf(latitude));
			params.put("longitude", String.valueOf(longitude));
			post(getAbsoluteUrl(BASE_URL, "api/user/onStart"), asyncHttpResponseHandler, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void reportProblem(int user_id,int order_id, String content){
        try {
            ParameterMap params = new ParameterMap();
            params.put("user_id", String.valueOf(user_id));
            params.put("order_id", String.valueOf(order_id));
            params.put("content", content);
            post(getAbsoluteUrl(BASE_URL, "api/report/index"), asyncHttpResponseHandler, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getOrderStatus(int orderID){
        try{
            ParameterMap params = new ParameterMap();
            params.put("order_id",String.valueOf(orderID));
            post(getAbsoluteUrl(BASE_URL,"api/driver/getOrderStatus"),asyncHttpResponseHandler,params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getOrderTimeStart(int orderID){
        try{
            ParameterMap params = new ParameterMap();
            params.put("order_id",String.valueOf(orderID));
            post(getAbsoluteUrl(BASE_URL,"api/user/getOrderTimeStart"),asyncHttpResponseHandler,params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void driverConfirmOrder(int order_id, int driver_id){
        try{
            ParameterMap params = new ParameterMap();
            params.put("order_id",String.valueOf(order_id));
            params.put("driver_id",String.valueOf(driver_id));
            post(getAbsoluteUrl(BASE_URL,"api/driver/confirmOrder"),asyncHttpResponseHandler,params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void passwordRest(String phoneNumber){
        try{
            ParameterMap params = new ParameterMap();
            params.put("phone_number",phoneNumber);
            post(getAbsoluteUrl(BASE_URL,"api/user/passwordReset"),asyncHttpResponseHandler,params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getDirections(LatLng source, LatLng destination){
        try{
            ParameterMap params = new ParameterMap();
            params.put("origin", source.latitude+","+source.longitude);
            params.put("destination", destination.latitude+","+destination.longitude);
            get("http://maps.googleapis.com/maps/api/directions/json",asyncHttpResponseHandler,params);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

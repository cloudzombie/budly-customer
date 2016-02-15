package com.budly.android.CustomerApp.td.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;

public class MyJsonAsyncCallback extends AsyncCallback{
	
	JSONObject response = new JSONObject();
	JSONArray response2 = new JSONArray();

	@Override
	public void onComplete(final HttpResponse httpResponse) {
		response = new JSONObject();
		response2 = new JSONArray();
		String content = null;
		try {
			content = httpResponse.getBodyAsString();
			response = new JSONObject(content);
		} catch (Exception e) {
			try {
				response.put("success", 0);
				response.put("errorMessage", String.valueOf(content));
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		try {
			response2 = new JSONArray(content);
		} catch (Exception e) { }
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				onSuccess(httpResponse, response2);
				onSuccess(httpResponse, response);
			}
		}).start();
	}

	@Override
	public void onError(Exception e) {
		onFailure(e);
	}
	
	public void onSuccess(HttpResponse httpResponse, JSONArray re) {}
	public void onSuccess(HttpResponse httpResponse, JSONObject re) {}
	public void onFailure(Exception e) {}

}

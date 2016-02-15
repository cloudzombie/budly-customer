package com.budly.android.CustomerApp.td.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class UploadFile {
	public static int serverResponseCode = 0;
	public static String Upload(String url, String imagePath, String userId,
			String type) throws IOException, ClientProtocolException,
			JSONException {
		String reponseBody;
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);

		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		File file = new File(imagePath);

		ContentBody encFile = new FileBody(file, type);

		entity.addPart("myFile", encFile);
		entity.addPart("UserId", new StringBody(userId));

		request.setEntity(entity);

		ResponseHandler<String> reponsehandler = new BasicResponseHandler();
		reponseBody = client.execute(request, reponsehandler);

		if (reponseBody != null && reponseBody.length() > 0) {
			Log.i("Reponse", "Reponse" + reponseBody);
		}
		return reponseBody;
	}
	
	public static String uploadFile(final String serverUrl, final String sourceFileUri, final Context context) {
		return uploadFile(serverUrl, sourceFileUri, context, new LinkedHashMap<String, String>());
	}
	
	public static String uploadFile(final String serverUrl, final String sourceFileUri, final Context context, LinkedHashMap<String, String> params) {
		if(sourceFileUri==null) {
			JSONObject jo = new JSONObject();
			try {
				Log.i("UploadFile", "2");
				jo.put("status", 404);
				jo.put("data", new JSONArray());
			} catch (Exception e) {
				Log.i("UploadFile", "3");
				e.printStackTrace();
			}
			return jo.toString();
		}
		String fileName = sourceFileUri;
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		String reponseBody = null;
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(sourceFileUri);
		//String uploadFilePath = sourceFile.getAbsolutePath();
		//final String uploadFileName = sourceFile.getName();
		Log.i("Tuan", "Source File:" + sourceFileUri);
		if (!sourceFile.isFile()) {
			Log.i("uploadFile", "Source File not exist :" + sourceFileUri);
			JSONObject jo = new JSONObject();
			try {
				Log.i("UploadFile", "2");
				jo.put("status", 404);
				jo.put("data", new JSONArray());
			} catch (Exception e) {
				Log.i("UploadFile", "3");
				e.printStackTrace();
			}
			return jo.toString();

		} else {
			try {
				Log.i("UploadFile", "4");
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				final String mimeType = URLConnection.guessContentTypeFromName(sourceFileUri);
//				final String mimeType = MimeTypeMap.getFileExtensionFromUrl(sourceFileUri);
//				ContentResolver cR = context.getContentResolver();
//			    MimeTypeMap mime = MimeTypeMap.getSingleton();
//			    String mimeType = mime.getExtensionFromMimeType(cR.getType(Uri.fromFile(sourceFile)));
				// Táº¡o káº¿t ná»‘i URL connection Ä‘áº¿n server
				URL url = new URL(serverUrl);

				// Má»Ÿ HTTP connection tá»« URL
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("upload_file", fileName);
				//conn.setRequestProperty("user_id", userId);
				if(params==null) params = new LinkedHashMap<String, String>();
				Log.i("UploadFile", "5");
				try {
					dos = new DataOutputStream(conn.getOutputStream());
				} catch (Exception e) {
					e.printStackTrace();
				}
				 
				 
				if (null==dos) {
					try {
						serverResponseCode = conn.getResponseCode();
					} catch (Exception e) {
						JSONObject jo = new JSONObject();
						jo.put("status", 500);
						jo.put("data", new JSONArray());
						return jo.toString();
					}
				}
				
				
				for (String key : params.keySet()) {
					String value = params.get(key).toString();
					try {
						dos.writeBytes(twoHyphens + boundary + lineEnd);
					} catch (Exception e) {
						e.printStackTrace();
					}
					dos.writeBytes("Content-Type: text/plain;charset=UTF-8;" + lineEnd);
					dos.writeBytes("Content-Disposition: form-data; name=\""+key+"\"" + lineEnd);
					dos.writeBytes("Content-Length: " + value.length() + ";" + lineEnd + lineEnd);
					dos.writeBytes(value+lineEnd);
				}
				Log.i("Tuan",String.valueOf(mimeType));
				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-type: " + mimeType + ";" + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"fileUpload\";filename=\"" + fileName + "\";" + lineEnd);
				dos.writeBytes(lineEnd);

				// Dat buffer size
				bytesAvailable = fileInputStream.available();

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// Ä�á»�c file vÃ  ghi DataOutputStream
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				try {
					serverResponseCode = conn.getResponseCode();
				} catch (Exception e) {
					JSONObject jo = new JSONObject();
					jo.put("status", 500);
					jo.put("data", new JSONArray());
					return jo.toString();
				}
				
				String serverResponseMessage = conn.getResponseMessage();
				Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

				if (serverResponseCode == 200) {
					Log.i("UploadFile", "6");
					reponseBody = IOUtils.toString((InputStream) conn.getContent());
				} else {
					Log.i("UploadFile", "7");
					JSONObject jo = new JSONObject();
					try {
						jo.put("status", 500);
						jo.put("data", new JSONArray());
					} catch (Exception e) {
						e.printStackTrace();
					}
					reponseBody = jo.toString();
				}

				// close the streams //
				fileInputStream.close();
				try {
					dos.flush();
					dos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (MalformedURLException ex) {
				Log.i("UploadFile", "8");
				ex.printStackTrace();
				Log.i("Upload file to server", "error: " + ex.getMessage(), ex);
			} catch (Exception e) {
				Log.i("UploadFile", "9");
				e.printStackTrace();
				Log.i("Upload file to server Exception", "Exception : " + e.getMessage(), e);
			}
			return reponseBody;

		} // End else block
	}
}

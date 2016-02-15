package com.budly.android.CustomerApp.td.widget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.media.MediaPlayer;
import android.util.Log;

import com.budly.Common;
import com.budly.android.CustomerApp.td.utils.MyHash;

public class MyPlayer extends MediaPlayer {

	public String cacheAudio(String path) {
		try {
			if(path.startsWith(Common.SDCARD_AUDIO)) return path;
			String newPath = Common.SDCARD_AUDIO + MyHash.md5(path) + ".m4a";
			File f = new File(newPath);
			if(f.exists()) return newPath;
			if(DownloadFile(new URL(path), newPath)) {
				return newPath;
			} else {
				return path;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return path;
		}
	}

	public static Boolean DownloadFile(URL theURL, String outPath) throws IOException {

		URLConnection con = theURL.openConnection();
		con.connect();

		String type = con.getContentType();
		System.out.println(type);

		if (type != null) {
			byte[] buffer = new byte[4096];
			FileOutputStream os = new FileOutputStream(outPath);
			InputStream in = con.getInputStream();
			int read;
			while ((read = in.read(buffer)) > 0) {
				os.write(buffer, 0, read);
			}

			os.close();
			in.close();

			return true;
		}
		return false;
	}

	public void mPlay(String path) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
		//path = cacheAudio(path);
		this.setDataSource(path);
		this.prepare();
		this.start();
	}
	
	public void mStop() {
		this.reset();
		this.stop();
		this.release();
	}
	
	public static void clearCache() {
		try {
			File folder = null;
			try {
				folder = new File(Common.SDCARD_AUDIO);	
			} catch (Exception e) {
				return;
			}
			if(folder==null) return;
			File[] files = folder.listFiles();
			if (files == null) return;
			for (int i = 0; i < files.length; i++) {
				File fileCache = files[i];
				//Date lastModified = new Date(fileCache.lastModified());
				//777610000 = >9 ngay
				if(System.currentTimeMillis()-fileCache.lastModified()>777610000) {
					Log.d("DELETE", "detele "+fileCache.getName());
					try {
						fileCache.delete();
					} catch (Exception e) { }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

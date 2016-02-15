package com.budly;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ImageViewActivity extends Activity {
	ImageView image;
	
	DisplayImageOptions options = new DisplayImageOptions.Builder()
	.cacheInMemory(false).cacheOnDisc(true).build();
	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.main_activity_viewimage);
		image = (ImageView) findViewById(R.id.image);
		try {
			String url = getIntent().getStringExtra("license");
			ImageLoader.getInstance().displayImage(url, image, options);
		} catch (Exception e) { }
	}
}
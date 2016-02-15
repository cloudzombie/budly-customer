package com.budly.android.CustomerApp;

import com.budly.R;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.io.File;

import android.app.Application;
import android.content.res.Configuration;
import android.graphics.Bitmap.CompressFormat;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.disc.impl.TotalSizeLimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.budly.android.CustomerApp.td.utils.LocationHelper;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;

public class MyApplication extends Application {

	public static GoogleAnalytics analytics;
	public static Tracker tracker;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		analytics = GoogleAnalytics.getInstance(this);

		analytics.setLocalDispatchPeriod(600);
		tracker = analytics.newTracker("UA-37172466-25"); // Replace with actual tracker/property Id
		tracker.enableExceptionReporting(true);
		tracker.enableAdvertisingIdCollection(true);
		tracker.setScreenName("customer");
//		tracker.enableAutoActivityTracking(true);
		MyApplication.tracker.send(new HitBuilders.ScreenViewBuilder().build());

		Fabric.with(this, new Crashlytics());
		// config image loader
		File cacheDir = StorageUtils.getCacheDirectory(getApplicationContext());
        PreferenceHelper.initializeInstance(this);
        LocationHelper.initLocationHelper(this);
		if (!cacheDir.exists())
			cacheDir.mkdir();

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(false).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.defaultDisplayImageOptions(defaultOptions)
				// .memoryCacheExtraOptions(480, 800) // default = device screen
				// dimensions
				.discCacheExtraOptions(480, 800, CompressFormat.PNG, 75, null)
				.threadPoolSize(2) // default
				.threadPriority(Thread.NORM_PRIORITY - 1) // default
				.tasksProcessingOrder(QueueProcessingType.FIFO) // default
				.denyCacheImageMultipleSizesInMemory()
				// .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
				// .memoryCache(new WeakMemoryCache())
				.memoryCache(new FIFOLimitedMemoryCache(15 * 1024 * 1024))
				// .memoryCacheSizePercentage(13) // default
				// .discCache(new UnlimitedDiscCache(cacheDir)) // default
				// .discCacheSize(50 * 1024 * 1024)
				.discCache(new TotalSizeLimitedDiscCache(cacheDir, 300 * 1024 * 1024))
				// .discCacheFileCount(100)
				.discCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
				.imageDownloader(new BaseImageDownloader(getApplicationContext())) // default
				.imageDecoder(new BaseImageDecoder(false)) // default
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
				.writeDebugLogs().build();

		ImageLoader.getInstance().init(config);
	}
}

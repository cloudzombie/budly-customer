package com.budly.android.CustomerApp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.budly.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class ImageViewActivity extends Activity {
	ImageView image;
    TextView document;
	DisplayImageOptions options = new DisplayImageOptions.Builder()
	.cacheInMemory(false).cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.main_activity_viewimage);
//		image = (ImageView) findViewById(R.id.image);

        String url_license = getIntent().getStringExtra("license");
        String url_recomendation = getIntent().getStringExtra("recomendation");
        Log.i("Edwin", "License "+url_license);
        Log.i("Edwin", "Recomendation "+url_recomendation);
        document = (TextView) findViewById(R.id.document);
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        ImageAdapter adapter = new ImageAdapter(this, url_license, url_recomendation);
        viewPager.setAdapter(adapter);
	}

    public class ImageAdapter extends PagerAdapter {
        Context context;
        private String[] GalImages;
        ImageAdapter(Context context, String... urls){
            this.context=context;
            GalImages = urls.clone();
        }
        @Override
        public int getCount() {
            return GalImages.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((ImageView) object);
        }

        @Override
        public void startUpdate(ViewGroup container) {

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            ImageView imageView = new ImageView(context);
            int padding = 10;
            imageView.setPadding(padding, padding, padding, padding);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            ImageLoader.getInstance().displayImage(GalImages[position], imageView);
            container.addView(imageView, position);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
           container.removeView((ImageView) object);
        }
    }
}
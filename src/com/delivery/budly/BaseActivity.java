package com.budly;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;

public class BaseActivity extends FragmentActivity {

	@Override
	protected void onResume() {
		Common.ON_SCREEN = true;
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		Common.ON_SCREEN = false;
		super.onPause();
	}
}

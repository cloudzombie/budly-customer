package com.budly.android.CustomerApp.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.budly.BaseActivity;
import com.budly.R;

public class MainActivity extends BaseActivity
  implements View.OnClickListener
{
  ImageView btn_i_am_a_driver;
  ImageView btn_place_an_order;

  public void onClick(View paramView)
  {
    switch (paramView.getId())
    {
    default:
      return;
    case R.id.btn_place_an_order:
      startActivity(new Intent(this, ProfileActivity.class));
      return;
    case R.id.btn_i_am_a_driver:
    }
    startActivity(new Intent(this, LoginActivity.class));
  }

  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.driver_activity_main);
    this.btn_i_am_a_driver = ((ImageView)findViewById(R.id.btn_i_am_a_driver));
    this.btn_place_an_order = ((ImageView)findViewById(R.id.btn_place_an_order));
    this.btn_i_am_a_driver.setOnClickListener(this);
    this.btn_place_an_order.setOnClickListener(this);
  }
  
  @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(StatusActivity.IS_FIRST) {
			StatusActivity.IS_FIRST = false;
			finish();
		}
	}
}
package com.citaq.citaqfactory;


import com.citaq.util.LEDControl;
import com.citaq.util.MainBoardUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

public class LedActivity extends Activity {

	private ToggleButton bt_red;
	private ToggleButton bt_blue;
	private ToggleButton bt_fresh;
	private RelativeLayout rlayout_blue, rlayout_fresh;
	
	private LEDControl freshThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_led);

		initView();
		/*Intent a = new Intent();
		a.setAction("android.intent.action.SHOW_NAVIGATION_BAR");
		sendBroadcast(a);
		
		 Intent localIntent = new Intent();
		    localIntent.setAction("android.navigationbar.state");
		    localIntent.putExtra("state", "on");
		    sendBroadcast(localIntent);
		    localIntent.setAction("android.statusbar.state");
		    localIntent.putExtra("state", "on");
		    sendBroadcast(localIntent);*/
	}

	private void initView() {
		bt_red = (ToggleButton) findViewById(R.id.tb_red);
		bt_blue = (ToggleButton) findViewById(R.id.tb_blue);
		bt_fresh = (ToggleButton) findViewById(R.id.tb_fresh);

		bt_red.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1){
					LEDControl.trunOnRedRight(true);
				}else{
					LEDControl.trunOnRedRight(false);
				}
			}
		});

		bt_blue.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1){
					LEDControl.trunOnBlueRight(true);
				}else{
					LEDControl.trunOnBlueRight(false);
				}

			}
		});

		bt_fresh.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(arg1){
					if(freshThread == null)
					{
						freshThread = new LEDControl();
						freshThread.StartFresh();
					}
				}else{
					if(freshThread != null)
					{
						freshThread.StopFresh();
						freshThread = null;
					}
				}
			}
		});

		if(MainBoardUtil.isRK3288_CTD()){
			rlayout_blue = (RelativeLayout)findViewById(R.id.rl_blue);
			rlayout_fresh = (RelativeLayout)findViewById(R.id.rl_fresh);
			rlayout_blue.setVisibility(View.INVISIBLE);
			rlayout_fresh.setVisibility(View.INVISIBLE);
		}

	}
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		LEDControl.trunOnRedRight(false);
		LEDControl.trunOnBlueRight(false);
		if(freshThread != null)
		{
			freshThread.StopFresh();
			freshThread = null;
		}
	}

}

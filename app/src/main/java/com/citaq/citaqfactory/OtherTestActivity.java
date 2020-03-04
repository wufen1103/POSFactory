package com.citaq.citaqfactory;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.citaq.util.GPIOUtils;

public class OtherTestActivity extends Activity {
	private Button bt_opencash;
	int mCurrentBt = -1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_other);
		initView();
	}

	private void initView() {
		bt_opencash = (Button) findViewById(R.id.bt_opencash);
		bt_opencash.setOnClickListener(SendPrintListener);
	}

	View.OnClickListener SendPrintListener = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			mCurrentBt = v.getId();
			switch(mCurrentBt){
				case R.id.bt_opencash:
					if(GPIOUtils.getGPIOStatus(GPIOUtils.Cash_3288) == 0x30){ //当钱箱为关闭状态时执行
						GPIOUtils.witchStatus_SEC( (byte)0x30,GPIOUtils.Cash_3288);
					}
					break;
			}
		}

	};
}

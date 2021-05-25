package com.citaq.citaqfactory;

import com.citaq.util.MainBoardUtil;
import com.citaq.util.SerialPortManager;
import com.printer.util.CallbackSerial;
import com.printer.util.USBConnectUtil;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MSRActivity extends Activity {
	protected static final String TAG = "MSRActivity";
	
	Context mContext;
	EditText tv_received;
	Button bt_delete;
	USBConnectUtil mUSBConnectUtil = null;

	SerialPortManager mSerialPortManager = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_msr);
		mContext =this;
		initView();

		if(MainBoardUtil.isRK3288() || MainBoardUtil.isAllwinnerA63()){
			tv_received.requestFocus();

		}else {

			tv_received.setInputType(InputType.TYPE_NULL);
			initSerial();
		}

	}

	private void initSerial(){
		if(MainBoardUtil.isRK3368() || MainBoardUtil.isRK3288_CTE()){
			mSerialPortManager = new SerialPortManager(this,SerialPortManager.CTMDISPLAYSERIALPORT_TTYS4);
		}else{
			mSerialPortManager = new SerialPortManager(this,SerialPortManager.MSRSERIALPORT_TTYS2);
		}
//		mSerialPortManager = new SerialPortManager(this,SerialPortManager.CTMDISPLAYSERIALPORT_TTYS4);
		mSerialPortManager.setCallback(new CallbackSerial() {
			@Override
			public void onDataReceived(final byte[] buffer, final int size) {
				runOnUiThread(new Runnable() {
					public void run() {
						if (tv_received != null) {
							tv_received.append(new String(buffer, 0, size));
						}
					}
				});
			}
		});


	}

/*	private void initUSBConnect() {       // remember to destroyPrinter on
		if(mUSBConnectUtil == null){
			mUSBConnectUtil = USBConnectUtil.getInstance();


			mUSBConnectUtil.setCallback(new CallbackUSB() {

				@Override
				public void callback(final String str,boolean toShow) {
					tv_received.append(str);

				}

				@Override
				public void hasUSB(boolean hasUSB) {
					if(!hasUSB) Toast.makeText(mContext, R.string.nousbdevice, Toast.LENGTH_SHORT).show();

				}

			});

			mUSBConnectUtil.initConnect(this,USBConnectUtil.TYPE_MAGCARD);
		}

	}*/
	
	private void initView() {
		tv_received = (EditText) findViewById(R.id.TVTTYS2Reception);
		bt_delete = (Button) findViewById(R.id.BtnReceptionClear);
		bt_delete.setOnClickListener(ClearDataListener);
	}


	OnClickListener ClearDataListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			tv_received.setText("");
		}
			
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mUSBConnectUtil != null)
			mUSBConnectUtil.destroyPrinter();
		if(mSerialPortManager != null)
			mSerialPortManager.destroy();
		Log.v(TAG, "onDestroy");
	}

}

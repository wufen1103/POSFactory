package com.citaq.citaqfactory;

import java.io.IOException;
import java.security.InvalidParameterException;

import com.citaq.citaqfactory.SerialPortActivity.ReadThread;
import com.citaq.util.MainBoardUtil;
import com.printer.util.CallbackUSB;
import com.printer.util.USBConnectUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MSRActivity extends SerialPortActivity {
	protected static final String TAG = "MSRActivity";
	
	Context mContext;
	EditText tv_received;
	Button bt_delete;
	USBConnectUtil mUSBConnectUtil = null;
	
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
			try {
				if(MainBoardUtil.isRK3368()){
					mSerialPort = mApplication.getMSRSerialPort_S4();
				}else{
					mSerialPort = mApplication.getMSRSerialPort();
				}
				mSerialPort = mApplication.getMSRSerialPort_S4();
//			mOutputStream = mSerialPort.getOutputStream();
				mInputStream = mSerialPort.getInputStream();

				/* Create a receiving thread */
				mReadThread = new ReadThread();
//			mReadThread.setTag(0);
				mReadThread.start();
			} catch (SecurityException e) {
				DisplayError(R.string.error_security);
			} catch (IOException e) {
				DisplayError(R.string.error_unknown);
			} catch (InvalidParameterException e) {
				DisplayError(R.string.error_configuration);
			}

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
	protected void onDataReceived(final byte[] buffer, final int size) {
		//Log.i(TAG, "size = " + size);
		runOnUiThread(new Runnable() {
			public void run() {
				if (tv_received != null) {
					tv_received.append(new String(buffer, 0, size));
				}
			}
		});
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mUSBConnectUtil != null)
			mUSBConnectUtil.destroyPrinter();
		Log.v(TAG, "onDestroy");
	}

}

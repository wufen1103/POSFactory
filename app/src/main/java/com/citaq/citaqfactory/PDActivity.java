package com.citaq.citaqfactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.citaq.util.Command;
import com.citaq.util.MainBoardUtil;
import com.printer.util.CallbackUSB;
import com.printer.util.USBConnectUtil;

public class PDActivity extends SerialPortActivity {
	protected static final String TAG = "PDActivity";

	Context mContext;
	
	private TextView tv_title;
	private EditText et_QR, et_Title, et_Cmd;
	
	private Spinner spinner_cmd;
	private ArrayAdapter<?> adapter_type, adapter_cmd;
	private String cmdString;
	
	private TextView tv_recevice;
	private Button btn_send = null;
	private Button btn_setTime = null;
	private Button btn_setQR  = null;
	private Button btn_setTitle  = null;
	
	private Button btn_White = null;
	private Button btn_Red = null;
	private Button btn_Blue = null;
	private Button btn_Green = null;
	private Button btn_Black = null;
	
	RadioGroup pd_type;
	RadioButton pd_serial = null;
	RadioButton pd_usb = null;
	int pdType = 0;
	public static byte[] pd = new byte[] { 0x1B, 0x51, 0x41, (byte) 0x31, (byte) 0x32,
			(byte) 0x33, (byte) 0x34,(byte) 0x35, (byte) 0x38,(byte) 0x38, (byte) 0x38, (byte) 0x0D
	,(byte) 0x2E,(byte) 0x2E,(byte) 0x2D,(byte) 0x2D,(byte) 0x2E,(byte) 0x2E,(byte) 0x2E}; //\x1b\x51\x4112345678\x0d
	FileOutputStream mFileOutputStream = null;
	private static final int PD_AUTO_START = 1000;
	private static final int PD_AUTO_STOP = 1001;
	boolean isPDtest = false;
    String[] cmdPD2 = new String[]{};
	String[] cmdPD3 = new String[]{};
    int pd_cmd_id = -1;

	LinearLayout layout_send1,layout_send2,layout_send3;
	
	USBConnectUtil mUSBConnectUtil = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_pd);
		mContext =this;
		initView();
		if(MainBoardUtil.isRK3288()) {
			mOutputStream =null;

			cmdPD2 = getResources().getStringArray(R.array.PD_cmd2);
			cmdPD3 = getResources().getStringArray(R.array.PD_cmd3);
			pd_usb.setVisibility(View.INVISIBLE);
			btn_setTitle.setText(R.string.start_autopdtest);
			et_Title.setVisibility(View.INVISIBLE);
			btn_setTime.setEnabled(false);
			btn_setQR.setEnabled(false);
			et_QR.setEnabled(false);
			btn_White.setEnabled(false);
			btn_Red.setEnabled(false);
			btn_Blue.setEnabled(false);
			btn_Green.setEnabled(false);
			btn_Black.setEnabled(false);
		}
		initSerial();
	}

	private boolean serialWrite2(byte[] cmd){
		try {
			if(mFileOutputStream == null) {
				mFileOutputStream = new FileOutputStream("/dev/ttyACM0");
			}
			mFileOutputStream.write(cmd);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void initSerial(){
		try {
			if(MainBoardUtil.isRK3288()) {
				mSerialPort = mApplication.getCtmDisplaySerialPort2();
			}else{
				mSerialPort = mApplication.getCtmDisplaySerialPort();
			}

			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			DisplayError(R.string.error_security);
		} catch (IOException e) {
			DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
			DisplayError(R.string.error_configuration);
		}
	}
	
	private void initUSBConnect() {       // remember to destroyPrinter on
		if(mUSBConnectUtil == null){
			 mUSBConnectUtil = USBConnectUtil.getInstance();
		       
		        
			 mUSBConnectUtil.setCallback(new CallbackUSB() {
					
					@Override
					public void callback(final String str,boolean toShow) {
						//Log.v(TAG, str.toString());
						
					}

					@Override
					public void hasUSB(boolean hasUSB) {
						if(!hasUSB) Toast.makeText(mContext, R.string.nousbdevice, Toast.LENGTH_SHORT).show();
						
					}
	
				});
		        
			 mUSBConnectUtil.initConnect(this,USBConnectUtil.TYPE_PD);
		}
			
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(isPDtest){
			serialWrite(Command.transToPrintText(cmdPD2[1]));
			pd_cmd_id = -1;
			isPDtest = false;
		}

		super.onDestroy();

		if(mUSBConnectUtil != null)
			mUSBConnectUtil.destroyPrinter();

		if(mFileOutputStream != null) {
			try {
				mFileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		Log.v(TAG, "onDestroy");

	}
	
	
	private void initView(){
		
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.requestFocus();
		
		et_QR = (EditText) findViewById(R.id.et_QR);
		et_QR.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				et_QR.setFocusable(true);
				et_QR.requestFocus();
				
			}
		});
		
		et_Title= (EditText) findViewById(R.id.et_Title);
		et_Title.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				et_Title.setFocusable(true);
				et_Title.requestFocus();
				
			}
		});
		
		et_Cmd = (EditText) findViewById(R.id.et_cmd);
		et_Cmd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				et_Cmd.setFocusable(true);
				et_Cmd.requestFocus();
				
			}
		});
		

		adapter_type= ArrayAdapter.createFromResource(this, R.array.PD_type, android.R.layout.simple_spinner_item);
		adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		
		pd_type = (RadioGroup) findViewById(R.id.pd_type);
		pd_serial = (RadioButton) findViewById(R.id.pd_serial);
		pd_usb = (RadioButton) findViewById(R.id.pd_usb);
		
		pd_type.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(pd_serial.getId()==checkedId){
					pdType = 0;
				}else if(pd_usb.getId()==checkedId){
					pdType = 1;
					if(mUSBConnectUtil == null){
						initUSBConnect();
					}
				}
				
			}
		});
		
		//
		spinner_cmd = (Spinner) findViewById(R.id.spinner_cmd);
		int pd_command = cmdPD2.length == 0 ? R.array.PD_cmd:  R.array.PD_cmd2; //MainBoardUtil.isRK3288() cmdPD2.length == 0 is false
		adapter_cmd=ArrayAdapter.createFromResource(this, pd_command, android.R.layout.simple_spinner_item);
		adapter_cmd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_cmd.setAdapter(adapter_cmd);
		spinner_cmd.setSelection(0);
        cmdString = spinner_cmd.getSelectedItem().toString();
        spinner_cmd.setOnItemSelectedListener(
        		new OnItemSelectedListener(){
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						cmdString=((Spinner)arg0).getSelectedItem().toString();						
						//txtPrint.setText((tmp.split("\n"))[0]);//以\n分割字符串，并只使用第一段
						et_Cmd.setText((cmdString.split("\n"))[0]);
						
					}
					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
         		}
        );
        
		tv_recevice = (TextView) findViewById(R.id.tv_recevice);
		
		btn_send = (Button) findViewById(R.id.btn_send);
		btn_send.setOnClickListener(pdListener);
		
		btn_setTime = (Button) findViewById(R.id.btn_setTime);
		btn_setTime.setOnClickListener(pdListener);
		
		btn_setQR = (Button) findViewById(R.id.btn_setQR);
		btn_setQR.setOnClickListener(pdListener);
		
		btn_setTitle= (Button) findViewById(R.id.btn_setTitle);
		btn_setTitle.setOnClickListener(pdListener);
		
		btn_White = (Button) findViewById(R.id.btn_White);
		btn_White.setOnClickListener(pdListener);
		
		btn_Red = (Button) findViewById(R.id.btn_Red);
		btn_Red.setOnClickListener(pdListener);
		
		btn_Blue = (Button) findViewById(R.id.btn_Blue);
		btn_Blue.setOnClickListener(pdListener);
		
		btn_Green = (Button) findViewById(R.id.btn_Green);
		btn_Green.setOnClickListener(pdListener);
		
		btn_Black = (Button) findViewById(R.id.btn_Black);
		btn_Black.setOnClickListener(pdListener);
		
		layout_send1 = (LinearLayout) findViewById(R.id.layout_send1);
		layout_send2 = (LinearLayout) findViewById(R.id.layout_send2);
		layout_send3 = (LinearLayout) findViewById(R.id.layout_send3);
	}
	
	private void setVisibility(int show){
		if(show == View.INVISIBLE){
			layout_send1.setVisibility(View.INVISIBLE);
			layout_send2.setVisibility(View.INVISIBLE);
			layout_send3.setVisibility(View.INVISIBLE);
		}else{
			layout_send1.setVisibility(View.VISIBLE);
			layout_send2.setVisibility(View.VISIBLE);
			layout_send3.setVisibility(View.VISIBLE);
		}
	}
	
	OnClickListener pdListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			if(pdType == 0){   //serial
				switch(v.getId()){
				case R.id.btn_send:
					
//					byte[] a = Command.getCodepage(15);
//					byte[] b = Command.getChineseMode(0);
					
					String cmdStr =et_Cmd.getText().toString();
					
//					byte[] c = Command.transToPrintText(cmdStr);

//					serialWrite(Command.transToPrintText((cmdString.split("\n"))[0]));
					serialWrite(Command.transToPrintText(cmdStr));
					break;
				case R.id.btn_setTitle:
					if(btn_setTitle.getText().equals(getString(R.string.stop_autopdtest))){
						btn_setTitle.setText(R.string.start_autopdtest);
					}else if (btn_setTitle.getText().equals(getString(R.string.start_autopdtest))){
						btn_setTitle.setText(R.string.stop_autopdtest);
					}

					if(btn_setTitle.getText().equals(getString(R.string.stop_autopdtest))){
						isPDtest = true;
						handler.sendEmptyMessage(PD_AUTO_START);
						//handler.sendEmptyMessageDelayed(PD_AUTO_START, 1000);
					}else if(btn_setTitle.getText().equals(getString(R.string.start_autopdtest))){

						handler.sendEmptyMessage(PD_AUTO_STOP);
					}else {
						String title = et_Title.getText().toString();
						if (title.getBytes().length > 30) {
							Toast.makeText(mContext, "Too Long(must < 30 bytes", Toast.LENGTH_SHORT).show();
							break;

						}
						//					title ="STX N " + title + "CR";
						byte[] data1 = {2, 78};
						byte[] data2 = Command.transCommandBytes(title);
						byte[] data3 = {13};
						byte[] data4 = new byte[title.getBytes().length + 3];
						System.arraycopy(data1, 0, data4, 0, 2);
						System.arraycopy(data2, 0, data4, data1.length, data2.length);
						System.arraycopy(data3, 0, data4, data1.length + data2.length, data3.length);

						serialWrite(data4);
					}
					
					break;
				case R.id.btn_setTime:
					serialWrite(Command.getSetTimeCmd());
					break;
				case R.id.btn_setQR:
					byte[] cmd = Command.getSendQRCmd(et_QR.getText().toString());
					if(cmd != null){
						serialWrite(cmd);
					}
					break;
					
				case R.id.btn_Red:
					serialWrite(Command.getColorCmd(Command.RED));//1001
					break;
				case R.id.btn_Blue:
					serialWrite(Command.getColorCmd(Command.BLUE));
					break;
				case R.id.btn_Green:
					serialWrite(Command.getColorCmd(Command.GREEN));
					break;
				case R.id.btn_Black:
					serialWrite(Command.getColorCmd(Command.BLACK));
					break;
				case R.id.btn_White:
					serialWrite(Command.getColorCmd(Command.WHITE));
					break;
			
				}
			}else{
				//usb
				String cmd;
				boolean isSend =false;
				StringBuffer sb = new StringBuffer();
				switch(v.getId()){
				case R.id.btn_send:
//					if(cmdString.equals("Chinese CP866")){
//						
//						//Command.getCodepage(18);//cp866
//						//Command.getChineseMode(0); //GBK
//						sb = mUSBPDUtil.sendMessageToPointByte(Command.getCodepage(15));
//						sb = mUSBPDUtil.sendMessageToPointByte(Command.getChineseMode(0));
//						break;
//					}
					cmd =et_Cmd.getText().toString();
					isSend = mUSBConnectUtil.sendMessageToPoint(Command.transToPrintText((cmd.split("\n"))[0]));
					if(isSend){
						tv_recevice.setText("Data send!");
					}else{
						tv_recevice.setText("Data can not sent!");
					}
					
					break;
					
				case R.id.btn_setTitle:
					String title = et_Title.getText().toString();
					if(title.getBytes().length>30){
						Toast.makeText(mContext, "Too Long(must < 30 bytes", Toast.LENGTH_SHORT).show();
						break;
					
					}
//					title ="STX N " + title + "CR";
					byte[] data1 = {2,78};
					byte[] data2 = Command.transCommandBytes(title);
					byte[] data3 = {13};
					byte[] data4 = new byte[title.getBytes().length + 3];
					System.arraycopy(data1,0,data4,0,2);
					System.arraycopy(data2,0,data4,data1.length,data2.length);
					System.arraycopy(data3,0,data4,data1.length + data2.length,data3.length);
					
					isSend = mUSBConnectUtil.sendMessageToPoint(data4);
					if(isSend){
						tv_recevice.setText("Data send!");
					}else{
						tv_recevice.setText("Data can not sent!");
					}
					break;
				case R.id.btn_setTime:
					isSend = mUSBConnectUtil.sendMessageToPoint(Command.getSetTimeCmd());
					if(isSend){
						tv_recevice.setText("Data send!");
					}else{
						tv_recevice.setText("Data can not sent!");
					}
					break;
				case R.id.btn_setQR:
					isSend = mUSBConnectUtil.sendMessageToPoint(Command.transToPrintText((et_QR.getText().toString().split("\n"))[0]));
					if(isSend){
						tv_recevice.setText("Data send!");
					}else{
						tv_recevice.setText("Data can not sent!");
					}
					break;
					
				case R.id.btn_Red:
					isSend = mUSBConnectUtil.sendMessageToPoint(Command.getColorCmd(Command.RED));
					if(isSend){
						tv_recevice.setText("Data send!");
					}else{
						tv_recevice.setText("Data can not sent!");
					}
					break;
				case R.id.btn_Blue:
					isSend = mUSBConnectUtil.sendMessageToPoint(Command.getColorCmd(Command.BLUE));
					if(isSend){
						tv_recevice.setText("Data send!");
					}else{
						tv_recevice.setText("Data can not sent!");
					}
					break;
				case R.id.btn_Green:
					isSend = mUSBConnectUtil.sendMessageToPoint(Command.getColorCmd(Command.GREEN));
					if(isSend){
						tv_recevice.setText("Data send!");
					}else{
						tv_recevice.setText("Data can not sent!");
					}
					break;
				case R.id.btn_Black:
					isSend = mUSBConnectUtil.sendMessageToPoint(Command.getColorCmd(Command.BLACK));
					if(isSend){
						tv_recevice.setText("Data send!");
					}else{
						tv_recevice.setText("Data can not sent!");
					}
					break;
				case R.id.btn_White:
					isSend = mUSBConnectUtil.sendMessageToPoint(Command.getColorCmd(Command.WHITE));
					if(isSend){
						tv_recevice.setText("Data send!");
					}else{
						tv_recevice.setText("Data can not sent!");
					}
					break;
			
				}
				
				
			}
		}
			
	};

	private  boolean serialWrite(byte[] cmd){
		if(MainBoardUtil.isRK3288()) {
			return  serialWrite1(cmd);
		}else{
			return serialWrite1(cmd);
		}
	}
	
	private  boolean serialWrite1(byte[] cmd){
    	boolean returnValue=true;
    	try{
		
			mOutputStream.write(cmd);
    	}
    	catch(Exception ex)
    	{
    		returnValue=false;
    	}
    	return returnValue;
    }

	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (tv_recevice != null) {
					tv_recevice.append(new String(buffer, 0, size));
				}
			}
		});

	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(isPDtest == false) return;
			switch (msg.what) {
				case PD_AUTO_START:

					if(pd_cmd_id >= cmdPD3.length -1){
						pd_cmd_id = 0;
					}else {
						pd_cmd_id = pd_cmd_id + 1;
					}
					serialWrite(Command.transToPrintText(cmdPD3[pd_cmd_id]));
					handler.sendEmptyMessageDelayed(PD_AUTO_START, 1000);
					break;
				case PD_AUTO_STOP:
					serialWrite(Command.transToPrintText(cmdPD2[1]));
					pd_cmd_id = -1;
					isPDtest = false;
					break;
				default:
					break;
			}
		}
	};

}

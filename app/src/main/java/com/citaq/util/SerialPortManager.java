package com.citaq.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.citaq.citaqfactory.R;
import com.printer.util.CallbackSerial;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

public class SerialPortManager {
	private static final String TAG = "SerialPortManager";

	public static final int PRINTSERIALPORT_TTYS1 = 0;
	public static final int PRINTSERIALPORT_TTYMT0 = 2;
	public static final int MSRSERIALPORT_TTYS2 = 3;
	public static final int SERIALPORT_TTYS1 = 4;
	public static final int SERIALPORT_TTYS3 = 5;
	public static final int CTMDISPLAYSERIALPORT_TTYS3 = 6;
	public static final int CTMDISPLAYSERIALPORT_TTYACM0 = 7;
	public static final int CTMDISPLAYSERIALPORT_TTYS4 = 8;

	Context mContext;
	private static final boolean isPrintLog = true;
	private SerialPort mSerialPort = null;
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private boolean isStop = false;
	protected ReadThread mReadThread;

	CallbackSerial mCallbackSerial;

	boolean initState = true;

	public void setCallback(CallbackSerial mCallback) {
		mCallbackSerial = mCallback;

		getInputStream();

		mReadThread = new ReadThread();
		mReadThread.start();
	}

	public SerialPortManager(Context context,int type){
		mContext = context;
		try {
			switch (type) {
				case PRINTSERIALPORT_TTYS1:
					getPrintSerialPort();
					break;
				case PRINTSERIALPORT_TTYMT0:
					getPrintSerialPortMT();
					break;
				case MSRSERIALPORT_TTYS2:
					getMSRSerialPort();
					break;
				case SERIALPORT_TTYS1:
					getttyS1();
					break;
				case SERIALPORT_TTYS3:
					getttyS3();
					break;
				case CTMDISPLAYSERIALPORT_TTYS3:
					getCtmDisplaySerialPort();
					break;
				case CTMDISPLAYSERIALPORT_TTYACM0:
					getCtmDisplaySerialPort2();
					break;
				case CTMDISPLAYSERIALPORT_TTYS4:
					getMSRSerialPort_S4();
					break;
				default:
					initState =false;
			}

			if(initState)
				getOutputStream();


		} catch (SecurityException e) {
			DisplayError(R.string.error_security);
		} catch (IOException e) {
			DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
			DisplayError(R.string.error_configuration);
		}
	}

	
	private SerialPort openSerialPort(File device, int baudrate, int stopBits, int dataBits, int parity, boolean flowCon){
		closeSerialPort();
		try {
			mSerialPort = new SerialPort(device, baudrate, stopBits, dataBits, parity, flowCon);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return mSerialPort;
	}

	private SerialPort getPrintSerialPort() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Open the serial port */
			return openSerialPort(new File("/dev/ttyS1"), 115200, 1,8,0, true);
		}
		return mSerialPort;
	}

	private SerialPort getPrintSerialPortMT() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Open the serial port */
//			mSerialPort = new SerialPort(new File("/dev/ttyMT0"), 115200, 0, true);
			return openSerialPort(new File("/dev/ttyMT0"), 115200, 1,8,0, true);
		}
		return mSerialPort;
	}

	private SerialPort getMSRSerialPort() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Open the serial port */
//			mSerialPort = new SerialPort(new File("/dev/ttyS2"), 19200, 0, false);
			return openSerialPort(new File("/dev/ttyS2"), 19200, 1,8,0, false);
		}
		return mSerialPort;
	}

	private SerialPort getttyS1() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Open the serial port */
			return openSerialPort(new File("/dev/ttyS1"), 9600, 1,8,0, false);
		}
		return mSerialPort;
	}

	private SerialPort getttyS3() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Open the serial port */
			return openSerialPort(new File("/dev/ttyS3"), 9600, 1,8,0, false);
		}
		return mSerialPort;
	}

	private SerialPort getCtmDisplaySerialPort() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Open the serial port */
//			mSerialPort = new SerialPort(new File("/dev/ttyS3"), 9600, 0, false);
			return openSerialPort(new File("/dev/ttyS3"), 9600, 1,8,0, false);
		}
		return mSerialPort;
	}

	private SerialPort getCtmDisplaySerialPort2() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Open the serial port */
//			mSerialPort = new SerialPort(new File("/dev/ttyACM0"), 2400, 0, false);
			return openSerialPort(new File("/dev/ttyACM0"), 2400, 1,8,0, false);
		}
		return mSerialPort;
	}

	private SerialPort getMSRSerialPort_S4() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Open the serial port */
//			mSerialPort = new SerialPort(new File("/dev/ttyS4"), 19200, 0, false);
			return openSerialPort(new File("/dev/ttyS4"), 19200, 1,8,0, false);
		}
		return mSerialPort;
	}

	
	private void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}
	
    private InputStream getInputStream(){
    	if(mSerialPort == null){
    		return null;
    	}
    	if(mInputStream == null) {
			try {
				mInputStream = mSerialPort.getInputStream();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return mInputStream;
		}
		return mInputStream;
    }
    
    private OutputStream getOutputStream(){
    	if(mSerialPort == null){
    		return null;
    	}
    	if(mOutputStream == null) {
			try {
				mOutputStream = mSerialPort.getOutputStream();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return mOutputStream;
		}
    	return mOutputStream;
    }

	protected class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while(!isStop && !isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mInputStream == null) return;
					size = mInputStream.read(buffer);
					if (size > 0) {
						mCallbackSerial.onDataReceived(buffer, size);
						//stopread();

					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}

		public void cancel(){
			interrupt();
		}

		@Override
		public void start() {
			// TODO Auto-generated method stub
			super.start();
			isStop = false;
		}


	}


	public void destroy(){
		stopread();
		closeSerialPort();
	}


	//停止读取线程
	public void stopread(){
		isStop = true;
		if(mReadThread != null){
			mReadThread.cancel();
			mReadThread = null;
		}
	}

	public boolean write(byte[] cmd){

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

	public boolean  getInitState(){
		return initState;
	}

	public void DisplayError(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(mContext);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				((Activity) mContext).finish();
			}
		});
		b.show();
	}

}

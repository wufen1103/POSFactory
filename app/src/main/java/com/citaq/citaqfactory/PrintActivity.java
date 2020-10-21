package com.citaq.citaqfactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.citaq.util.BitmapUtil;
import com.citaq.util.Command;
import com.citaq.util.MainBoardUtil;
import com.citaq.util.SerialPortManager;
import com.citaq.view.ProgressListFileDialog;
import com.printer.util.BytesUtil;
import com.printer.util.CallbackSerial;
import com.printer.util.CallbackUSB;
import com.printer.util.DataQueue;
import com.printer.util.USBConnectUtil;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PrintActivity extends Activity {
	private static final String TAG  ="PrintActivity2";
	
	Context mContext;
	
	private Button btn_Opencash;
	private Button btn_cutPaper;
	private Button btn_GetprintStatus;
	private Button btn_Printtest;
	private Button btn_Printdemo;
	private Button btn_PrintQR;
	private Button btn_OpenPicture;
	private Button btn_PrintPicture;
	private Button btn_disableChinese;
	
	private EditText et_cmd;
	private Button btn_cmd;
	
	private Button bt_More;
	
	private Bitmap mBitmap = null;
	static private int openfileDialogId = 0;
	
	int mCurrentBt = -1;
	
	
	TextView tv_Reception;
	ImageView imageForPrint;
	
	Spinner spinnerCP,spinnerCS,spinnerResidentCS;
	private ArrayAdapter<?> cpAdapter,csAdapter,residentCsAdapter;
	private int cpIndex,csIndex,residentCsIndex;
	
	////////////////
	USBConnectUtil mUSBConnectUtil = null;

	SerialPortManager mSerialPortManager = null;
	
	private ArrayAdapter<?> adapter_type, adapter_cmd;
	
	RadioGroup print_type;
	RadioButton print_serial = null;
	RadioButton print_usb = null;
	int printType = 0;
	
	SendThread mSendThread;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mContext = this;
		
		setContentView(R.layout.activity_print);
		
		String model = MainBoardUtil.getCpuHardware();
		
		if(MainBoardUtil.isRK3188() || MainBoardUtil.isRK3368() || MainBoardUtil.isRK30BOARD() || MainBoardUtil.isRK3368_8_1()){
			initSerial();
		}else{
			
		}


		initView();
		
		mSendThread = new SendThread();
		mSendThread.start();

		if(MainBoardUtil.isRK3188() || MainBoardUtil.isRK3368() || MainBoardUtil.isRK30BOARD() || MainBoardUtil.isRK3368_8_1()){
			
		}else{
			print_usb.setChecked(true);
			print_serial.setEnabled(false);
			bt_More.setVisibility(View.INVISIBLE);
		}
		
	}
	
	private void initSerial(){
		mSerialPortManager = new SerialPortManager(this,SerialPortManager.PRINTSERIALPORT_TTYS1);
		mSerialPortManager.setCallback(new CallbackSerial() {
			@Override
			public void onDataReceived(final byte[] buffer, final int size) {
				if(mCurrentBt == R.id.btn_getprintstatus){
					runOnUiThread(new Runnable() {
						public void run() {
					/*for(int i = 0; i < size; i++){
							String s = Integer.toHexString((int)buffer[i]);//String.valueOf(((char)buffer[i]));
							tv_Reception.append(s + ' ');
					}*/
							if(size > 0){
								String debstr;
								debstr = "Rec " + size + " bytes(Serial):   ";
								for (int i = 0; i < size; i++) {
									String s;
									if(buffer[i] < 0){
										s = Integer.toHexString(256 + buffer[i]);//String.valueOf(((char)buffer[i]));
									}
									else {
										s = Integer.toHexString(buffer[i]);//String.valueOf(((char)buffer[i]));
									}

									if(s.length() < 2){
										s = "0x0" + s + ',';
									}else{
										s = "0x" + s + ',';
									}
									debstr += s;
								}
								debstr += "\r\n";
								System.out.println(debstr);
								tv_Reception.append(debstr);
							}
						}

					});
				}
			}
		});
	}
	
	private void initUSBConnect() {       // remember to destroyPrinter on
		if(mUSBConnectUtil == null){
			 mUSBConnectUtil = USBConnectUtil.getInstance();
		       
		        
			 mUSBConnectUtil.setCallback(new CallbackUSB() {
					
					@Override
					public void callback(final String str,boolean toShow) {
						Log.v(TAG, str.toString());
						if(toShow)
							runOnUiThread(new Runnable() {
								public void run() {
									 tv_Reception.append(str);
								}
							});
					}

					@Override
					public void hasUSB(boolean hasUSB) {
						if(!hasUSB) Toast.makeText(mContext, R.string.nousbdevice, Toast.LENGTH_SHORT).show();
						
					}
	
				});
		        
			 mUSBConnectUtil.initConnect(this,USBConnectUtil.TYPE_PRINT);
		}
			
	}
	
	private void initView(){
		btn_Opencash = (Button) findViewById(R.id.btn_opencash);
		btn_Opencash.setOnClickListener(SendPrintListener);
		
		btn_cutPaper = ((Button) findViewById(R.id.btn_cutPaper));
		btn_cutPaper.setOnClickListener(SendPrintListener);
		
		btn_GetprintStatus = (Button) findViewById(R.id.btn_getprintstatus);
		btn_GetprintStatus.setOnClickListener(SendPrintListener);
		
		btn_Printtest = (Button) findViewById(R.id.btn_printtest);
		btn_Printtest.setOnClickListener(SendPrintListener);
		
		btn_Printdemo = (Button) findViewById(R.id.btn_printdemo);
		btn_Printdemo.setOnClickListener(SendPrintListener);

		btn_PrintQR = (Button) findViewById(R.id.btn_printQR);
		btn_PrintQR.setOnClickListener(SendPrintListener);

		btn_disableChinese = (Button) findViewById(R.id.btn_disableChinese);
		btn_disableChinese.setOnClickListener(SendPrintListener);
		
		btn_OpenPicture = (Button) findViewById(R.id.btn_openPicture);
		btn_OpenPicture.setOnClickListener(SendPrintListener);
		
		btn_PrintPicture = (Button) findViewById(R.id.btn_printPicture);
		btn_PrintPicture.setOnClickListener(SendPrintListener);
		
		et_cmd = (EditText) findViewById(R.id.et_cmd);
		btn_cmd = (Button) findViewById(R.id.btn_cmd);
		btn_cmd.setOnClickListener(SendPrintListener);

		bt_More = (Button) findViewById(R.id.bt_more);
		bt_More.setOnClickListener(SendPrintListener);
		
		tv_Reception = (TextView) findViewById(R.id.tv_printReception);
		imageForPrint = (ImageView) findViewById(R.id.imageForPrint);
		

		adapter_type= ArrayAdapter.createFromResource(this, R.array.PD_type, android.R.layout.simple_spinner_item);
		adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		
		print_type = (RadioGroup) findViewById(R.id.print_type);
		print_serial = (RadioButton) findViewById(R.id.print_serial);
		print_usb = (RadioButton) findViewById(R.id.print_usb);
		
		print_type.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(print_serial.getId()==checkedId){
					printType = 0;
				}else if(print_usb.getId()==checkedId){
					printType = 1;
					if(mUSBConnectUtil == null){
						initUSBConnect();
					}
				}
				
			}
		});
		
		
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.banma);;//打开图片文件
		imageForPrint.setImageBitmap(mBitmap);
		
	/*	List<VolumeInfo> list = mStorageManager.getVolumes(); 
		for (VolumeInfo volumeInfo : list) { 
			if (volumeInfo.getType() == 0) { 
				DiskInfo diskInfo = volumeInfo.getDisk(); 
				if (diskInfo != null && (diskInfo.isUsb())) { 
					int i = volumeInfo.getState(); 
					//volumeInfo.getPath()通过这个方法就能取得路径 
					//这里的Volume就是U盘的信息了 
					} 
				} 
			}*/
				
	
	//	getStoragePath(mContext,"USB");
		if(MainBoardUtil.isRK3288() || MainBoardUtil.isAllwinnerA63()){
			et_cmd.setText("1DH 47H 08H 01H");
		}
	}
	
	/**
     * 6.0获取外置sdcard和U盘路径，并区分
     * @param mContext
     * @param keyword  SD = "内部存储"; EXT = "SD卡"; USB = "U盘"
     * @return
     */
    public static String getStoragePath(Context mContext,String keyword) {
        String targetpath = "";
        StorageManager mStorageManager = (StorageManager) mContext
                .getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            
            Method getPath = storageVolumeClazz.getMethod("getPath");
                  
            Object result = getVolumeList.invoke(mStorageManager);
            
            final int length = Array.getLength(result);
            
            Method getUserLabel = storageVolumeClazz.getMethod("getUserLabel");
            
            
            for (int i = 0; i < length; i++) {
                
                Object storageVolumeElement = Array.get(result, i);
               
                String userLabel = (String) getUserLabel.invoke(storageVolumeElement);
                
                String path = (String) getPath.invoke(storageVolumeElement);
                
                if(userLabel.contains(keyword)){
                    targetpath = path;
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return targetpath ;
    }
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		et_cmd.clearFocus();

		Log.v(TAG, "onResume");
	}
	
	
	OnClickListener SendPrintListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			mCurrentBt = v.getId();
			switch(mCurrentBt){
			case R.id.btn_opencash:
				
				printerWrite(Command.openCash);
				
				break;
			case R.id.btn_cutPaper:
				printerWrite(Command.cutPaper);
				break;
			case R.id.btn_getprintstatus:
				printerWrite(Command.printStatus);
				break;
			case R.id.btn_printtest:
				if(MainBoardUtil.isRK3288() || MainBoardUtil.isAllwinnerA63()){
					printerWrite(Command.printTest2);
				}else{
					printerWrite(Command.printTest);
				}
				break;
			case R.id.btn_printdemo:
//				printerWrite(testData());
				if(getLanguageEnv()){
					printerWrite(Command.getPrintDemoZH());
				}else{
					printerWrite(Command.getPrintDemo());
				}
				break;
				case R.id.btn_printQR:
					printerWrite(Command.getQRCmd("Thank you for using CITAQ printer!"));
					break;
			case R.id.btn_openPicture:
//				mBitmap = null;

				showListDialog();
				break;
			case R.id.btn_printPicture:
				if(mBitmap != null)
				{
					
					/*Thread thread=new Thread(new Runnable()  
			        {  
			            @Override  
			            public void run()  
			            {  
			            	printerWrite(Command.getPrintPictureCmd(mBitmap));
			            }  
			        });  
			        thread.start();*/
					printerWrite(Command.getPrintPictureCmd(BitmapUtil.adjustSize(mBitmap)));
					
				}
				break;
				
				//////////
			case R.id.btn_setCodepage:
				printerWrite(Command.getCodepage(cpIndex));//扩展字符集 //参数！！！！！！修改  Spinner
				break;
				
			case R.id.btn_setCharacterSet:
				printerWrite(Command.getCharacterSet(csIndex));//国际字符集 //参数！！！！！！修改  Spinner
				break;
			case R.id.btn_setResidentCharacterSet:
				printerWrite(Command.getResidentCharacterSet(residentCsIndex));
				break;
			case R.id.btn_enableChinese:
				printerWrite(Command.getChineseMode(1));
				break;
			case R.id.btn_disableChinese:
				printerWrite(Command.getChineseMode(0));
				break;
			case R.id.btn_enableBuzzer:
				printerWrite(Command.getBuzzer(1));
				break;
			case R.id.btn_disableBuzzer:
				printerWrite(Command.getBuzzer(0));
				break;
			case R.id.btn_cmd:
				String cmd = et_cmd.getText().toString();
				if(cmd.trim().length()>0){
//					cmd= cmd +"\n";
					//{ 0x1D, 0x47, (byte) 0x08, (byte) 0x01 };
					//byte[] data= BytesUtil.getBytesFromHexString("1D 47 08 01".replaceAll(" ", ""););  //no H
					//et_cmd.setText("1D 47 08 01");
					byte[] data=Command.transToPrintText(cmd);  //has H
					printerWrite(data);
				}
				break;
			case R.id.bt_more:
				Intent intent = new Intent(mContext, PrintMoreActivity.class);
				intent.putExtra("Print_type",printType);

				mContext.startActivity(intent);
			}
		}
			
	};
	private boolean printerWrite(byte[] cmd){
		boolean returnValue = true;
		
		boolean needSubpackage;		
		if(printType == 0){   //serial
			needSubpackage = false;
		}else{   //usb
			needSubpackage = true;
		}
		
		
		//neddSubpackage if set false ,len is not used
		mSendThread.addData(cmd,needSubpackage,1024);
		
		/*byte[] printText = new byte[1];
		printText[0] = 0x0a;
		mSendThread.addData(printText,true,1024);*/
		
		return returnValue;
	}
	
	private  boolean serialWrite(byte[] cmd){
    	boolean returnValue=true;
    	try{

			mSerialPortManager.write(cmd);
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    		returnValue=false;
    		
    		// more 后返回 java.io.IOException: write failed: EBADF (Bad file number)

    		initSerial();
    		
    		try{

				mSerialPortManager.write(cmd);
        	}
        	catch(Exception e)
        	{
        		ex.printStackTrace();
        		returnValue=false;
        	}
    		
    	}
    	return returnValue;
    }
	
	private  boolean usbWrite(byte[] cmd){
		return mUSBConnectUtil.sendMessageToPrint(cmd);
    }
	
    private boolean getLanguageEnv() {  
        Locale l = Locale.getDefault();  
        String language = l.getLanguage();  
        if ("zh".equals(language)) {  
            return true;
        } 
        return false;  
    }  

	
	public class SendThread extends Thread {  
		DataQueue list = new DataQueue();  
		boolean isrun = true;
		
		public void addData(byte[] cmd, boolean needSubpackage, int len){
			if(len > 0 ){
				list.enQueue(cmd, needSubpackage,len);
			}
	
		}
		
		private void stopRun() {
			isrun = false;

		}
		
	    public void run(){
	    	while(true && isrun){
	    		if(list.QueueLength()>0){
	    			//打印
	    			byte[] data = list.deQueue();
	    			if( data != null){
	    				if(printType == 0){   //serial
	    				 serialWrite(data);
	    			}else{   //usb
	    				 usbWrite(data);
	    			   }
	    			}
	    			
	    			/*try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
	    		}
	    	}
	       
	    }  
	} 
/*	private  byte [] testData(){
		String res = "";   
		 byte [] buffer= new byte[] {0x20,0x0A, 0x1D, 0x56, 0x42, 0x00 };;
		try{   
		    //得到资源中的Raw数据流  
		    InputStream in = getResources().openRawResource(R.raw.test);   
		   
		    //得到数据的大小  
		    int length = in.available();         
		  
		    buffer = new byte[length];          
		  
		    //读取数据  
		    in.read(buffer);           

		    
		    //关闭      
		    in.close();  
		    
		   
		  
		   }catch(Exception e){  
		   e.printStackTrace();
		   }
		return buffer;
	}
*/
	ProgressListFileDialog mProgressListFileDialog;
	private void showListDialog() {
		mProgressListFileDialog = new ProgressListFileDialog(mContext, "/",  new ProgressListFileDialog.ProgressListDialogListener() {
			@Override
			public void onListItemLongClick(int position, String path) {
//				Toast.makeText(mContext, path + "  " + position,Toast.LENGTH_SHORT).show();
				int dix = path.lastIndexOf('.');
				if(dix<0){
					Toast.makeText(mContext, "Error!", Toast.LENGTH_SHORT).show();
				}else{
					String sf = path.substring(dix+1);
					if (sf.equals("jpg") || sf.equals("png") || sf.equals("jpeg") || sf.equals("bmp")) {
						mBitmap = BitmapFactory.decodeFile(path);//打开图片文件
						//显示要处理的图片
						imageForPrint.setImageBitmap(mBitmap);
						mProgressListFileDialog.dismiss();
					}else{
						Toast.makeText(mContext, "It's not a picture", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		mProgressListFileDialog.setTitle("FileDialog");
		mProgressListFileDialog.show();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.v(TAG, "onStop");
	}
	 
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mUSBConnectUtil != null)
			mUSBConnectUtil.destroyPrinter();
		if(mSerialPortManager != null)
			mSerialPortManager.destroy();
		
		mSendThread.stopRun();
		mSendThread = null;
		
		Log.v(TAG, "onDestroy");
	}
	 

}
